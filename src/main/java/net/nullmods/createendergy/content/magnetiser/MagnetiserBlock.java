package net.nullmods.createendergy.content.magnetiser;

import com.simibubi.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nullmods.createendergy.EndergyBlockEntityTypes;

/** Takes rotation through the shaft that runs along its horizontal axis. */
public class MagnetiserBlock extends HorizontalAxisKineticBlock implements IBE<MagnetiserBlockEntity> {
    // Built for the model's own orientation, with the shaft along X. The outline covers the
    // moving parts so they can be aimed at; only the casing is solid.
    private static final VoxelShape CASING_X = box(1, 5, 1, 15, 13, 15);
    private static final VoxelShape SHAPE_X = Shapes.or(CASING_X,
            box(6.5, 13, 2.5, 10, 16, 12.5),  // circles above it
            box(1.5, 0, 7, 14.5, 5, 9.5));    // conductors below it
    private static final VoxelShape SHAPE_Z = rotateToZ(SHAPE_X);
    private static final VoxelShape CASING_Z = rotateToZ(CASING_X);

    public MagnetiserBlock(Properties properties) {
        super(properties);
    }

    /**
     * Without sneaking it lines up with neighbouring shafts, as Create does. Sneaking skips that,
     * like Create's pillar machines: the shaft points into the clicked face, or along the look
     * direction when clicking a top or bottom face.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown())
            return super.getStateForPlacement(context);
        Direction face = context.getClickedFace();
        Direction.Axis axis = face.getAxis().isHorizontal() ? face.getAxis() : context.getHorizontalDirection().getAxis();
        return defaultBlockState().setValue(HORIZONTAL_AXIS, axis);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HORIZONTAL_AXIS) == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HORIZONTAL_AXIS) == Direction.Axis.X ? CASING_X : CASING_Z;
    }

    /** Hands the discharge event on to the block entity, which draws the bolt on clients. */
    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        BlockEntity be = level.getBlockEntity(pos);
        return be != null && be.triggerEvent(id, param);
    }

    @Override
    public Class<MagnetiserBlockEntity> getBlockEntityClass() {
        return MagnetiserBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MagnetiserBlockEntity> getBlockEntityType() {
        return EndergyBlockEntityTypes.MAGNETISER.get();
    }

    /** Same turn the blockstate applies for axis=z ("y": 90): x' = 1 - z, z' = x. */
    private static VoxelShape rotateToZ(VoxelShape shape) {
        VoxelShape[] result = {Shapes.empty()};
        shape.forAllBoxes((x1, y1, z1, x2, y2, z2) ->
                result[0] = Shapes.or(result[0], Shapes.box(1 - z2, y1, x1, 1 - z1, y2, x2)));
        return result[0];
    }
}
