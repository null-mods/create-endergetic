package net.nullmods.createendergy.content.wire;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RodBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nullmods.createendergy.EndergyBlockEntityTypes;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.Map;

public class InsulatorBlock extends RodBlock implements EntityBlock {
    public static final MapCodec<InsulatorBlock> CODEC = simpleCodec(InsulatorBlock::new);

    public static final float NECK_BOTTOM = 2.08f, NECK_TOP = 2.88f, NECK_HALF_WIDTH = 1.28f;
    // Bigger than the 3 px model on purpose, so it can still be aimed at
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> COLLISION = new EnumMap<>(Direction.class);

    static {
        for (Direction facing : Direction.values()) {
            SHAPES.put(facing, rotated(6, 0, 6, 10, 4, 10, facing));
            COLLISION.put(facing, rotated(6.4, 0, 6.4, 9.6, 3.52, 9.6, facing));
        }
    }

    public InsulatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends RodBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return COLLISION.get(state.getValue(FACING));
    }

    public static Vec3 attachPoint(BlockPos pos, BlockState state) {
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.UP;
        double fromCentre = (NECK_BOTTOM + NECK_TOP) / 2 / 16 - .5;
        return Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(facing.getNormal()).scale(fromCentre));
    }

    private static VoxelShape rotated(double x0, double y0, double z0, double x1, double y1, double z1,
            Direction facing) {
        Vector3f a = new Vector3f((float) x0 - 8, (float) y0 - 8, (float) z0 - 8).rotate(facing.getRotation());
        Vector3f b = new Vector3f((float) x1 - 8, (float) y1 - 8, (float) z1 - 8).rotate(facing.getRotation());
        return box(Math.min(a.x, b.x) + 8, Math.min(a.y, b.y) + 8, Math.min(a.z, b.z) + 8,
                Math.max(a.x, b.x) + 8, Math.max(a.y, b.y) + 8, Math.max(a.z, b.z) + 8);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return EndergyBlockEntityTypes.INSULATOR.create(pos, state);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !newState.is(this)
                && level.getBlockEntity(pos) instanceof InsulatorBlockEntity insulator)
            insulator.dropWires();
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
