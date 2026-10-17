package net.nullmods.createendergy.content.motor;

import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.nullmods.createendergy.EndergyBlockEntityTypes;

public class EndergyMotorBlock extends CreativeMotorBlock {
    public EndergyMotorBlock(Properties properties) {
        super(properties);
    }

    // Create's block entity type only accepts Create's own motor block
    @Override
    public BlockEntityType<? extends CreativeMotorBlockEntity> getBlockEntityType() {
        return EndergyBlockEntityTypes.MOTOR.get();
    }
}
