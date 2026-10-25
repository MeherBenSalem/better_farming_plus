package tn.nightbeam.betterfarmingplus.client;

import net.minecraft.network.chat.Component;
import tn.nightbeam.betterfarmingplus.block.FarmingCrateBlockEntity;

/** The crate screen's status texts (shared by every version's screen). */
public final class CrateText {
    private CrateText() {
    }

    public static Component status(int status) {
        return Component.translatable(switch (status) {
            case FarmingCrateBlockEntity.STATUS_WORKING -> "gui.better_farming_plus.crate.working";
            case FarmingCrateBlockEntity.STATUS_FULL -> "gui.better_farming_plus.crate.full";
            default -> "gui.better_farming_plus.crate.idle";
        });
    }

    public static int statusColor(int status) {
        return switch (status) {
            case FarmingCrateBlockEntity.STATUS_WORKING -> 0xFF3F6B2A;
            case FarmingCrateBlockEntity.STATUS_FULL -> 0xFFB02E26;
            default -> 0xFF404040;
        };
    }

    public static Component harvested(int count) {
        return Component.translatable("gui.better_farming_plus.crate.harvested", String.format("%,d", count));
    }

    public static Component area(int radius) {
        int side = radius * 2 + 1;
        return Component.translatable("gui.better_farming_plus.crate.area", side, side);
    }
}
