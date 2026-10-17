package net.nullmods.createendergy.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.nullmods.createendergy.CreateEndergy;
import net.nullmods.createendergy.EndergyBlocks;
import net.nullmods.createendergy.EndergyRecipeTypes;
import net.nullmods.createendergy.content.magnet.MagnetisedEnchanting;
import net.nullmods.createendergy.content.magnetiser.MagnetisingRecipe;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class EndergyJEI implements IModPlugin {
    private static final ResourceLocation ID = CreateEndergy.asResource("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new MagnetisingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level == null)
            return;
        List<RecipeHolder<MagnetisingRecipe>> recipes = new ArrayList<>(level.getRecipeManager()
                .getAllRecipesFor(EndergyRecipeTypes.MAGNETISING.<SingleRecipeInput, MagnetisingRecipe>getType()));
        recipes.addAll(enchantingExamples(level.registryAccess()));
        registration.addRecipes(MagnetisingCategory.TYPE, recipes);
    }

    private static final TagKey<Item> MAGNETISABLE = TagKey.create(Registries.ITEM, CreateEndergy.asResource("enchantable/magnetised"));

    private static List<RecipeHolder<MagnetisingRecipe>> enchantingExamples(RegistryAccess registries) {
        List<RecipeHolder<MagnetisingRecipe>> examples = new ArrayList<>();
        List<Item> inputs = new ArrayList<>(List.of(Items.BOOK));
        for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(MAGNETISABLE))
            inputs.add(item.value());
        for (Item input : inputs) {
            ItemStack result = MagnetisedEnchanting.apply(registries, new ItemStack(input));
            if (result.isEmpty())
                continue;
            var id = CreateEndergy.asResource("magnetising/enchant/" + BuiltInRegistries.ITEM.getKey(input).toString().replace(':', '/'));
            MagnetisingRecipe recipe = new StandardProcessingRecipe.Builder<>(MagnetisingRecipe::new, id)
                    .require(input)
                    .output(result)
                    .build();
            examples.add(new RecipeHolder<>(id, recipe));
        }
        return examples;
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(EndergyBlocks.MAGNETISER.asStack(), MagnetisingCategory.TYPE);
    }
}
