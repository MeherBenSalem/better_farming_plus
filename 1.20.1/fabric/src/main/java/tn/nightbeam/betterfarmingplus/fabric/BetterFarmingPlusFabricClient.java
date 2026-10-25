package tn.nightbeam.betterfarmingplus.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import tn.nightbeam.betterfarmingplus.BetterFarmingPlus;
import tn.nightbeam.betterfarmingplus.Constants;
import tn.nightbeam.betterfarmingplus.client.FarmingCrateScreen;
import tn.nightbeam.betterfarmingplus.client.IndicatorPack;
import tn.nightbeam.betterfarmingplus.content.ModContent;
import tn.nightbeam.betterfarmingplus.core.FarmingConfig;

public final class BetterFarmingPlusFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModContent.FARMING_CRATE_MENU.get(), FarmingCrateScreen::new);
        if (FarmingConfig.get().harvestIndicators()) {
            FabricLoader.getInstance().getModContainer(Constants.MOD_ID).ifPresent(mod -> ResourceManagerHelper.registerBuiltinResourcePack(
                    BetterFarmingPlus.id(BetterFarmingPlus.INDICATOR_PACK), mod,
                    Component.translatable("pack.better_farming_plus.harvest_indicators"), ResourcePackActivationType.DEFAULT_ENABLED));
            ClientLifecycleEvents.CLIENT_STARTED.register(client ->
                    IndicatorPack.selectOnce(client, FabricLoader.getInstance().getConfigDir(), IndicatorPack.FABRIC_ID));
        }
    }
}
