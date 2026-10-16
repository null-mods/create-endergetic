package net.nullmods.createendergy;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = CreateEndergy.MOD_ID, dist = Dist.CLIENT)
public class CreateEndergyClient {
    public CreateEndergyClient(IEventBus modEventBus) {
        EndergyPartialModels.init();
    }
}
