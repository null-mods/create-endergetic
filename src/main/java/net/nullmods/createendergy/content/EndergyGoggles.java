package net.nullmods.createendergy.content;

import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.nullmods.createendergy.CreateEndergy;

import java.text.NumberFormat;
import java.util.List;

public final class EndergyGoggles {
    private EndergyGoggles() {
    }

    public static void header(List<Component> tooltip) {
        lang().translate("gui.goggles.endergy_stats").forGoggles(tooltip);
    }

    public static void voltage(List<Component> tooltip, int voltage) {
        lang().translate("gui.goggles.voltage").style(ChatFormatting.GRAY).forGoggles(tooltip);
        lang().translate("generic.unit.volts", voltage).style(ChatFormatting.AQUA).forGoggles(tooltip, 1);
    }

    public static void storage(List<Component> tooltip, long stored, long capacity) {
        energy(tooltip, "gui.goggles.stored", lang().translate("generic.unit.endergy", format(stored)));
        energy(tooltip, "gui.goggles.capacity", lang().translate("generic.unit.endergy", format(capacity)));
    }

    public static void infiniteStorage(List<Component> tooltip) {
        energy(tooltip, "gui.goggles.stored", lang().translate("gui.goggles.infinite"));
        energy(tooltip, "gui.goggles.capacity", lang().translate("gui.goggles.infinite"));
    }

    private static void energy(List<Component> tooltip, String label, LangBuilder value) {
        lang().translate(label).style(ChatFormatting.GRAY).forGoggles(tooltip);
        value.style(ChatFormatting.GOLD).forGoggles(tooltip, 1);
    }

    private static String format(long value) {
        return NumberFormat.getIntegerInstance().format(value);
    }

    private static LangBuilder lang() {
        return new LangBuilder(CreateEndergy.MOD_ID);
    }
}
