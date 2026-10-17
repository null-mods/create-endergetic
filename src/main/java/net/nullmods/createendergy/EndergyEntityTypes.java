package net.nullmods.createendergy;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nullmods.createendergy.content.magnet.MovingMagnetEntity;

public class EndergyEntityTypes {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE,
            CreateEndergy.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<MovingMagnetEntity>> MOVING_MAGNET = ENTITY_TYPES
            .register("moving_magnet",
                    () -> EntityType.Builder.<MovingMagnetEntity>of(MovingMagnetEntity::new, MobCategory.MISC)
                            .sized(.98f, .98f)
                            .clientTrackingRange(10)
                            .updateInterval(2)
                            .build("moving_magnet"));

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
