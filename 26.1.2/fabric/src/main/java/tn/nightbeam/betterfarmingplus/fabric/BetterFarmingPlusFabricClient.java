package tn.nightbeam.betterfarmingplus.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import tn.nightbeam.betterfarmingplus.BetterFarmingPlus;
import tn.nightbeam.betterfarmingplus.Constants;
import tn.nightbeam.betterfarmingplus.client.FarmingCrateScreen;
import tn.nightbeam.betterfarmingplus.content.ModContent;
import tn.nightbeam.betterfarmingplus.core.FarmingConfig;

public final class BetterFarmingPlusFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModContent.FARMING_CRATE_MENU.get(), FarmingCrateScreen::new);
        if (FarmingConfig.get().harvestIndicators()) {
            FabricLoader.getInstance().getModContainer(Constants.MOD_ID).ifPresent(mod -> ResourceLoader.registerBuiltinPack(
                    BetterFarmingPlus.id(BetterFarmingPlus.INDICATOR_PACK), mod,
                    Component.translatable("pack.better_farming_plus.harvest_indicators"), PackActivationType.DEFAULT_ENABLED));
        }
    }
}
