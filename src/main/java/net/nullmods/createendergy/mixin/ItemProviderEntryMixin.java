package net.nullmods.createendergy.mixin;

import com.simibubi.create.AllItems;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import net.minecraft.world.item.Item;
import net.nullmods.createendergy.InsulatedWrenchItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Most of Create checks its wrench with AllItems.WRENCH.isIn(stack), which lands here
@Mixin(ItemProviderEntry.class)
public abstract class ItemProviderEntryMixin {
    @Inject(method = "is", at = @At("HEAD"), cancellable = true)
    private void create_endergy$acceptInsulatedWrench(Item item, CallbackInfoReturnable<Boolean> cir) {
        if (item instanceof InsulatedWrenchItem && (Object) this == AllItems.WRENCH)
            cir.setReturnValue(true);
    }
}
