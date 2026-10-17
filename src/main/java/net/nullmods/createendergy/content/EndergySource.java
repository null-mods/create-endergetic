package net.nullmods.createendergy.content;

import net.minecraft.core.BlockPos;

public interface EndergySource {
    int getVoltage();

    BlockPos getSourcePos();
}
