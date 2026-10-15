package net.nullmods.createendergy.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.trains.bogey.AbstractBogeyBlock;
import net.minecraft.world.item.ItemStack;
import net.nullmods.createendergy.InsulatedWrenchItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Cycling a train bogey's style uses {@code stack.is(AllItems.WRENCH.get())}. */
@Mixin(AbstractBogeyBlock.class)
public abstract class AbstractBogeyBlockMixin {
    @ModifyExpressionValue(method = "useItemOn", at = @At(value = "INVOKE",
            target = "Lcom/tterrag/registrate/util/entry/ItemEntry;get()Ljava/lang/Object;"))
    private Object create_endergy$acceptInsulatedWrench(Object createWrench,
            @Local(argsOnly = true) ItemStack stack) {
        return InsulatedWrenchItem.substituteFor(createWrench, stack);
    }
}
