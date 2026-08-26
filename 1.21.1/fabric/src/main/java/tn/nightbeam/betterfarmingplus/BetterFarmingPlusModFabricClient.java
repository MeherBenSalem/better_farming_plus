package tn.nightbeam.betterfarmingplus;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import tn.naizo.jauml.JaumlConfigLib;
import tn.nightbeam.betterfarmingplus.client.HarvestIndicatorModel;
import tn.nightbeam.betterfarmingplus.client.HarvestIndicators;
import tn.nightbeam.betterfarmingplus.client.gui.FarmingCrateGUIScreen;
import tn.nightbeam.betterfarmingplus.init.ModRegistry;

import java.util.HashSet;
import java.util.Set;

public class BetterFarmingPlusModFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModRegistry.FARMING_CRATE_GUI, FarmingCrateGUIScreen::new);

		ModelLoadingPlugin.register(pluginContext -> pluginContext.modifyModelAfterBake().register(ModelModifier.WRAP_PHASE, (model, context) -> {
			HarvestIndicators.showHarvestIndicators = JaumlConfigLib.getBooleanValue("betterfarming", "global", "show_harvest_indicators");
			if (HarvestIndicators.showHarvestIndicators || model == null) {
				return model;
			}
			Set<String> indicatorModelIds = new HashSet<>();
			for (ResourceLocation loc : HarvestIndicators.INDICATOR_MODELS) {
				indicatorModelIds.add(loc.toString());
			}
			ResourceLocation modelId = context.resourceId() != null ? context.resourceId()
					: (context.topLevelId() != null ? context.topLevelId().id() : null);
			if (modelId != null && indicatorModelIds.contains(modelId.toString()) && !(model instanceof HarvestIndicatorModel)) {
				return new HarvestIndicatorModel(model);
			}
			return model;
		}));
	}
}
