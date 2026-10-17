package net.nullmods.createendergy;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EndergyCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,
            CreateEndergy.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.create_endergy"))
                    .icon(() -> EndergyItems.ENDERGETIC_ALLOY.asStack())
                    .displayItems((params, output) -> {
                        output.accept(EndergyItems.ENDERGETIC_ALLOY);
                        output.accept(EndergyItems.ENDERGETIC_PRECISION_MECHANISM);
                        output.accept(EndergyItems.INSULATED_WRENCH);
                        output.accept(EndergyBlocks.ENDERGETIC_CASING);
                        output.accept(EndergyBlocks.MAGNETISER);
                        output.accept(EndergyItems.MAGNET);
                        output.accept(EndergyItems.ENDERGETIC_PLATE);
                        output.accept(EndergyItems.COPPER_COIL);
                        output.accept(EndergyItems.GOLD_COIL);
                        output.accept(EndergyItems.ENDERGETIC_COIL);
                        output.accept(EndergyBlocks.MAGNET_BLOCK);
                        output.accept(EndergyBlocks.ENDERGETIC_ALLOY_BLOCK);
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}