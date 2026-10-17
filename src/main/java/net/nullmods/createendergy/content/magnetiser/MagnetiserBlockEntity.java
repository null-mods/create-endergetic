package net.nullmods.createendergy.content.magnetiser;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.recipe.RecipeApplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.nullmods.createendergy.EndergyRecipeTypes;
import net.nullmods.createendergy.content.magnet.MagnetisedEnchanting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Charges while it turns and there is something to magnetise below it; at full charge the
 * conductors touch, the item is processed and they spring back. Both sides run the same tick
 * so the animation is smooth without a packet per tick, but only the server decides when the
 * discharge happens, so the item is processed once.
 *
 * What it can do to an item: a magnetising recipe, or else {@link MagnetisedEnchanting} (books
 * and tools gain the Magnetised enchantment).
 *
 * Targets, checked in this order, mirroring Create's press, which works across a one-block
 * gap: an item held on a belt or depot two blocks below (one item per discharge), an item in a
 * basin two blocks below (one per discharge), and items lying in the gap (the whole stack).
 * Belts and depots only look for a processing machine two blocks above them, so this spacing
 * is what makes them hold the item under it.
 */
public class MagnetiserBlockEntity extends KineticBlockEntity {
    /** At 32 RPM a full charge takes 5 seconds; it scales with speed. */
    private static final float CHARGE_PER_RPM_TICK = 1 / 3200f;
    /** Charge lost per tick when it has nothing to do, so the conductors relax. */
    private static final float DRAIN_PER_TICK = 0.02f;
    private static final int CONTACT_TICKS = 4;
    private static final int RECOVER_TICKS = 8;
    private static final int SCAN_INTERVAL = 5;
    /** How far from a belt segment's centre an item still counts as under the machine. */
    private static final float BELT_REACH = .5f;

    private static final int NO_TARGET = -1, TARGET_BELT = 0, TARGET_BASIN = 1, TARGET_GROUND = 2;
    /** Block event sent to clients on discharge; its parameter is which target was struck. */
    static final int EVENT_DISCHARGE = 1;
    private static final int BOLT_TICKS = CONTACT_TICKS;
    /** Where the bolt leaves the touching conductor tips, relative to this block. */
    static final Vec3 BOLT_START = new Vec3(.5, .1, .5);

    private float charge;
    private float prevCharge;
    /** Ticks left of the contact-and-recover motion after a discharge; 0 while charging. */
    private int afterDischarge;
    /** Whether there is something below to work on. Decided by the server and synced. */
    private boolean hasTarget;
    private int scanCooldown;
    /** Client only: ticks left of the visible bolt, and what it is striking. */
    private int boltTicks;
    private int boltTarget = NO_TARGET;

    public MagnetiserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        // Belts and depots ask the block above them what to do with an item reaching the centre
        behaviours.add(new BeltProcessingBehaviour(this)
                .whenItemEnters((stack, handler) -> holdIfMagnetisable(stack))
                .whileItemHeld((stack, handler) -> holdIfMagnetisable(stack)));
    }

    private ProcessingResult holdIfMagnetisable(TransportedItemStack stack) {
        return getSpeed() != 0 && canMagnetise(stack.stack) ? ProcessingResult.HOLD : ProcessingResult.PASS;
    }

    @Override
    public void tick() {
        super.tick();
        prevCharge = charge;
        if (boltTicks > 0)
            boltTicks--;

        if (!level.isClientSide && --scanCooldown <= 0) {
            scanCooldown = SCAN_INTERVAL;
            boolean found = getSpeed() != 0 && scanTargets(false) != NO_TARGET;
            if (found != hasTarget) {
                hasTarget = found;
                sendData();
            }
        }

        if (afterDischarge > 0) {
            afterDischarge--;
            return;
        }

        if (getSpeed() == 0 || !hasTarget) {
            charge = Math.max(0, charge - DRAIN_PER_TICK);
            return;
        }

        charge = Math.min(1, charge + Math.abs(getSpeed()) * CHARGE_PER_RPM_TICK);
        // The client waits at full charge for the server's word, so the two never strike apart
        if (charge >= 1 && !level.isClientSide)
            discharge();
    }

    private void discharge() {
        charge = 0;
        prevCharge = 0;
        afterDischarge = CONTACT_TICKS + RECOVER_TICKS;
        int target = scanTargets(true);
        scanCooldown = 0;
        sendData();
        level.playSound(null, worldPosition, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.BLOCKS,
                .35f, 1.7f + level.random.nextFloat() * .2f);
        level.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_DISCHARGE, target);
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id != EVENT_DISCHARGE)
            return super.triggerEvent(id, param);
        if (level.isClientSide) {
            boltTicks = BOLT_TICKS;
            boltTarget = param;
            spawnSparks();
        }
        return true;
    }

    private void spawnSparks() {
        Vec3 origin = Vec3.atLowerCornerOf(worldPosition);
        Vec3 end = getBoltEnd();
        Vec3 at = origin.add(end != null ? end : BOLT_START);
        for (int i = 0; i < (end != null ? 12 : 5); i++)
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z,
                    (level.random.nextFloat() - .5f) * .3f, level.random.nextFloat() * .15f, (level.random.nextFloat() - .5f) * .3f);
    }

    public int getBoltTicks() {
        return boltTicks;
    }

    /** Where the bolt lands, relative to this block, or null when the discharge hit nothing. */
    public Vec3 getBoltEnd() {
        return switch (boltTarget) {
            case TARGET_BELT -> new Vec3(.5, -2 + .8, .5);   // belt or depot surface
            case TARGET_BASIN -> new Vec3(.5, -2 + .6, .5);  // items resting in the basin
            case TARGET_GROUND -> new Vec3(.5, -1 + .1, .5); // items lying in the gap
            default -> null;
        };
    }

    @Override
    protected AABB createRenderBoundingBox() {
        // The bolt reaches two blocks down; without this it vanishes when this block is off screen
        return super.createRenderBoundingBox().expandTowards(0, -2, 0);
    }

    /** Looks for something to magnetise; with {@code process} it also magnetises the first one found. */
    private int scanTargets(boolean process) {
        BlockPos gap = worldPosition.below();
        BlockPos target = worldPosition.below(2);

        TransportedItemStackHandlerBehaviour belt = BlockEntityBehaviour.get(level, target, TransportedItemStackHandlerBehaviour.TYPE);
        if (belt != null && onBelt(belt, process))
            return TARGET_BELT;

        if (level.getBlockEntity(target) instanceof BasinBlockEntity basin && inBasin(basin, process))
            return TARGET_BASIN;

        return onGround(gap, process) ? TARGET_GROUND : NO_TARGET;
    }

    private boolean onBelt(TransportedItemStackHandlerBehaviour belt, boolean process) {
        boolean[] found = {false};
        belt.handleCenteredProcessingOnAllItems(BELT_REACH, transported -> {
            if (found[0])
                return TransportedResult.doNothing();
            if (!canMagnetise(transported.stack))
                return TransportedResult.doNothing();
            found[0] = true;
            if (!process)
                return TransportedResult.doNothing();

            // One item per discharge; the rest of the stack stays held for the next one
            List<TransportedItemStack> outputs = new ArrayList<>();
            for (ItemStack result : resultsForOne(transported.stack)) {
                TransportedItemStack output = transported.getSimilar();
                output.stack = result;
                outputs.add(output);
            }
            TransportedItemStack left = transported.copy();
            left.stack.shrink(1);
            if (!left.stack.isEmpty())
                return TransportedResult.convertToAndLeaveHeld(outputs, left);
            return outputs.isEmpty() ? TransportedResult.removeItem() : TransportedResult.convertTo(outputs);
        });
        return found[0];
    }

    private boolean inBasin(BasinBlockEntity basin, boolean process) {
        SmartInventory inputs = basin.getInputInventory();
        for (int slot = 0; slot < inputs.getSlots(); slot++) {
            ItemStack stack = inputs.getStackInSlot(slot);
            if (!canMagnetise(stack))
                continue;
            if (!process)
                return true;

            List<ItemStack> results = resultsForOne(stack);
            // A full basin keeps its item and waits, like Create's own basin machines
            if (!basin.acceptOutputs(results, List.of(), true))
                return true;
            basin.acceptOutputs(results, List.of(), false);
            ItemStack left = stack.copy();
            left.shrink(1);
            inputs.setStackInSlot(slot, left);
            basin.notifyChangeOfContents();
            return true;
        }
        return false;
    }

    private boolean onGround(BlockPos gap, boolean process) {
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(gap))) {
            if (!item.isAlive())
                continue;
            if (!canMagnetise(item.getItem()))
                continue;
            if (!process)
                return true;
            Optional<RecipeHolder<MagnetisingRecipe>> recipe = findRecipe(item.getItem());
            if (recipe.isPresent()) {
                RecipeApplier.applyRecipeOn(item, recipe.get().value(), true);
                return true;
            }
            // Enchanted books and tools don't stack, so these go one per discharge
            for (ItemStack result : resultsForOne(item.getItem()))
                level.addFreshEntity(new ItemEntity(level, item.getX(), item.getY(), item.getZ(), result, 0, 0, 0));
            item.getItem().shrink(1);
            if (item.getItem().isEmpty())
                item.discard();
            return true;
        }
        return false;
    }

    private boolean canMagnetise(ItemStack stack) {
        return findRecipe(stack).isPresent() || !MagnetisedEnchanting.apply(level.registryAccess(), stack).isEmpty();
    }

    /** What one of {@code stack} turns into: its recipe's results, or the enchanted copy. */
    private List<ItemStack> resultsForOne(ItemStack stack) {
        ItemStack one = stack.copyWithCount(1);
        Optional<RecipeHolder<MagnetisingRecipe>> recipe = findRecipe(one);
        if (recipe.isPresent())
            return RecipeApplier.applyRecipeOn(level, one, recipe.get().value(), true);
        ItemStack enchanted = MagnetisedEnchanting.apply(level.registryAccess(), one);
        return enchanted.isEmpty() ? List.of() : List.of(enchanted);
    }

    private Optional<RecipeHolder<MagnetisingRecipe>> findRecipe(ItemStack stack) {
        if (stack.isEmpty() || level == null)
            return Optional.empty();
        return level.getRecipeManager().getRecipeFor(
                EndergyRecipeTypes.MAGNETISING.<SingleRecipeInput, MagnetisingRecipe>getType(), new SingleRecipeInput(stack), level);
    }

    /**
     * How far the conductors have swung toward each other: 0 apart, 1 touching. They speed up
     * as the charge builds, hold together on contact, then spring back.
     */
    public float getConductorProgress(float partialTicks) {
        if (afterDischarge > 0) {
            float left = afterDischarge - partialTicks;
            if (left > RECOVER_TICKS)
                return 1;
            float t = Mth.clamp(left / RECOVER_TICKS, 0, 1);
            return t * t;
        }
        float c = Mth.lerp(partialTicks, prevCharge, charge);
        return c * c;
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putFloat("Charge", charge);
        compound.putInt("AfterDischarge", afterDischarge);
        compound.putBoolean("HasTarget", hasTarget);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        charge = compound.getFloat("Charge");
        afterDischarge = compound.getInt("AfterDischarge");
        hasTarget = compound.getBoolean("HasTarget");
        // A sync replaces the client's own guess; don't interpolate across the jump
        if (clientPacket)
            prevCharge = charge;
    }
}
