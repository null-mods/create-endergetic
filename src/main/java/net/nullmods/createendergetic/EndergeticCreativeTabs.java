package net.nullmods.createendergetic;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EndergeticCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateEndergetic.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.create_endergetic"))
                    .icon(() -> EndergeticItems.ENDERGETIC_ALLOY.asStack())
                    .displayItems((params, output) -> {
                        output.accept(EndergeticItems.ENDERGETIC_ALLOY);
                        output.accept(EndergeticItems.ENDERGETIC_PRECISION_MECHANISM);
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}