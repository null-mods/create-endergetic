package net.nullmods.createendergetic.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.contraptions.wrench.RadialWrenchHandler;
import net.minecraft.client.Minecraft;
import net.nullmods.createendergetic.InsulatedWrenchItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** The rotate-block menu compares the main-hand item to {@code AllItems.WRENCH.get()} directly. */
@Mixin(RadialWrenchHandler.class)
public abstract class RadialWrenchHandlerMixin {
    @ModifyExpressionValue(method = "onKeyInput", at = @At(value = "INVOKE",
            target = "Lcom/tterrag/registrate/util/entry/ItemEntry;get()Ljava/lang/Object;"))
    private static Object create_endergetic$acceptInsulatedWrench(Object createWrench) {
        return InsulatedWrenchItem.substituteFor(createWrench, Minecraft.getInstance().player.getMainHandItem());
    }
}
