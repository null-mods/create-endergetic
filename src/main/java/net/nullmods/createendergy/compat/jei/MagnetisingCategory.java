package net.nullmods.createendergy.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.nullmods.createendergy.CreateEndergy;
import net.nullmods.createendergy.EndergyBlocks;
import net.nullmods.createendergy.content.magnetiser.MagnetisingRecipe;

import java.util.List;

/** Laid out like Create's own pressing category, with the magnetiser in place of the press. */
public class MagnetisingCategory implements IRecipeCategory<RecipeHolder<MagnetisingRecipe>> {
    public static final RecipeType<RecipeHolder<MagnetisingRecipe>> TYPE =
            RecipeType.createRecipeHolderType(CreateEndergy.asResource("magnetising"));

    private static final int WIDTH = 177, HEIGHT = 70;
    /** The bolt drawn between the machine and the item, in the alloy's palette. */
    private static final int BOLT_GLOW = 0xFF2CCDB1, BOLT_CORE = 0xFF8CF4E2;

    private final IDrawable icon;

    public MagnetisingCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(EndergyBlocks.MAGNETISER.asStack());
    }

    @Override
    public RecipeType<RecipeHolder<MagnetisingRecipe>> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("create_endergy.recipe.magnetising");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<MagnetisingRecipe> holder, IFocusGroup focuses) {
        MagnetisingRecipe recipe = holder.value();
        builder.addSlot(RecipeIngredientRole.INPUT, 27, 51)
                .setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
                .addIngredients(recipe.getIngredients().get(0));

        List<ProcessingOutput> results = recipe.getRollableResults();
        for (int i = 0; i < results.size(); i++) {
            ProcessingOutput output = results.get(i);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 131 + 19 * i, 50)
                    .setBackground(CreateRecipeCategory.getRenderedSlot(output), -1, -1)
                    .addItemStack(output.getStack())
                    .addRichTooltipCallback(CreateRecipeCategory.addStochasticTooltip(output));
        }
    }

    @Override
    public void draw(RecipeHolder<MagnetisingRecipe> recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 61, 41);
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 52, 54);

        // The machine at twice item size, above the shadow
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(72, 4, 0);
        pose.scale(2, 2, 2);
        graphics.renderItem(EndergyBlocks.MAGNETISER.asStack(), 0, 0);
        pose.popPose();

        // A small zigzag bolt from the conductors down to where the item would be
        int[][] bolt = {{87, 36}, {89, 39}, {86, 42}, {89, 45}};
        for (int[] step : bolt) {
            graphics.fill(step[0] - 1, step[1] - 1, step[0] + 3, step[1] + 4, BOLT_GLOW);
            graphics.fill(step[0], step[1], step[0] + 2, step[1] + 3, BOLT_CORE);
        }
    }
}
