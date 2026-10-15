package net.nullmods.createendergy.mixin;

import com.simibubi.create.AllItems;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import net.minecraft.world.item.Item;
import net.nullmods.createendergy.InsulatedWrenchItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Most of Create asks {@code AllItems.WRENCH.isIn(stack)}, which ends up in {@code is(Item)}.
 * Answering yes there for the insulated wrench covers belts, funnels, crafters, latches,
 * the minecart insta-kill and every other check of that form at once.
 */
@Mixin(ItemProviderEntry.class)
public abstract class ItemProviderEntryMixin {
    @Inject(method = "is", at = @At("HEAD"), cancellable = true)
    private void create_endergy$acceptInsulatedWrench(Item item, CallbackInfoReturnable<Boolean> cir) {
        if (item instanceof InsulatedWrenchItem && (Object) this == AllItems.WRENCH)
            cir.setReturnValue(true);
    }
}
