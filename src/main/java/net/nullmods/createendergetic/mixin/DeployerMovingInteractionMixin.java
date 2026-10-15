package net.nullmods.createendergetic.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.deployer.DeployerMovingInteraction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.nullmods.createendergetic.InsulatedWrenchItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Switching a deployer's mode on a moving contraption uses {@code item.equals(AllItems.WRENCH.get())}. */
@Mixin(DeployerMovingInteraction.class)
public abstract class DeployerMovingInteractionMixin {
    @ModifyExpressionValue(method = "handlePlayerInteraction", at = @At(value = "INVOKE",
            target = "Lcom/tterrag/registrate/util/entry/ItemEntry;get()Ljava/lang/Object;"))
    private Object create_endergetic$acceptInsulatedWrench(Object createWrench,
            @Local(argsOnly = true) Player player, @Local(argsOnly = true) InteractionHand hand) {
        return InsulatedWrenchItem.substituteFor(createWrench, player.getItemInHand(hand));
    }
}
