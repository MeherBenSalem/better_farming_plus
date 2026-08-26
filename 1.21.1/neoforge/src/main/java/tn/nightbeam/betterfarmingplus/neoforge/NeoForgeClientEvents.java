package tn.nightbeam.betterfarmingplus.neoforge;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import tn.naizo.jauml.JaumlConfigLib;
import tn.nightbeam.betterfarmingplus.BetterFarmingPlusModNeoForge;
import tn.nightbeam.betterfarmingplus.Constants;
import tn.nightbeam.betterfarmingplus.client.HarvestIndicatorModel;
import tn.nightbeam.betterfarmingplus.client.HarvestIndicators;
import tn.nightbeam.betterfarmingplus.client.gui.FarmingCrateGUIScreen;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NeoForgeClientEvents {
	@SubscribeEvent
	public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
		HarvestIndicators.showHarvestIndicators = JaumlConfigLib.getBooleanValue("betterfarming", "global", "show_harvest_indicators");

		if (!HarvestIndicators.showHarvestIndicators) {
			Set<String> indicatorModelIds = new HashSet<>();
			for (ResourceLocation loc : HarvestIndicators.INDICATOR_MODELS) {
				indicatorModelIds.add(loc.toString());
			}

			Map<?, BakedModel> models = event.getModels();
			for (Map.Entry<?, BakedModel> entry : models.entrySet()) {
				String key = String.valueOf(entry.getKey());
				int variantSeparator = key.indexOf('#');
				String modelId = variantSeparator >= 0 ? key.substring(0, variantSeparator) : key;
				if (indicatorModelIds.contains(modelId)) {
					BakedModel existing = entry.getValue();
					if (existing != null && !(existing instanceof HarvestIndicatorModel)) {
						entry.setValue(new HarvestIndicatorModel(existing));
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void registerScreens(RegisterMenuScreensEvent event) {
		event.register(BetterFarmingPlusModNeoForge.FARMING_CRATE_GUI.get(), FarmingCrateGUIScreen::new);
	}
}
