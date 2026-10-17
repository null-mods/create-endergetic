package net.nullmods.createendergy.content.magnet;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class MagnetBlock extends DirectionalBlock implements IWrenchable {
    public static final MapCodec<MagnetBlock> CODEC = simpleCodec(MagnetBlock::new);
    public static final EnumProperty<MagnetPolarity> POLARITY = EnumProperty.create("polarity", MagnetPolarity.class);

    private static final int REACTION_DELAY = 2;

    public MagnetBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POLARITY, MagnetPolarity.DIPOLE));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POLARITY);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction away = context.getNearestLookingDirection();
        Player player = context.getPlayer();
        boolean positiveToPlayer = player != null && player.isShiftKeyDown();
        return defaultBlockState().setValue(FACING, positiveToPlayer ? away.getOpposite() : away);
    }

    public static MagnetPolarity polarityOf(ItemStack stack) {
        BlockItemStateProperties properties = stack.get(DataComponents.BLOCK_STATE);
        MagnetPolarity polarity = properties == null ? null : properties.get(POLARITY);
        return polarity == null ? MagnetPolarity.DIPOLE : polarity;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Direction face = context.getClickedFace();
        Direction positive = state.getValue(FACING);
        BlockState result = switch (state.getValue(POLARITY)) {
            case DIPOLE -> face == positive ? state.setValue(POLARITY, MagnetPolarity.POSITIVE)
                    : face == positive.getOpposite() ? state.setValue(POLARITY, MagnetPolarity.NEGATIVE)
                    : state.setValue(FACING, positive.getOpposite());
            case POSITIVE -> state.setValue(POLARITY, MagnetPolarity.DIPOLE).setValue(FACING, face);
            case NEGATIVE -> state.setValue(POLARITY, MagnetPolarity.DIPOLE).setValue(FACING, face.getOpposite());
        };
        Level level = context.getLevel();
        if (!level.isClientSide) {
            level.setBlock(context.getClickedPos(), result, Block.UPDATE_ALL);
            IWrenchable.playRotateSound(level, context.getClickedPos());
        }
        return InteractionResult.SUCCESS;
    }

    // Only a player placement triggers it, and only this block moves, so magnets can't push each other in a loop
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide)
            level.scheduleTick(pos, this, REACTION_DELAY);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        for (Direction face : Direction.values()) {
            Boolean mine = poleFacing(state, face);
            if (mine == null)
                continue;
            Boolean touching = poleFacing(level.getBlockState(pos.relative(face)), face.getOpposite());

            if (touching != null && touching == mine) {
                if (moveTo(level, pos, state, pos.relative(face.getOpposite()), false))
                    return;
                continue;
            }

            BlockPos gap = pos.relative(face);
            Boolean across = poleFacing(level.getBlockState(gap.relative(face)), face.getOpposite());
            if (touching == null && across != null && across != mine && moveTo(level, pos, state, gap, true))
                return;
        }
    }

    private static Boolean poleFacing(BlockState state, Direction direction) {
        if (!(state.getBlock() instanceof MagnetBlock))
            return null;
        return switch (state.getValue(POLARITY)) {
            case POSITIVE -> true;
            case NEGATIVE -> false;
            case DIPOLE -> {
                Direction positive = state.getValue(FACING);
                yield direction == positive ? Boolean.TRUE : direction == positive.getOpposite() ? Boolean.FALSE : null;
            }
        };
    }

    private static boolean moveTo(ServerLevel level, BlockPos from, BlockState state, BlockPos to, boolean attracted) {
        BlockState there = level.getBlockState(to);
        if (!(there.isAir() || there.canBeReplaced()) || level.isOutsideBuildHeight(to))
            return false;
        MovingMagnetEntity.launch(level, from, to, state);
        level.playSound(null, from, attracted ? SoundEvents.PISTON_CONTRACT : SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS,
                .4f, 1.6f + level.random.nextFloat() * .2f);
        return true;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
