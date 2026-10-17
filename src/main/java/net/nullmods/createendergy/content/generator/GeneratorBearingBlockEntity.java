package net.nullmods.createendergy.content.generator;

import com.simibubi.create.content.contraptions.bearing.BearingBlock;
import com.simibubi.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.nullmods.createendergy.content.EndergyGoggles;
import net.nullmods.createendergy.content.EndergySource;

import java.util.List;

public class GeneratorBearingBlockEntity extends MechanicalBearingBlockEntity implements EndergySource {
    private static final int ANALYSIS_INTERVAL = 20;

    private GeneratorStats stats = GeneratorStats.EMPTY;
    private int analysisCooldown;

    public GeneratorBearingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide)
            return;
        if (!running || movedContraption == null) {
            setStats(GeneratorStats.EMPTY);
            return;
        }
        if (--analysisCooldown <= 0) {
            analysisCooldown = ANALYSIS_INTERVAL;
            setStats(GeneratorStats.analyse(level, worldPosition, getBlockState().getValue(BearingBlock.FACING),
                    movedContraption.getContraption()));
        }
        if (Math.abs(getSpeed()) > stats.maxSafeSpeed())
            disassemble();
    }

    @Override
    public void assemble() {
        super.assemble();
        analysisCooldown = 0;
    }

    private void setStats(GeneratorStats newStats) {
        if (newStats.equals(stats))
            return;
        stats = newStats;
        if (hasNetwork())
            getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
        sendData();
    }

    @Override
    public float calculateStressApplied() {
        float impact = super.calculateStressApplied() + stats.powerPerRpm() + stats.weightPerRpm();
        lastStressApplied = impact;
        return impact;
    }

    @Override
    public int getVoltage() {
        return Math.round(stats.voltage(getSpeed()));
    }

    @Override
    public BlockPos getSourcePos() {
        return worldPosition;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        EndergyGoggles.header(tooltip);
        EndergyGoggles.voltage(tooltip, getVoltage());
        EndergyGoggles.generator(tooltip, Math.round(stats.power(getSpeed())), stats.activeBlocks(),
                stats.rotorBlocks(), Math.round(stats.coverage() * 100), stats.maxSafeSpeed());
        return true;
    }

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.put("Generator", stats.write());
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        stats = GeneratorStats.read(tag.getCompound("Generator"));
        super.read(tag, registries, clientPacket);
    }
}
