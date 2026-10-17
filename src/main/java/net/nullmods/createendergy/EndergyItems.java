package net.nullmods.createendergy;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;
import net.nullmods.createendergy.content.wire.WireCoilItem;
import net.nullmods.createendergy.content.wire.WireType;

public class EndergyItems {
        private static final CreateRegistrate REGISTRATE = CreateEndergy.REGISTRATE;

        public static final ItemEntry<Item> ENDERGETIC_ALLOY = REGISTRATE.item("endergetic_alloy", Item::new)
                        .properties(p -> p.stacksTo(64))
                        .register();

        public static final ItemEntry<Item> ENDERGETIC_PRECISION_MECHANISM = REGISTRATE
                        .item("endergetic_precision_mechanism", Item::new)
                        .properties(p -> p.stacksTo(64))
                        .register();

        public static final ItemEntry<InsulatedWrenchItem> INSULATED_WRENCH = REGISTRATE
                        .item("insulated_wrench", InsulatedWrenchItem::new)
                        .properties(p -> p.stacksTo(1))
                        .register();

        public static final ItemEntry<Item> MAGNET = REGISTRATE
                        .item("magnet", Item::new)
                        .properties(p -> p.stacksTo(64))
                        .register();

        public static final ItemEntry<Item> ENDERGETIC_PLATE = REGISTRATE
                        .item("endergetic_plate", Item::new)
                        .properties(p -> p.stacksTo(64))
                        .register();

        public static final ItemEntry<WireCoilItem> COPPER_COIL = REGISTRATE
                        .item("copper_coil", p -> new WireCoilItem(p, WireType.COPPER))
                        .properties(p -> p.stacksTo(64))
                        .register();

        public static final ItemEntry<WireCoilItem> GOLD_COIL = REGISTRATE
                        .item("gold_coil", p -> new WireCoilItem(p, WireType.GOLD))
                        .properties(p -> p.stacksTo(64))
                        .register();

        public static final ItemEntry<WireCoilItem> ENDERGETIC_COIL = REGISTRATE
                        .item("endergetic_coil", p -> new WireCoilItem(p, WireType.ENDERGETIC))
                        .properties(p -> p.stacksTo(64))
                        .register();

        public static final ItemEntry<WireCoilItem> INSULATED_COPPER_COIL = REGISTRATE
                        .item("insulated_copper_coil", p -> new WireCoilItem(p, WireType.INSULATED_COPPER))
                        .properties(p -> p.stacksTo(64))
                        .register();

        public static final ItemEntry<WireCoilItem> INSULATED_GOLD_COIL = REGISTRATE
                        .item("insulated_gold_coil", p -> new WireCoilItem(p, WireType.INSULATED_GOLD))
                        .properties(p -> p.stacksTo(64))
                        .register();

        public static final ItemEntry<WireCoilItem> INSULATED_ENDERGETIC_COIL = REGISTRATE
                        .item("insulated_endergetic_coil", p -> new WireCoilItem(p, WireType.INSULATED_ENDERGETIC))
                        .properties(p -> p.stacksTo(64))
                        .register();

        public static void register() {
        }
}