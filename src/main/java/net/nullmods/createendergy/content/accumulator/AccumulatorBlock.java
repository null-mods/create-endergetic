package net.nullmods.createendergy.content.accumulator;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.nullmods.createendergy.EndergyBlockEntityTypes;

public class AccumulatorBlock extends Block implements IWrenchable, IBE<AccumulatorBlockEntity> {
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private final boolean creative;

    public static AccumulatorBlock regular(Properties properties) {
        return new AccumulatorBlock(properties, false);
    }

    public static AccumulatorBlock creative(Properties properties) {
        return new AccumulatorBlock(properties, true);
    }

    protected AccumulatorBlock(Properties properties, boolean creative) {
        super(properties);
        this.creative = creative;
        registerDefaultState(defaultBlockState().setValue(TOP, true).setValue(BOTTOM, true).setValue(POWERED, creative));
    }

    public static boolean isAccumulator(BlockState state) {
        return state.getBlock() instanceof AccumulatorBlock;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TOP, BOTTOM, POWERED);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        if (oldState.getBlock() == state.getBlock() || moved)
            return;
        withBlockEntityDo(level, pos, AccumulatorBlockEntity::updateConnectivity);
        // Forming the multiblock changes the state, which skips the neighbour update that placing would have sent
        BlockState newState = level.getBlockState(pos);
        if (state != newState && newState.getBlock() == this)
            level.markAndNotifyBlock(pos, level.getChunkAt(pos), oldState, newState, UPDATE_ALL_IMMEDIATE, 512);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (state.hasBlockEntity() && (state.getBlock() != newState.getBlock() || !newState.hasBlockEntity())) {
            if (!(level.getBlockEntity(pos) instanceof AccumulatorBlockEntity accumulator))
                return;
            level.removeBlockEntity(pos);
            ConnectivityHandler.splitMulti(accumulator);
        }
    }

    @Override
    public Class<AccumulatorBlockEntity> getBlockEntityClass() {
        return AccumulatorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AccumulatorBlockEntity> getBlockEntityType() {
        return creative ? EndergyBlockEntityTypes.CREATIVE_ACCUMULATOR.get() : EndergyBlockEntityTypes.ACCUMULATOR.get();
    }
}
