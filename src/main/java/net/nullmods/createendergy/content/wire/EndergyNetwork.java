package net.nullmods.createendergy.content.wire;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.nullmods.createendergy.content.accumulator.AccumulatorBlockEntity;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public final class EndergyNetwork {
    private static final int MAX_NODES = 4096;

    private EndergyNetwork() {
    }

    public static int voltageAt(Level level, BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> sources = new HashSet<>();
        Deque<BlockPos> frontier = new ArrayDeque<>();
        visited.add(start);
        frontier.add(start);
        int voltage = 0;
        while (!frontier.isEmpty() && visited.size() <= MAX_NODES) {
            BlockPos pos = frontier.poll();
            if (!(level.getBlockEntity(pos) instanceof InsulatorBlockEntity insulator))
                continue;
            AccumulatorBlockEntity source = insulator.getSource();
            if (source != null && sources.add(source.getController()))
                voltage += source.getVoltage();
            for (InsulatorBlockEntity.Link link : insulator.getLinks())
                if (!link.outgoing() && visited.add(link.other()))
                    frontier.add(link.other());
        }
        return voltage;
    }
}
