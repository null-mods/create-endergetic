package net.nullmods.createendergy.content.magnet;

import net.minecraft.util.StringRepresentable;

public enum MagnetPolarity implements StringRepresentable {
    DIPOLE, POSITIVE, NEGATIVE;

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
}
