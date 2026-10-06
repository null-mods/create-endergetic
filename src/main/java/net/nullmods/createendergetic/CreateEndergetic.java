package net.nullmods.createendergetic;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CreateEndergetic.MOD_ID)
public class CreateEndergetic {
    public static final String MOD_ID = "create_endergetic";
    public static final String NAME = "Create: Endergetic";
    public static final Logger LOGGER = LogUtils.getLogger();


    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
            .defaultCreativeTab((ResourceKey<CreativeModeTab>) null);

    public CreateEndergetic(IEventBus modEventBus) {
        EndergeticItems.register();

        EndergeticCreativeTabs.register(modEventBus);

        REGISTRATE.registerEventListeners(modEventBus);
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
