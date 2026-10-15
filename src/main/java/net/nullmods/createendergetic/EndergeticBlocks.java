package net.nullmods.createendergetic;

import com.simibubi.create.content.decoration.encasing.CasingBlock;
import com.simibubi.create.foundation.data.BuilderTransformers;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.material.MapColor;

public class EndergeticBlocks {
    private static final CreateRegistrate REGISTRATE = CreateEndergetic.REGISTRATE;

    public static final BlockEntry<CasingBlock> ENDERGETIC_CASING = REGISTRATE
            .block("endergetic_casing", CasingBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_CYAN))
            .transform(BuilderTransformers.casing(() -> EndergeticSpriteShifts.ENDERGETIC_CASING))
            .register();

    public static void register() {
    }
}
