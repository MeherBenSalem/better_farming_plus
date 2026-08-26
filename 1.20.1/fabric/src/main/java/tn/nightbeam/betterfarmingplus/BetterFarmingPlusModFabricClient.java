package tn.nightbeam.betterfarmingplus;

import tn.naizo.jauml.JaumlConfigLib;
import tn.nightbeam.betterfarmingplus.client.HarvestIndicatorModel;
import tn.nightbeam.betterfarmingplus.client.HarvestIndicators;
import tn.nightbeam.betterfarmingplus.client.gui.FarmingCrateGUIScreen;
import tn.nightbeam.betterfarmingplus.init.ModRegistry;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.gui.screens.MenuScreens;

public class BetterFarmingPlusModFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HarvestIndicators.showHarvestIndicators = JaumlConfigLib.getBooleanValue("betterfarming", "global", "show_harvest_indicators");

		MenuScreens.register(ModRegistry.FARMING_CRATE_GUI, FarmingCrateGUIScreen::new);

		ModelLoadingPlugin.register(pluginContext -> pluginContext.modifyModelAfterBake().register((model, context) -> {
			if (!HarvestIndicators.showHarvestIndicators && HarvestIndicators.shouldWrap(context.id())) {
				return new HarvestIndicatorModel(model);
			}
			return model;
		}));
	}
}
