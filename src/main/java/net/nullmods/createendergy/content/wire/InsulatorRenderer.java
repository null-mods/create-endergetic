package net.nullmods.createendergy.content.wire;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class InsulatorRenderer implements BlockEntityRenderer<InsulatorBlockEntity> {
    private static final int SEGMENTS = 24;
    private static final float THICKNESS = .03f;
    private static final double SAG_PER_BLOCK = .04;
    private static final float WRAP_OVERHANG = .1f, WRAP_INSET = .05f;
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("block/white_concrete");

    public InsulatorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(InsulatorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light,
            int overlay) {
        BlockPos pos = be.getBlockPos();
        Vec3 origin = Vec3.atLowerCornerOf(pos);
        for (InsulatorBlockEntity.Link link : be.getLinks()) {
            // Both ends store the link; only the lower position draws it so each wire is
            // drawn once
            if (pos.asLong() > link.other().asLong())
                continue;
            Vec3 from = be.getAttachPoint(InsulatorBlock.Neck.of(link.outgoing())).subtract(origin);
            Vec3 to = InsulatorBlock.attachPoint(link.other(), be.getLevel().getBlockState(link.other()),
                    InsulatorBlock.Neck.of(!link.outgoing())).subtract(origin);
            int lightTo = LevelRenderer.getLightColor(be.getLevel(), link.other());
            drawWire(ms, buffer.getBuffer(RenderType.cutout()), from, to, light, lightTo, link.type());
        }
        Direction facing = be.getBlockState().getValue(InsulatorBlock.FACING);
        for (InsulatorBlock.Neck neck : InsulatorBlock.Neck.values())
            be.getLinks().stream().filter(link -> InsulatorBlock.Neck.of(link.outgoing()) == neck).findFirst()
                    .ifPresent(link -> drawWrap(ms, buffer.getBuffer(RenderType.cutout()), facing, neck, link.type(),
                            light));
    }

    private static void drawWrap(PoseStack ms, VertexConsumer vc, Direction facing, InsulatorBlock.Neck neck,
            WireType type, int light) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TEXTURE);
        float r = (InsulatorBlock.NECK_HALF_WIDTH + WRAP_OVERHANG) / 16;
        float lo = (neck.bottom + WRAP_INSET) / 16, hi = (neck.top - WRAP_INSET) / 16;
        float a = .5f - r, b = .5f + r;
        int color = 0xFF000000 | type.light();
        ms.pushPose();
        ms.translate(.5, .5, .5);
        ms.mulPose(facing.getRotation());
        ms.translate(-.5, -.5, -.5);
        PoseStack.Pose pose = ms.last();
        quad(vc, pose, sprite, light, color, a, lo, a, b, lo, a, b, hi, a, a, hi, a);
        quad(vc, pose, sprite, light, color, b, lo, b, a, lo, b, a, hi, b, b, hi, b);
        quad(vc, pose, sprite, light, color, a, lo, b, a, lo, a, a, hi, a, a, hi, b);
        quad(vc, pose, sprite, light, color, b, lo, a, b, lo, b, b, hi, b, b, hi, a);
        quad(vc, pose, sprite, light, color, a, hi, a, b, hi, a, b, hi, b, a, hi, b);
        quad(vc, pose, sprite, light, color, a, lo, b, b, lo, b, b, lo, a, a, lo, a);
        ms.popPose();
    }

    private static void quad(VertexConsumer vc, PoseStack.Pose pose, TextureAtlasSprite sprite, int light, int color,
            float... c) {
        float[][] uv = { { sprite.getU0(), sprite.getV0() }, { sprite.getU1(), sprite.getV0() },
                { sprite.getU1(), sprite.getV1() }, { sprite.getU0(), sprite.getV1() } };
        for (int i = 0; i < 4; i++)
            vertex(vc, pose, new Vec3(c[i * 3], c[i * 3 + 1], c[i * 3 + 2]), color, uv[i], light);
        for (int i = 3; i >= 0; i--)
            vertex(vc, pose, new Vec3(c[i * 3], c[i * 3 + 1], c[i * 3 + 2]), color, uv[i], light);
    }

    private static void drawWire(PoseStack ms, VertexConsumer vc, Vec3 from, Vec3 to, int lightFrom, int lightTo,
            WireType type) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TEXTURE);
        double sag = from.distanceTo(to) * SAG_PER_BLOCK;
        Vec3 horizontal = new Vec3(to.x - from.x, 0, to.z - from.z);
        Vec3 side = horizontal.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0)
                : new Vec3(-horizontal.z, 0, horizontal.x).normalize();
        Vec3 up = new Vec3(0, 1, 0);
        PoseStack.Pose pose = ms.last();
        Vec3 prev = point(from, to, sag, 0);
        for (int i = 1; i <= SEGMENTS; i++) {
            float t0 = (i - 1) / (float) SEGMENTS, t1 = i / (float) SEGMENTS;
            Vec3 next = point(from, to, sag, t1);
            int color = 0xFF000000 | type.light();
            int l0 = lerpLight(lightFrom, lightTo, t0), l1 = lerpLight(lightFrom, lightTo, t1);
            ribbon(vc, pose, sprite, prev, next, side.scale(THICKNESS), color, l0, l1);
            ribbon(vc, pose, sprite, prev, next, up.scale(THICKNESS), color, l0, l1);
            prev = next;
        }
    }

    private static Vec3 point(Vec3 from, Vec3 to, double sag, float t) {
        return from.lerp(to, t).add(0, -sag * 4 * t * (1 - t), 0);
    }

    private static int lerpLight(int a, int b, float t) {
        int block = Mth.lerpInt(t, LightTexture.block(a), LightTexture.block(b));
        int sky = Mth.lerpInt(t, LightTexture.sky(a), LightTexture.sky(b));
        return LightTexture.pack(block, sky);
    }

    private static void ribbon(VertexConsumer vc, PoseStack.Pose pose, TextureAtlasSprite sprite, Vec3 a, Vec3 b,
            Vec3 offset,
            int color, int lightA, int lightB) {
        Vec3[] corners = { a.subtract(offset), a.add(offset), b.add(offset), b.subtract(offset) };
        int[] lights = { lightA, lightA, lightB, lightB };
        float[][] uv = { { sprite.getU0(), sprite.getV0() }, { sprite.getU1(), sprite.getV0() },
                { sprite.getU1(), sprite.getV1() }, { sprite.getU0(), sprite.getV1() } };
        for (int i = 0; i < 4; i++)
            vertex(vc, pose, corners[i], color, uv[i], lights[i]);
        for (int i = 3; i >= 0; i--)
            vertex(vc, pose, corners[i], color, uv[i], lights[i]);
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose pose, Vec3 p, int color, float[] uv, int light) {
        vc.addVertex(pose, (float) p.x, (float) p.y, (float) p.z).setColor(color).setUv(uv[0], uv[1]).setLight(light)
                .setNormal(pose, 0, 1, 0);
    }

    @Override
    public AABB getRenderBoundingBox(InsulatorBlockEntity be) {
        AABB box = new AABB(be.getBlockPos());
        for (InsulatorBlockEntity.Link link : be.getLinks())
            box = box.minmax(new AABB(link.other()));
        return box;
    }
}
