package net.nullmods.createendergy.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.contraptions.wrench.RadialWrenchHandler;
import net.minecraft.client.Minecraft;
import net.nullmods.createendergy.InsulatedWrenchItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// The rotate menu compares the held item to AllItems.WRENCH.get() directly
@Mixin(RadialWrenchHandler.class)
public abstract class RadialWrenchHandlerMixin {
    @ModifyExpressionValue(method = "onKeyInput", at = @At(value = "INVOKE", target = "Lcom/tterrag/registrate/util/entry/ItemEntry;get()Ljava/lang/Object;"))
    private static Object create_endergy$acceptInsulatedWrench(Object createWrench) {
        return InsulatedWrenchItem.substituteFor(createWrench, Minecraft.getInstance().player.getMainHandItem());
    }
}
