package net.nullmods.createendergy.content.magnet;

import net.minecraft.util.StringRepresentable;

/** A normal two-pole magnet, or one turned by the wrench into a single pole on every face. */
public enum MagnetPolarity implements StringRepresentable {
    DIPOLE, POSITIVE, NEGATIVE;

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
}
