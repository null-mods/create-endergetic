package net.nullmods.createendergy.content.magnet;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.nullmods.createendergy.CreateEndergy;

import java.util.Optional;

/**
 * Pulls nearby dropped items toward a player holding something magnetic: a tool or sword
 * with the Magnetised enchantment, or anything in the {@code create_endergy:magnetic} tag
 * (the magnet and the magnet block), which attracts without needing the enchantment.
 */
@EventBusSubscriber(modid = CreateEndergy.MOD_ID)
public class MagnetHandler {
    public static final ResourceKey<Enchantment> MAGNETISED =
            ResourceKey.create(Registries.ENCHANTMENT, CreateEndergy.asResource("magnetised"));
    public static final TagKey<Item> MAGNETIC = TagKey.create(Registries.ITEM, CreateEndergy.asResource("magnetic"));

    private static final double RANGE = 3;
    private static final double PULL = .35;
    /** Tag other mods put on item entities that must not be moved remotely, e.g. on conveyors. */
    private static final String PREVENT_REMOTE_MOVEMENT = "PreventRemoteMovement";

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();
        if (level.isClientSide || player.isSpectator())
            return;

        Optional<Holder.Reference<Enchantment>> magnetised =
                level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(MAGNETISED);
        if (!isMagnetic(player.getMainHandItem(), magnetised) && !isMagnetic(player.getOffhandItem(), magnetised))
            return;

        Vec3 target = player.position().add(0, player.getBbHeight() / 2, 0);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(RANGE))) {
            // Freshly thrown items keep their pickup delay, so dropping something doesn't snap it straight back
            if (!item.isAlive() || item.hasPickUpDelay() || item.getPersistentData().contains(PREVENT_REMOTE_MOVEMENT))
                continue;
            Vec3 toPlayer = target.subtract(item.position());
            if (toPlayer.lengthSqr() > RANGE * RANGE)
                continue;
            item.setDeltaMovement(item.getDeltaMovement().scale(.5).add(toPlayer.normalize().scale(PULL)));
            item.hasImpulse = true;
        }
    }

    private static boolean isMagnetic(ItemStack stack, Optional<Holder.Reference<Enchantment>> magnetised) {
        if (stack.isEmpty())
            return false;
        if (stack.is(MAGNETIC))
            return true;
        return magnetised.isPresent() && EnchantmentHelper.getItemEnchantmentLevel(magnetised.get(), stack) > 0;
    }
}
