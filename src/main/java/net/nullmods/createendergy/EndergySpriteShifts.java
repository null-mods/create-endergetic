package net.nullmods.createendergy;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;

public class EndergySpriteShifts {
    public static final CTSpriteShiftEntry ENDERGETIC_CASING = CTSpriteShifter.getCT(AllCTTypes.OMNIDIRECTIONAL,
            CreateEndergy.asResource("block/endergetic_casing"),
            CreateEndergy.asResource("block/endergetic_casing_connected"));
}
