package net.nullmods.createendergy.content.wire;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.nullmods.createendergy.EndergyItems;

import java.util.function.Supplier;

public enum WireType implements StringRepresentable {
    COPPER(0xE77C56, 0x9C4E31, () -> EndergyItems.COPPER_WIRE.get()),
    GOLD(0xFAD64A, 0xDC9613, () -> EndergyItems.GOLD_WIRE.get()),
    ENDERGETIC(0x349988, 0x197162, () -> EndergyItems.ENDERGETIC_WIRE.get()),
    INSULATED_COPPER(0x363636, 0x303030, () -> EndergyItems.INSULATED_COPPER_WIRE.get()),
    INSULATED_GOLD(0x363636, 0x303030, () -> EndergyItems.INSULATED_GOLD_WIRE.get()),
    INSULATED_ENDERGETIC(0x363636, 0x303030, () -> EndergyItems.INSULATED_ENDERGETIC_WIRE.get());

    private final int light;
    private final int dark;
    private final Supplier<Item> item;

    WireType(int light, int dark, Supplier<Item> item) {
        this.light = light;
        this.dark = dark;
        this.item = item;
    }

    public int light() {
        return light;
    }

    public int dark() {
        return dark;
    }

    public boolean insulated() {
        return name().startsWith("INSULATED_");
    }

    public Item item() {
        return item.get();
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }

    public static WireType byName(String name) {
        for (WireType type : values())
            if (type.getSerializedName().equals(name))
                return type;
        return COPPER;
    }
}
