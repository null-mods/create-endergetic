package net.nullmods.createendergy.content.generator;

import com.simibubi.create.content.contraptions.bearing.MechanicalBearingBlock;
import com.simibubi.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.nullmods.createendergy.EndergyBlockEntityTypes;

public class GeneratorBearingBlock extends MechanicalBearingBlock {
    public GeneratorBearingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends MechanicalBearingBlockEntity> getBlockEntityType() {
        return EndergyBlockEntityTypes.GENERATOR_BEARING.get();
    }
}
