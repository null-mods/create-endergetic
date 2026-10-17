package net.nullmods.createendergy.content.accumulator;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.nullmods.createendergy.content.EndergyGoggles;
import net.nullmods.createendergy.content.EndergySource;

import java.util.List;
import java.util.Objects;

public class AccumulatorBlockEntity extends SmartBlockEntity implements IMultiBlockEntityContainer, IHaveGoggleInformation, EndergySource {
    private static final int MAX_WIDTH = 3;
    private static final int MAX_HEIGHT = 32;
    public static final long CAPACITY_PER_BLOCK = 100_000;

    protected BlockPos controller;
    protected BlockPos lastKnownPos;
    protected boolean updateConnectivity;
    protected int width = 1;
    protected int height = 1;
    // Kept per block instead of in the controller, so merging and splitting the multiblock never loses charge
    protected long stored;

    public AccumulatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    protected void updateConnectivity() {
        updateConnectivity = false;
        if (level.isClientSide || !isController())
            return;
        ConnectivityHandler.formMulti(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (lastKnownPos == null)
            lastKnownPos = getBlockPos();
        else if (!lastKnownPos.equals(worldPosition)) {
            removeController(true);
            lastKnownPos = worldPosition;
            return;
        }
        if (updateConnectivity)
            updateConnectivity();
    }

    @Override
    public int getVoltage() {
        return 0;
    }

    @Override
    public BlockPos getSourcePos() {
        return getController();
    }

    public boolean isInfinite() {
        return false;
    }

    public long getCapacity() {
        return (long) width * width * height * CAPACITY_PER_BLOCK;
    }

    public long getTotalStored() {
        long total = 0;
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                for (int z = 0; z < width; z++)
                    if (level.getBlockEntity(worldPosition.offset(x, y, z)) instanceof AccumulatorBlockEntity part
                            && part.getController().equals(worldPosition))
                        total += part.stored;
        return total;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        AccumulatorBlockEntity controller = getControllerBE();
        if (controller == null)
            return false;
        EndergyGoggles.header(tooltip);
        EndergyGoggles.voltage(tooltip, controller.getVoltage());
        if (controller.isInfinite())
            EndergyGoggles.infiniteStorage(tooltip);
        else
            EndergyGoggles.storage(tooltip, controller.getTotalStored(), controller.getCapacity());
        return true;
    }

    @Override
    public boolean isController() {
        return controller == null || controller.equals(worldPosition);
    }

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AccumulatorBlockEntity getControllerBE() {
        if (isController() || !hasLevel())
            return this;
        return level.getBlockEntity(controller) instanceof AccumulatorBlockEntity accumulator ? accumulator : null;
    }

    @Override
    public void setController(BlockPos controller) {
        if (level.isClientSide && !isVirtual())
            return;
        if (controller.equals(this.controller))
            return;
        this.controller = controller;
        setChanged();
        sendData();
    }

    @Override
    public void removeController(boolean keepContents) {
        if (level.isClientSide)
            return;
        updateConnectivity = true;
        controller = null;
        width = 1;
        height = 1;
        BlockState state = getBlockState();
        if (AccumulatorBlock.isAccumulator(state))
            level.setBlock(worldPosition, state.setValue(AccumulatorBlock.TOP, true).setValue(AccumulatorBlock.BOTTOM, true),
                    Block.UPDATE_CLIENTS | Block.UPDATE_INVISIBLE | Block.UPDATE_KNOWN_SHAPE);
        setChanged();
        sendData();
    }

    @Override
    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }

    @Override
    public void notifyMultiUpdated() {
        BlockState state = getBlockState();
        if (AccumulatorBlock.isAccumulator(state)) {
            int bottom = getController().getY();
            state = state.setValue(AccumulatorBlock.BOTTOM, bottom == worldPosition.getY())
                    .setValue(AccumulatorBlock.TOP, bottom + height - 1 == worldPosition.getY());
            level.setBlock(worldPosition, state, Block.UPDATE_CLIENTS | Block.UPDATE_INVISIBLE);
        }
        setChanged();
    }

    @Override
    public Direction.Axis getMainConnectionAxis() {
        return Direction.Axis.Y;
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        return longAxis == Direction.Axis.Y ? MAX_HEIGHT : MAX_WIDTH;
    }

    @Override
    public int getMaxWidth() {
        return MAX_WIDTH;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        BlockPos controllerBefore = controller;
        int widthBefore = width, heightBefore = height;
        updateConnectivity = tag.contains("Uninitialized");
        stored = tag.getLong("Stored");
        lastKnownPos = tag.contains("LastKnownPos") ? NBTHelper.readBlockPos(tag, "LastKnownPos") : null;
        controller = tag.contains("Controller") ? NBTHelper.readBlockPos(tag, "Controller") : null;
        if (isController()) {
            width = tag.getInt("Size");
            height = tag.getInt("Height");
        }
        // Parts whose controller changed may keep the same block state, so their connected textures need a manual refresh
        if (clientPacket && hasLevel()
                && (!Objects.equals(controllerBefore, controller) || widthBefore != width || heightBefore != height))
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 16);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        if (updateConnectivity)
            tag.putBoolean("Uninitialized", true);
        tag.putLong("Stored", stored);
        if (lastKnownPos != null)
            tag.put("LastKnownPos", NbtUtils.writeBlockPos(lastKnownPos));
        if (!isController())
            tag.put("Controller", NbtUtils.writeBlockPos(controller));
        if (isController()) {
            tag.putInt("Size", width);
            tag.putInt("Height", height);
        }
        super.write(tag, registries, clientPacket);
    }
}
