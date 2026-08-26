package tn.nightbeam.betterfarmingplus.client;

import tn.naizo.jauml.JaumlConfigLib;
import tn.nightbeam.betterfarmingplus.BetterFarmingPlusModForge;
import tn.nightbeam.betterfarmingplus.client.gui.FarmingCrateGUIScreen;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.gui.screens.MenuScreens;

import java.util.Map;

@Mod.EventBusSubscriber(modid = tn.nightbeam.betterfarmingplus.Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
	@SubscribeEvent
	public static void clientLoad(FMLClientSetupEvent event) {
		event.enqueueWork(() -> MenuScreens.register(BetterFarmingPlusModForge.FARMING_CRATE_GUI.get(), FarmingCrateGUIScreen::new));
	}

	@SubscribeEvent
	public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
		HarvestIndicators.showHarvestIndicators = JaumlConfigLib.getBooleanValue("betterfarming", "global", "show_harvest_indicators");

		if (!HarvestIndicators.showHarvestIndicators) {
			Map<?, BakedModel> models = event.getModels();
			for (Map.Entry<?, BakedModel> entry : models.entrySet()) {
				String key = String.valueOf(entry.getKey());
				if (HarvestIndicators.shouldWrap(key)) {
					BakedModel existing = entry.getValue();
					if (existing != null && !(existing instanceof HarvestIndicatorModelForge)) {
						entry.setValue(new HarvestIndicatorModelForge(existing));
					}
				}
			}
		}
	}
}
