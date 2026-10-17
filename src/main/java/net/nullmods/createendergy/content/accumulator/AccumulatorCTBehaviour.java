package net.nullmods.createendergy.content.accumulator;

import com.simibubi.create.content.fluids.tank.FluidTankCTBehaviour;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class AccumulatorCTBehaviour extends FluidTankCTBehaviour {
    private final CTSpriteShiftEntry poweredTop;

    public AccumulatorCTBehaviour(CTSpriteShiftEntry side, CTSpriteShiftEntry top, CTSpriteShiftEntry poweredTop,
            CTSpriteShiftEntry inner) {
        super(side, top, inner);
        this.poweredTop = poweredTop;
    }

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, TextureAtlasSprite sprite) {
        if (sprite != null && direction.getAxis().isVertical() && sprite == poweredTop.getOriginal())
            return poweredTop;
        return super.getShift(state, direction, sprite);
    }
}
