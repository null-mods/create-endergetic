package net.nullmods.createendergy;

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.decoration.encasing.CasingBlock;
import com.simibubi.create.foundation.data.BuilderTransformers;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;
import net.nullmods.createendergy.content.magnet.MagnetBlock;
import net.nullmods.createendergy.content.motor.EndergyMotorBlock;
import net.nullmods.createendergy.content.wire.InsulatorBlock;
import net.nullmods.createendergy.content.magnetiser.MagnetiserBlock;

public class EndergyBlocks {
        private static final CreateRegistrate REGISTRATE = CreateEndergy.REGISTRATE;

        public static final BlockEntry<CasingBlock> ENDERGETIC_CASING = REGISTRATE
                        .block("endergetic_casing", CasingBlock::new)
                        .properties(p -> p.mapColor(MapColor.COLOR_CYAN))
                        .transform(BuilderTransformers.casing(() -> EndergySpriteShifts.ENDERGETIC_CASING))
                        .register();

        public static final BlockEntry<MagnetiserBlock> MAGNETISER = REGISTRATE
                        .block("magnetiser", MagnetiserBlock::new)
                        .initialProperties(SharedProperties::stone)
                        .properties(p -> p.noOcclusion().mapColor(MapColor.COLOR_CYAN))
                        .onRegister(block -> BlockStressValues.IMPACTS.register(block, () -> 8.0))
                        .simpleItem()
                        .register();

        public static final BlockEntry<MagnetBlock> MAGNET_BLOCK = REGISTRATE
                        .block("magnet_block", MagnetBlock::new)
                        .initialProperties(() -> Blocks.IRON_BLOCK)
                        .properties(p -> p.mapColor(MapColor.COLOR_PURPLE))
                        .simpleItem()
                        .register();

        public static final BlockEntry<Block> ENDERGETIC_ALLOY_BLOCK = REGISTRATE
                        .block("endergetic_alloy_block", Block::new)
                        .initialProperties(() -> Blocks.IRON_BLOCK)
                        .properties(p -> p.mapColor(MapColor.COLOR_CYAN))
                        .simpleItem()
                        .register();

        public static final BlockEntry<EndergyMotorBlock> COPPER_MOTOR = REGISTRATE
                        .block("copper_motor", EndergyMotorBlock::new)
                        .initialProperties(SharedProperties::stone)
                        .properties(p -> p.mapColor(MapColor.COLOR_CYAN).forceSolidOn())
                        .onRegister(block -> BlockStressValues.CAPACITIES.register(block, () -> 16384.0))
                        .onRegister(BlockStressValues.setGeneratorSpeed(256, true))
                        .simpleItem()
                        .register();

        public static final BlockEntry<EndergyMotorBlock> ENDERGETIC_MOTOR = REGISTRATE
                        .block("endergetic_motor", EndergyMotorBlock::new)
                        .initialProperties(SharedProperties::stone)
                        .properties(p -> p.mapColor(MapColor.COLOR_CYAN).forceSolidOn())
                        .onRegister(block -> BlockStressValues.CAPACITIES.register(block, () -> 16384.0))
                        .onRegister(BlockStressValues.setGeneratorSpeed(256, true))
                        .simpleItem()
                        .register();

        public static final BlockEntry<InsulatorBlock> INSULATOR = REGISTRATE
                        .block("insulator", InsulatorBlock::new)
                        .initialProperties(() -> Blocks.LIGHTNING_ROD)
                        .properties(p -> p.noOcclusion())
                        .simpleItem()
                        .register();

        public static void register() {
        }
}
