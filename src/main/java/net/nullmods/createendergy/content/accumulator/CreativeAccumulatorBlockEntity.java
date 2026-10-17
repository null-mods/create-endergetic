package net.nullmods.createendergy.content.accumulator;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CreativeAccumulatorBlockEntity extends AccumulatorBlockEntity {
    public static final int DEFAULT_VOLTAGE = 128;
    public static final int MAX_VOLTAGE = 2048;

    private ScrollValueBehaviour voltage;

    public CreativeAccumulatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        voltage = new VoltageScrollValueBehaviour(Component.translatable("create_endergy.accumulator.voltage"), this,
                new VoltageValueBox());
        voltage.between(0, MAX_VOLTAGE);
        voltage.value = DEFAULT_VOLTAGE;
        voltage.onlyActiveWhen(this::isController);
        behaviours.add(voltage);
    }

    @Override
    public int getVoltage() {
        return voltage.getValue();
    }

    @Override
    public boolean isInfinite() {
        return true;
    }

    // The settings board draws one notch per value, so 0-2048 V is shown as 256 notches of 8 V to fit the screen
    private static class VoltageScrollValueBehaviour extends ScrollValueBehaviour {
        private static final int STEP = 8;

        VoltageScrollValueBehaviour(Component label, SmartBlockEntity be, ValueBoxTransform slot) {
            super(label, be, slot);
        }

        @Override
        public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
            return new ValueSettingsBoard(label, MAX_VOLTAGE / STEP, 16, ImmutableList.of(Component.literal("V")),
                    new ValueSettingsFormatter(settings -> Component.literal(settings.value() * STEP + " V")));
        }

        @Override
        public void setValueSettings(Player player, ValueSettings settings, boolean ctrlDown) {
            if (settings.equals(getValueSettings()))
                return;
            setValue(settings.value() * STEP);
            playFeedbackSound(this);
        }

        @Override
        public ValueSettings getValueSettings() {
            return new ValueSettings(0, Math.round(value / (float) STEP));
        }
    }

    private static class VoltageValueBox extends ValueBoxTransform.Sided {
        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 16.05f);
        }
    }
}
