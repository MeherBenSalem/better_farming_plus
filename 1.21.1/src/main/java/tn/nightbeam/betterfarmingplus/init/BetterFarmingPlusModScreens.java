/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package tn.nightbeam.betterfarmingplus.init;

import tn.nightbeam.betterfarmingplus.client.gui.FarmingCrateGUIScreen;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = tn.nightbeam.betterfarmingplus.BetterFarmingPlusMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BetterFarmingPlusModScreens {
	@SubscribeEvent
	public static void registerScreens(RegisterMenuScreensEvent event) {
		event.register(BetterFarmingPlusModMenus.FARMING_CRATE_GUI.get(), FarmingCrateGUIScreen::new);
	}

	public interface ScreenAccessor {
		void updateMenuState(int elementType, String name, Object elementState);
	}
}