package net.nullmods.createendergetic;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;

public class EndergeticSpriteShifts {
    public static final CTSpriteShiftEntry ENDERGETIC_CASING = CTSpriteShifter.getCT(AllCTTypes.OMNIDIRECTIONAL,
            CreateEndergetic.asResource("block/endergetic_casing"),
            CreateEndergetic.asResource("block/endergetic_casing_connected"));
}
