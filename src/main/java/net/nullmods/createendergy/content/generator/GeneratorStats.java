package net.nullmods.createendergy.content.generator;

import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.nullmods.createendergy.EndergyBlocks;
import net.nullmods.createendergy.content.magnet.MagnetBlock;
import net.nullmods.createendergy.content.magnet.MagnetPolarity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record GeneratorStats(int activeBlocks, int rotorBlocks, float coverage, float radius, float powerPerRpm,
        float weightPerRpm, int maxSafeSpeed) {
    private static final int MAX_SPEED = 256;
    public static final GeneratorStats EMPTY = new GeneratorStats(0, 0, 0, 0, 0, 0, MAX_SPEED);
    private static final float VOLTS_PER_RPM_PER_BLOCK = 1.5f;
    private static final float COPPER_SU_PER_RPM = 4;
    private static final float BLOCK_WEIGHT = .5f;
    private static final float MAGNET_WEIGHT = 1;
    private static final float EFFICIENCY = .7f;

    private enum Kind {
        NONE, COIL, MAGNET
    }

    private record RotorBlock(Kind kind, double radius) {
    }

    private record Ring(float magnetCoverage, float coilCoverage) {
    }

    public float voltage(float speed) {
        return VOLTS_PER_RPM_PER_BLOCK * Math.abs(speed) * radius;
    }

    public float power(float speed) {
        return powerPerRpm * Math.abs(speed) * EFFICIENCY;
    }

    public static GeneratorStats analyse(Level level, BlockPos bearing, Direction facing, Contraption contraption) {
        Direction[] plane = planeOf(facing);
        BlockPos anchor = bearing.relative(facing);
        Map<Integer, List<RotorBlock>> layers = new HashMap<>();
        float weight = 0;
        for (Map.Entry<BlockPos, StructureBlockInfo> entry : contraption.getBlocks().entrySet()) {
            BlockPos local = entry.getKey();
            Kind kind = kindOf(entry.getValue().state());
            weight += kind == Kind.MAGNET ? MAGNET_WEIGHT : BLOCK_WEIGHT;
            int a = component(local, plane[0]), b = component(local, plane[1]);
            layers.computeIfAbsent(component(local, facing), d -> new ArrayList<>())
                    .add(new RotorBlock(kind, Math.sqrt(a * a + b * b)));
        }

        int active = 0, maxRadius = 0;
        float coverageSum = 0, radiusSum = 0;
        for (Map.Entry<Integer, List<RotorBlock>> layer : layers.entrySet()) {
            int rim = (int) Math.round(layer.getValue().stream().mapToDouble(RotorBlock::radius).max().orElse(0));
            if (rim == 0)
                continue;
            maxRadius = Math.max(maxRadius, rim);
            Ring ring = scanRing(level, anchor.relative(facing, layer.getKey()), plane, rim);
            for (RotorBlock block : layer.getValue()) {
                if (block.kind() == Kind.NONE || Math.round(block.radius()) != rim)
                    continue;
                float coverage = block.kind() == Kind.COIL ? ring.magnetCoverage() : ring.coilCoverage();
                if (coverage <= 0)
                    continue;
                active++;
                coverageSum += coverage;
                radiusSum += (float) block.radius() * coverage;
            }
        }

        int maxSafe = maxRadius == 0 ? MAX_SPEED : Math.min(MAX_SPEED, (int) (362 / Math.sqrt(maxRadius)));
        if (active == 0)
            return new GeneratorStats(0, contraption.getBlocks().size(), 0, 0, 0, weight, maxSafe);
        return new GeneratorStats(active, contraption.getBlocks().size(), coverageSum / active, radiusSum / coverageSum,
                COPPER_SU_PER_RPM * coverageSum, weight, maxSafe);
    }

    // The ring may touch the rim or leave one block of air; each candidate band is scored on its own and the
    // fuller one wins, since a ring split between both bands has no clear angular order to check polarity
    private static Ring scanRing(Level level, BlockPos centre, Direction[] plane, int rim) {
        Ring touching = scanBand(level, centre, plane, rim + 1);
        Ring gapped = scanBand(level, centre, plane, rim + 2);
        return new Ring(Math.max(touching.magnetCoverage(), gapped.magnetCoverage()),
                Math.max(touching.coilCoverage(), gapped.coilCoverage()));
    }

    private static Ring scanBand(Level level, BlockPos centre, Direction[] plane, int radius) {
        int reach = radius + 1;
        List<int[]> cells = new ArrayList<>();
        for (int a = -reach; a <= reach; a++)
            for (int b = -reach; b <= reach; b++) {
                double distance = Math.sqrt(a * a + b * b);
                if (distance > radius - .5 && distance <= radius + .5)
                    cells.add(new int[] { a, b });
            }
        cells.sort(Comparator.comparingDouble(cell -> Math.atan2(cell[1], cell[0])));

        int[] poles = new int[cells.size()];
        int coils = 0;
        for (int i = 0; i < cells.size(); i++) {
            int a = cells.get(i)[0], b = cells.get(i)[1];
            BlockState state = level.getBlockState(centre.relative(plane[0], a).relative(plane[1], b));
            Kind kind = kindOf(state);
            if (kind == Kind.COIL)
                coils++;
            else if (kind == Kind.MAGNET)
                poles[i] = poleTowardsAxis(state, inwards(a, b, plane));
        }

        int alternating = 0;
        for (int i = 0; i < poles.length; i++) {
            int previous = poles[(i + poles.length - 1) % poles.length];
            if (poles[i] != 0 && previous == -poles[i])
                alternating++;
        }
        return new Ring(alternating / (float) cells.size(), coils / (float) cells.size());
    }

    private static int poleTowardsAxis(BlockState state, Direction inwards) {
        MagnetPolarity polarity = state.getValue(MagnetBlock.POLARITY);
        if (polarity == MagnetPolarity.POSITIVE)
            return 1;
        if (polarity == MagnetPolarity.NEGATIVE)
            return -1;
        Direction positiveFace = state.getValue(MagnetBlock.FACING);
        return positiveFace == inwards ? 1 : positiveFace == inwards.getOpposite() ? -1 : 0;
    }

    private static Direction inwards(int a, int b, Direction[] plane) {
        if (Math.abs(a) >= Math.abs(b))
            return a > 0 ? plane[0].getOpposite() : plane[0];
        return b > 0 ? plane[1].getOpposite() : plane[1];
    }

    private static Kind kindOf(BlockState state) {
        if (state.is(EndergyBlocks.MAGNET_BLOCK.get()))
            return Kind.MAGNET;
        if (state.is(EndergyBlocks.COPPER_COIL.get()))
            return Kind.COIL;
        return Kind.NONE;
    }

    private static Direction[] planeOf(Direction facing) {
        Direction[] plane = new Direction[2];
        int i = 0;
        for (Direction.Axis axis : Direction.Axis.values())
            if (axis != facing.getAxis())
                plane[i++] = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
        return plane;
    }

    private static int component(BlockPos pos, Direction direction) {
        return pos.getX() * direction.getStepX() + pos.getY() * direction.getStepY() + pos.getZ() * direction.getStepZ();
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Active", activeBlocks);
        tag.putInt("Rotor", rotorBlocks);
        tag.putFloat("Coverage", coverage);
        tag.putFloat("Radius", radius);
        tag.putFloat("Power", powerPerRpm);
        tag.putFloat("Weight", weightPerRpm);
        tag.putInt("MaxSpeed", maxSafeSpeed);
        return tag;
    }

    public static GeneratorStats read(CompoundTag tag) {
        if (tag.isEmpty())
            return EMPTY;
        return new GeneratorStats(tag.getInt("Active"), tag.getInt("Rotor"), tag.getFloat("Coverage"),
                tag.getFloat("Radius"), tag.getFloat("Power"), tag.getFloat("Weight"), tag.getInt("MaxSpeed"));
    }
}
