package net.nullmods.createendergy;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;

public class EndergySpriteShifts {
        public static final CTSpriteShiftEntry ENDERGETIC_CASING = CTSpriteShifter.getCT(AllCTTypes.OMNIDIRECTIONAL,
                        CreateEndergy.asResource("block/endergetic_casing"),
                        CreateEndergy.asResource("block/endergetic_casing_connected"));

        public static final CTSpriteShiftEntry ACCUMULATOR = rectangle("accumulator"),
                        ACCUMULATOR_TOP = rectangle("accumulator_top"),
                        ACCUMULATOR_TOP_POWERED = rectangle("accumulator_top_powered",
                                        "accumulator_top_connected_powered"),
                        ACCUMULATOR_INNER = rectangle("accumulator_inner"),
                        CREATIVE_ACCUMULATOR = rectangle("creative_accumulator"),
                        CREATIVE_ACCUMULATOR_TOP = rectangle("creative_accumulator_top"),
                        CREATIVE_ACCUMULATOR_TOP_POWERED = rectangle("creative_accumulator_top_powered",
                                        "creative_accumulator_top_connected_powered"),
                        CREATIVE_ACCUMULATOR_INNER = rectangle("creative_accumulator_inner");

        private static CTSpriteShiftEntry rectangle(String name) {
                return rectangle(name, name + "_connected");
        }

        private static CTSpriteShiftEntry rectangle(String name, String connected) {
                return CTSpriteShifter.getCT(AllCTTypes.RECTANGLE, CreateEndergy.asResource("block/" + name),
                                CreateEndergy.asResource("block/" + connected));
        }
}
