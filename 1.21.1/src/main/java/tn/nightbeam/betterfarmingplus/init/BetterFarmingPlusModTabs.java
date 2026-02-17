/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package tn.nightbeam.betterfarmingplus.init;

import tn.nightbeam.betterfarmingplus.BetterFarmingPlusMod;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.core.registries.Registries;

@EventBusSubscriber(modid = BetterFarmingPlusMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class BetterFarmingPlusModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BetterFarmingPlusMod.MODID);

	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
		if (tabData.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
			tabData.accept(BetterFarmingPlusModBlocks.FARMING_CRATE.get().asItem());
		}
	}
}