package net.nullmods.createendergy.content.magnetiser;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.nullmods.createendergy.EndergyRecipeTypes;

public class MagnetisingRecipe extends StandardProcessingRecipe<SingleRecipeInput> {
    public MagnetisingRecipe(ProcessingRecipeParams params) {
        super(EndergyRecipeTypes.MAGNETISING, params);
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return !input.isEmpty() && ingredients.get(0).test(input.getItem(0));
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 2;
    }
}
