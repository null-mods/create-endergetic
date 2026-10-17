package net.nullmods.createendergy.content.magnet;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.Optional;

// In code because the result is the very item that went in, which a JSON recipe can't express
public final class MagnetisedEnchanting {
    private MagnetisedEnchanting() {
    }

    public static Optional<Holder.Reference<Enchantment>> enchantment(RegistryAccess registries) {
        return registries.registryOrThrow(Registries.ENCHANTMENT).getHolder(MagnetHandler.MAGNETISED);
    }

    public static ItemStack apply(RegistryAccess registries, ItemStack input) {
        Optional<Holder.Reference<Enchantment>> found = enchantment(registries);
        if (found.isEmpty() || input.isEmpty())
            return ItemStack.EMPTY;
        Holder<Enchantment> magnetised = found.get();

        if (input.is(Items.BOOK))
            return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(magnetised, 1));

        if (!magnetised.value().isSupportedItem(input) || EnchantmentHelper.getItemEnchantmentLevel(magnetised, input) > 0)
            return ItemStack.EMPTY;
        if (!EnchantmentHelper.isEnchantmentCompatible(EnchantmentHelper.getEnchantmentsForCrafting(input).keySet(), magnetised))
            return ItemStack.EMPTY;
        ItemStack result = input.copyWithCount(1);
        result.enchant(magnetised, 1);
        return result;
    }
}
