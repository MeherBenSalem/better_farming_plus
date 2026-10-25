package tn.nightbeam.betterfarmingplus.farming;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import tn.nightbeam.betterfarmingplus.core.FarmingConfig;

public final class Trampling {
    private Trampling() {
    }

    /** Whether {@code entity} landing on farmland must leave it as it is. */
    public static boolean prevent(Entity entity) {
        return switch (FarmingConfig.get().trampling()) {
            case NEVER -> true;
            case WITH_TOOL -> entity instanceof LivingEntity living && Harvesting.isHarvestTool(living.getMainHandItem());
            case VANILLA -> false;
        };
    }
}
