package net.nullmods.createendergetic;

import com.simibubi.create.content.equipment.wrench.WrenchItem;
import net.minecraft.world.item.ItemStack;

public class InsulatedWrenchItem extends WrenchItem {
    public InsulatedWrenchItem(Properties properties) {
        super(properties);
    }

    public static Object substituteFor(Object createWrench, ItemStack held) {
        return held.getItem() instanceof InsulatedWrenchItem ? held.getItem() : createWrench;
    }
}
