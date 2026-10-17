package net.nullmods.createendergy;

import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nullmods.createendergy.content.magnetiser.MagnetisingRecipe;

import java.util.Locale;

public enum EndergyRecipeTypes implements IRecipeTypeInfo {
    MAGNETISING(MagnetisingRecipe::new);

    private final ResourceLocation id;
    private final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> serializer;
    private final DeferredHolder<RecipeType<?>, RecipeType<?>> type;

    EndergyRecipeTypes(StandardProcessingRecipe.Factory<?> factory) {
        String name = name().toLowerCase(Locale.ROOT);
        id = CreateEndergy.asResource(name);
        serializer = Registers.SERIALIZERS.register(name, () -> serializerFor(factory));
        type = Registers.TYPES.register(name, () -> RecipeType.simple(id));
    }

    private static <R extends StandardProcessingRecipe<?>> RecipeSerializer<R> serializerFor(
            StandardProcessingRecipe.Factory<R> factory) {
        return new StandardProcessingRecipe.Serializer<>(factory);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends RecipeSerializer<?>> T getSerializer() {
        return (T) serializer.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <I extends RecipeInput, R extends Recipe<I>> RecipeType<R> getType() {
        return (RecipeType<R>) type.get();
    }

    public static void register(IEventBus modEventBus) {
        Registers.SERIALIZERS.register(modEventBus);
        Registers.TYPES.register(modEventBus);
    }

    private static class Registers {
        private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister
                .create(Registries.RECIPE_SERIALIZER, CreateEndergy.MOD_ID);
        private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE,
                CreateEndergy.MOD_ID);
    }
}
