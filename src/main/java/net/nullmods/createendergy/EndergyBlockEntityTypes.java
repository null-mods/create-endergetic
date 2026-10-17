package net.nullmods.createendergy;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.nullmods.createendergy.content.magnetiser.MagnetiserBlockEntity;
import net.nullmods.createendergy.content.magnetiser.MagnetiserRenderer;

public class EndergyBlockEntityTypes {
    private static final CreateRegistrate REGISTRATE = CreateEndergy.REGISTRATE;

    public static final BlockEntityEntry<MagnetiserBlockEntity> MAGNETISER = REGISTRATE
            .blockEntity("magnetiser", MagnetiserBlockEntity::new)
            .validBlocks(EndergyBlocks.MAGNETISER)
            .renderer(() -> MagnetiserRenderer::new)
            .register();

    public static void register() {
    }
}
