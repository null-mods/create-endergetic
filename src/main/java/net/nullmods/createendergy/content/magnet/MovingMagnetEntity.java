package net.nullmods.createendergy.content.magnet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.nullmods.createendergy.EndergyEntityTypes;

/**
 * A magnet block sliding one block after a push or pull. Drawn like falling sand, but it moves
 * in a straight line in any direction, ignoring gravity, and turns back into a block where it
 * was headed. If something took that spot meanwhile, it drops as an item instead.
 */
public class MovingMagnetEntity extends FallingBlockEntity {
    private static final int TRAVEL_TICKS = 6;

    /** Server side only; clients get the state through the spawn packet, as falling blocks do. */
    private BlockState magnet;
    private BlockPos target;
    private int travelled;

    public MovingMagnetEntity(EntityType<? extends MovingMagnetEntity> type, Level level) {
        super(type, level);
    }

    /** Takes the block out of {@code from} and starts it toward {@code to}. */
    public static void launch(Level level, BlockPos from, BlockPos to, BlockState state) {
        MovingMagnetEntity entity = new MovingMagnetEntity(EndergyEntityTypes.MOVING_MAGNET.get(), level);
        entity.magnet = state;
        entity.target = to;
        entity.setPos(from.getX() + .5, from.getY(), from.getZ() + .5);
        entity.xo = entity.getX();
        entity.yo = entity.getY();
        entity.zo = entity.getZ();
        entity.setStartPos(from);
        entity.setNoGravity(true);
        entity.setDeltaMovement(Vec3.atLowerCornerOf(to.subtract(from)).scale(1d / TRAVEL_TICKS));
        level.setBlock(from, state.getFluidState().createLegacyBlock(), Block.UPDATE_ALL);
        level.addFreshEntity(entity);
    }

    @Override
    public void tick() {
        xo = getX();
        yo = getY();
        zo = getZ();
        setPos(position().add(getDeltaMovement()));
        if (level().isClientSide || ++travelled < TRAVEL_TICKS)
            return;

        if (magnet != null && target != null) {
            BlockState there = level().getBlockState(target);
            if (there.isAir() || there.canBeReplaced())
                level().setBlock(target, magnet, Block.UPDATE_ALL);
            else
                spawnAtLocation(magnet.getBlock());
        }
        discard();
    }

    @Override
    public BlockState getBlockState() {
        return magnet != null ? magnet : super.getBlockState();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity, Block.getId(getBlockState()));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (magnet != null)
            tag.put("Magnet", NbtUtils.writeBlockState(magnet));
        if (target != null)
            tag.put("Target", NbtUtils.writeBlockPos(target));
        tag.putInt("Travelled", travelled);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Magnet"))
            magnet = NbtUtils.readBlockState(level().holderLookup(Registries.BLOCK), tag.getCompound("Magnet"));
        target = NbtUtils.readBlockPos(tag, "Target").orElse(null);
        travelled = tag.getInt("Travelled");
    }
}
