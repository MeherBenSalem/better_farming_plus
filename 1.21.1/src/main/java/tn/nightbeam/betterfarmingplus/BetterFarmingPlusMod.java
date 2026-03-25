package tn.nightbeam.betterfarmingplus;

import tn.nightbeam.betterfarmingplus.init.BetterFarmingPlusModTabs;
import tn.nightbeam.betterfarmingplus.init.BetterFarmingPlusModMenus;
import tn.nightbeam.betterfarmingplus.init.BetterFarmingPlusModItems;
import tn.nightbeam.betterfarmingplus.init.BetterFarmingPlusModBlocks;
import tn.nightbeam.betterfarmingplus.init.BetterFarmingPlusModBlockEntities;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;

@Mod("better_farming_plus")
public class BetterFarmingPlusMod {
	public static final Logger LOGGER = LogManager.getLogger(BetterFarmingPlusMod.class);
	public static final String MODID = "better_farming_plus";

	public BetterFarmingPlusMod(IEventBus bus) {

		BetterFarmingPlusModBlocks.REGISTRY.register(bus);
		BetterFarmingPlusModBlockEntities.REGISTRY.register(bus);
		BetterFarmingPlusModItems.REGISTRY.register(bus);

		BetterFarmingPlusModTabs.REGISTRY.register(bus);

		BetterFarmingPlusModMenus.REGISTRY.register(bus);

	}

}