package net.nullmods.createendergy;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.bearing.BearingRenderer;
import com.simibubi.create.content.contraptions.bearing.BearingVisual;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.kinetics.motor.CreativeMotorRenderer;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.nullmods.createendergy.content.accumulator.AccumulatorBlockEntity;
import net.nullmods.createendergy.content.accumulator.CreativeAccumulatorBlockEntity;
import net.nullmods.createendergy.content.generator.GeneratorBearingBlockEntity;
import net.nullmods.createendergy.content.magnetiser.MagnetiserBlockEntity;
import net.nullmods.createendergy.content.wire.InsulatorBlockEntity;
import net.nullmods.createendergy.content.wire.InsulatorRenderer;
import net.nullmods.createendergy.content.magnetiser.MagnetiserRenderer;

public class EndergyBlockEntityTypes {
        private static final CreateRegistrate REGISTRATE = CreateEndergy.REGISTRATE;

        public static final BlockEntityEntry<MagnetiserBlockEntity> MAGNETISER = REGISTRATE
                        .blockEntity("magnetiser", MagnetiserBlockEntity::new)
                        .validBlocks(EndergyBlocks.MAGNETISER)
                        .renderer(() -> MagnetiserRenderer::new)
                        .register();

        public static final BlockEntityEntry<CreativeMotorBlockEntity> MOTOR = REGISTRATE
                        .blockEntity("motor", CreativeMotorBlockEntity::new)
                        .visual(() -> OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF), false)
                        .validBlocks(EndergyBlocks.COPPER_MOTOR, EndergyBlocks.ENDERGETIC_MOTOR)
                        .renderer(() -> CreativeMotorRenderer::new)
                        .register();

        public static final BlockEntityEntry<InsulatorBlockEntity> INSULATOR = REGISTRATE
                        .blockEntity("insulator", InsulatorBlockEntity::new)
                        .validBlocks(EndergyBlocks.INSULATOR)
                        .renderer(() -> InsulatorRenderer::new)
                        .register();

        public static final BlockEntityEntry<AccumulatorBlockEntity> ACCUMULATOR = REGISTRATE
                        .blockEntity("accumulator", AccumulatorBlockEntity::new)
                        .validBlocks(EndergyBlocks.ACCUMULATOR)
                        .register();

        public static final BlockEntityEntry<CreativeAccumulatorBlockEntity> CREATIVE_ACCUMULATOR = REGISTRATE
                        .blockEntity("creative_accumulator", CreativeAccumulatorBlockEntity::new)
                        .validBlocks(EndergyBlocks.CREATIVE_ACCUMULATOR)
                        .register();

        public static final BlockEntityEntry<GeneratorBearingBlockEntity> GENERATOR_BEARING = REGISTRATE
                        .blockEntity("generator_bearing", GeneratorBearingBlockEntity::new)
                        .visual(() -> BearingVisual::new)
                        .validBlocks(EndergyBlocks.GENERATOR_BEARING)
                        .renderer(() -> BearingRenderer::new)
                        .register();

        public static void register() {
        }
}
