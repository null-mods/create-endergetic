package net.nullmods.createendergy.content.accumulator;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.createmod.catnip.data.Iterate;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.nullmods.createendergy.EndergySpriteShifts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccumulatorModel extends CTModel {
    private static final ModelProperty<boolean[]> CULLED = new ModelProperty<>();

    public static NonNullFunction<BakedModel, AccumulatorModel> standard() {
        ConnectedTextureBehaviour behaviour = new AccumulatorCTBehaviour(EndergySpriteShifts.ACCUMULATOR,
                EndergySpriteShifts.ACCUMULATOR_TOP, EndergySpriteShifts.ACCUMULATOR_TOP_POWERED,
                EndergySpriteShifts.ACCUMULATOR_INNER);
        return model -> new AccumulatorModel(model, behaviour);
    }

    public static NonNullFunction<BakedModel, AccumulatorModel> creative() {
        ConnectedTextureBehaviour behaviour = new AccumulatorCTBehaviour(EndergySpriteShifts.CREATIVE_ACCUMULATOR,
                EndergySpriteShifts.CREATIVE_ACCUMULATOR_TOP, EndergySpriteShifts.CREATIVE_ACCUMULATOR_TOP_POWERED,
                EndergySpriteShifts.CREATIVE_ACCUMULATOR_INNER);
        return model -> new AccumulatorModel(model, behaviour);
    }

    private AccumulatorModel(BakedModel originalModel, ConnectedTextureBehaviour behaviour) {
        super(originalModel, behaviour);
    }

    @Override
    protected ModelData.Builder gatherModelData(ModelData.Builder builder, BlockAndTintGetter world, BlockPos pos,
            BlockState state, ModelData blockEntityData) {
        super.gatherModelData(builder, world, pos, state, blockEntityData);
        boolean[] culled = new boolean[4];
        for (Direction d : Iterate.horizontalDirections)
            culled[d.get2DDataValue()] = ConnectivityHandler.isConnected(world, pos, pos.relative(d));
        return builder.with(CULLED, culled);
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData,
            RenderType renderType) {
        if (side != null)
            return Collections.emptyList();
        boolean[] culled = extraData.get(CULLED);
        List<BakedQuad> quads = new ArrayList<>();
        for (Direction d : Iterate.directions) {
            if (culled != null && d.getAxis().isHorizontal() && culled[d.get2DDataValue()])
                continue;
            quads.addAll(super.getQuads(state, d, rand, extraData, renderType));
        }
        quads.addAll(super.getQuads(state, null, rand, extraData, renderType));
        return quads;
    }
}
