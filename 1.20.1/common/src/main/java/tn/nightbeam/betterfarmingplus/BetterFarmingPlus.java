package tn.nightbeam.betterfarmingplus;

import net.minecraft.resources.ResourceLocation;
import tn.nightbeam.betterfarmingplus.content.ModContent;
import tn.nightbeam.betterfarmingplus.core.FarmingConfig;
import tn.nightbeam.betterfarmingplus.platform.Platform;
import tn.nightbeam.betterfarmingplus.platform.Registrar;

public final class BetterFarmingPlus {
    /** The built-in resource pack with the harvest indicators (under {@code resourcepacks/} in the jar). */
    public static final String INDICATOR_PACK = "harvest_indicators";

    private BetterFarmingPlus() {
    }

    public static void init(Platform platform, Registrar registrar, ModContent.MenuTypeFactory menus) {
        FarmingConfig.load(platform.configDir());
        ModContent.register(registrar, menus);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(Constants.MOD_ID, path);
    }
}
