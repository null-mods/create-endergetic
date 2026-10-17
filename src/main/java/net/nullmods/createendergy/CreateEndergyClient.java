package net.nullmods.createendergy;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.nullmods.createendergy.content.magnet.MagnetBlock;
import net.nullmods.createendergy.content.magnet.MagnetPolarity;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = CreateEndergy.MOD_ID, dist = Dist.CLIENT)
public class CreateEndergyClient {
    public CreateEndergyClient(IEventBus modEventBus) {
        EndergyPartialModels.init();
        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> event
                .registerEntityRenderer(EndergyEntityTypes.MOVING_MAGNET.get(), FallingBlockRenderer::new));
        // Values must match the overrides in models/item/magnet_block.json
        modEventBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(
                () -> ItemProperties.register(EndergyBlocks.MAGNET_BLOCK.asItem(), CreateEndergy.asResource("polarity"),
                        (stack, level, entity, seed) -> switch (MagnetBlock.polarityOf(stack)) {
                            case DIPOLE -> 0;
                            case POSITIVE -> .5f;
                            case NEGATIVE -> 1;
                        })));
        NeoForge.EVENT_BUS.addListener(CreateEndergyClient::addPolarityTooltip);
    }

    private static void addPolarityTooltip(ItemTooltipEvent event) {
        if (!EndergyBlocks.MAGNET_BLOCK.isIn(event.getItemStack()))
            return;
        MagnetPolarity polarity = MagnetBlock.polarityOf(event.getItemStack());
        event.getToolTip().add(1, Component.translatable("create_endergy.magnet_block.polarity",
                Component.translatable("create_endergy.magnet_block.polarity." + polarity.getSerializedName()))
                .withStyle(ChatFormatting.GRAY));
    }
}
