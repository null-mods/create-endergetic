package net.nullmods.createendergy;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CreateEndergy.MOD_ID)
public class CreateEndergy {
    public static final String MOD_ID = "create_endergy";
    public static final String NAME = "Create: Endergy";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
            .defaultCreativeTab((ResourceKey<CreativeModeTab>) null);

    public CreateEndergy(IEventBus modEventBus) {
        EndergyItems.register();
        EndergyBlocks.register();

        EndergyCreativeTabs.register(modEventBus);

        REGISTRATE.registerEventListeners(modEventBus);
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
