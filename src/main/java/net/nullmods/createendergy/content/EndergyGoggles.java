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

    public static void generator(List<Component> tooltip, int power, int active, int rotor, int coverage, int maxSpeed) {
        line(tooltip, "gui.goggles.power", lang().translate("generic.unit.endergy_watts", power), ChatFormatting.AQUA);
        line(tooltip, "gui.goggles.rotor", lang().translate("gui.goggles.rotor_blocks", active, rotor),
                ChatFormatting.WHITE);
        line(tooltip, "gui.goggles.coverage", lang().text(coverage + "%"), ChatFormatting.WHITE);
        line(tooltip, "gui.goggles.max_speed", lang().translate("generic.unit.rpm", maxSpeed), ChatFormatting.WHITE);
    }

    private static void line(List<Component> tooltip, String label, LangBuilder value, ChatFormatting colour) {
        lang().translate(label).style(ChatFormatting.GRAY).forGoggles(tooltip);
        value.style(colour).forGoggles(tooltip, 1);
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
