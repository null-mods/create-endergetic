package net.nullmods.createendergy;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

public class EndergyPartialModels {
    public static final PartialModel MAGNETISER_SHAFT = block("magnetiser/shaft");
    public static final PartialModel MAGNETISER_CIRCLE_LEFT = block("magnetiser/circle_left");
    public static final PartialModel MAGNETISER_CIRCLE_RIGHT = block("magnetiser/circle_right");
    public static final PartialModel MAGNETISER_CONDUCTOR_LEFT = block("magnetiser/conductor_left");
    public static final PartialModel MAGNETISER_CONDUCTOR_RIGHT = block("magnetiser/conductor_right");

    private static PartialModel block(String path) {
        return PartialModel.of(CreateEndergy.asResource("block/" + path));
    }

    public static void init() {
    }
}
