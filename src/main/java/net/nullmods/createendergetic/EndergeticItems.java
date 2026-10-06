package net.nullmods.createendergetic;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;

public class EndergeticItems {
    private static final CreateRegistrate REGISTRATE = CreateEndergetic.REGISTRATE;


    public static final ItemEntry<Item> ENDERGETIC_ALLOY = REGISTRATE
                .item("endergetic_alloy", Item::new)
                .properties(p -> p.stacksTo(64))
                .register();

    public static final ItemEntry<Item> ENDERGETIC_PRECISION_MECHANISM = REGISTRATE
                .item("endergetic_precision_mechanism", Item::new)
                .properties(p -> p.stacksTo(64))
                .register();

    public static void register() {}
}