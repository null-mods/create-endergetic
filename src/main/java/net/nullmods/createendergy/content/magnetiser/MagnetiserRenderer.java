package net.nullmods.createendergy.content.magnetiser;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.nullmods.createendergy.EndergyPartialModels;

/**
 * Draws the moving parts; the casing is the block's static model. This extends
 * SafeBlockEntityRenderer rather than KineticBlockEntityRenderer on purpose: the latter
 * draws nothing while Flywheel is active, which is the default with Create.
 */
public class MagnetiserRenderer extends SafeBlockEntityRenderer<MagnetiserBlockEntity> {
    // Rotation axes, in model pixels, measured from the Blockbench model. Both run along X.
    private static final float SHAFT_Y = 9, SHAFT_Z = 8;
    private static final float CIRCLE_Y = 11.2f, CIRCLE_Z = 7.59f;
    // Conductor hinges, and the swing that brings the two tips together (computed from the tip
    // positions). The conductors sit along X, so they swing about Z.
    private static final float HINGE_Y = 6, LEFT_HINGE_X = 15.5f, RIGHT_HINGE_X = 0.5f;
    private static final float CONDUCTOR_SWING = (float) Math.toRadians(9.8);

    public MagnetiserRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(MagnetiserBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
            int light, int overlay) {
        BlockState state = be.getBlockState();
        Direction.Axis axis = state.getValue(HorizontalAxisKineticBlock.HORIZONTAL_AXIS);
        VertexConsumer vc = buffer.getBuffer(RenderType.cutoutMipped());
        // Create's own formula, so the shaft lines up with the shafts next to it
        float angle = KineticBlockEntityRenderer.getAngleForBe(be, be.getBlockPos(), axis);

        render(EndergyPartialModels.MAGNETISER_SHAFT, state, axis, Direction.Axis.X, angle, 0, SHAFT_Y, SHAFT_Z, light, ms, vc);
        render(EndergyPartialModels.MAGNETISER_CIRCLE_LEFT, state, axis, Direction.Axis.X, angle, 0, CIRCLE_Y, CIRCLE_Z, light, ms, vc);
        render(EndergyPartialModels.MAGNETISER_CIRCLE_RIGHT, state, axis, Direction.Axis.X, -angle, 0, CIRCLE_Y, CIRCLE_Z, light, ms, vc);
        float swing = be.getConductorProgress(partialTicks) * CONDUCTOR_SWING;
        render(EndergyPartialModels.MAGNETISER_CONDUCTOR_LEFT, state, axis, Direction.Axis.Z, -swing, LEFT_HINGE_X, HINGE_Y, 0, light, ms, vc);
        render(EndergyPartialModels.MAGNETISER_CONDUCTOR_RIGHT, state, axis, Direction.Axis.Z, swing, RIGHT_HINGE_X, HINGE_Y, 0, light, ms, vc);

        if (be.getBoltTicks() > 0)
            renderBolt(be, ms, buffer);
    }

    private static final int BOLT_SEGMENTS = 8;
    private static final float BOLT_JITTER = .22f;

    /** A jagged line from the conductor tips to the target, re-drawn with new kinks every tick so it flickers. */
    private static void renderBolt(MagnetiserBlockEntity be, PoseStack ms, MultiBufferSource buffer) {
        Vec3 end = be.getBoltEnd();
        if (end == null)
            return;
        VertexConsumer vc = buffer.getBuffer(RenderType.lightning());
        PoseStack.Pose pose = ms.last();
        RandomSource random = RandomSource.create(be.getBlockPos().asLong() ^ be.getLevel().getGameTime());
        Vec3 start = MagnetiserBlockEntity.BOLT_START;
        Vec3 prev = start;
        for (int i = 1; i <= BOLT_SEGMENTS; i++) {
            Vec3 next = start.lerp(end, i / (float) BOLT_SEGMENTS);
            if (i < BOLT_SEGMENTS)
                next = next.add((random.nextFloat() - .5f) * BOLT_JITTER, 0, (random.nextFloat() - .5f) * BOLT_JITTER);
            // Wide faint glow in the alloy's teal, then a thin bright core
            segment(vc, pose, prev, next, .07f, 44, 205, 177, 90);
            segment(vc, pose, prev, next, .02f, 210, 255, 248, 230);
            prev = next;
        }
    }

    /** Two crossed ribbons along the segment, each drawn from both sides. */
    private static void segment(VertexConsumer vc, PoseStack.Pose pose, Vec3 a, Vec3 b, float w, int r, int g, int bl, int alpha) {
        ribbon(vc, pose, a, b, w, 0, r, g, bl, alpha);
        ribbon(vc, pose, a, b, 0, w, r, g, bl, alpha);
    }

    private static void ribbon(VertexConsumer vc, PoseStack.Pose pose, Vec3 a, Vec3 b, float dx, float dz,
            int r, int g, int bl, int alpha) {
        float[][] quad = {
                {(float) a.x - dx, (float) a.y, (float) a.z - dz}, {(float) a.x + dx, (float) a.y, (float) a.z + dz},
                {(float) b.x + dx, (float) b.y, (float) b.z + dz}, {(float) b.x - dx, (float) b.y, (float) b.z - dz}};
        for (int i = 0; i < 4; i++)
            vc.addVertex(pose, quad[i][0], quad[i][1], quad[i][2]).setColor(r, g, bl, alpha);
        for (int i = 3; i >= 0; i--)
            vc.addVertex(pose, quad[i][0], quad[i][1], quad[i][2]).setColor(r, g, bl, alpha);
    }

    /** Draws a part turned by {@code angle} about a line along {@code spin} through the pivot (model pixels). */
    private static void render(PartialModel model, BlockState state, Direction.Axis axis, Direction.Axis spin, float angle,
            float pivotX, float pivotY, float pivotZ, int light, PoseStack ms, VertexConsumer vc) {
        SuperByteBuffer part = CachedBuffers.partial(model, state);
        // Same turn as the blockstate's "y": 90, so the parts follow the static casing
        if (axis == Direction.Axis.Z)
            part.center().rotateDegrees(-90, Direction.Axis.Y).uncenter();
        part.translate(pivotX / 16f, pivotY / 16f, pivotZ / 16f)
                .rotate(angle, spin)
                .translate(-pivotX / 16f, -pivotY / 16f, -pivotZ / 16f)
                .light(light)
                .renderInto(ms, vc);
    }
}
