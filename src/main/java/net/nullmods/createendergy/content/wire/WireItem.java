package net.nullmods.createendergy.content.wire;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class WireItem extends Item {
    private static final String START = "WireStart";

    private final WireType type;

    public WireItem(Properties properties, WireType type) {
        super(properties);
        this.type = type;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!(level.getBlockEntity(pos) instanceof InsulatorBlockEntity here))
            return InteractionResult.PASS;
        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);

        if (!data.contains(START)) {
            if (here.isFull())
                return tell(player, "full");
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putLong(START, pos.asLong()));
            return tell(player, "start");
        }

        BlockPos start = BlockPos.of(data.copyTag().getLong(START));
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(START));
        if (start.equals(pos))
            return tell(player, "cancelled");
        if (!(level.getBlockEntity(start) instanceof InsulatorBlockEntity there))
            return tell(player, "lost");
        if (here.isLinkedTo(start))
            return tell(player, "already");
        if (here.isFull() || there.isFull())
            return tell(player, "full");
        if (here.getAttachPoint(InsulatorBlock.Neck.INPUT)
                .distanceTo(there.getAttachPoint(InsulatorBlock.Neck.OUTPUT)) > InsulatorBlockEntity.MAX_LENGTH)
            return tell(player, "too_far");

        here.addLink(start, type, false);
        there.addLink(pos, type, true);
        if (player == null || !player.getAbilities().instabuild)
            stack.shrink(1);
        level.playSound(null, pos, SoundEvents.LEASH_KNOT_PLACE, SoundSource.BLOCKS, 1, 1);
        return tell(player, "connected");
    }

    private static InteractionResult tell(Player player, String key) {
        if (player != null)
            player.displayClientMessage(Component.translatable("create_endergy.wire." + key,
                    (int) InsulatorBlockEntity.MAX_LENGTH), true);
        return InteractionResult.SUCCESS;
    }
}
