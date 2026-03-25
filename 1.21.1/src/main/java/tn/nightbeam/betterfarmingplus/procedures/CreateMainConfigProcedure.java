package tn.nightbeam.betterfarmingplus.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = tn.nightbeam.betterfarmingplus.BetterFarmingPlusMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class CreateMainConfigProcedure {
	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		execute();
	}

	public static void execute() {
		String directory = "";
		String filename = "";
		directory = "betterfarming";
		filename = "global";
		if (JaumlConfigLib.createConfigFile(directory, filename)) {
			JaumlConfigLib.createConfigFile(directory, filename);
		}
		if (!JaumlConfigLib.arrayKeyExists(directory, filename, "hoe_harvest_whitelist")) {
			JaumlConfigLib.addStringToArray(directory, filename, "hoe_harvest_whitelist", "minecraft:wooden_hoe");
			JaumlConfigLib.addStringToArray(directory, filename, "hoe_harvest_whitelist", "minecraft:stone_hoe");
			JaumlConfigLib.addStringToArray(directory, filename, "hoe_harvest_whitelist", "minecraft:iron_hoe");
			JaumlConfigLib.addStringToArray(directory, filename, "hoe_harvest_whitelist", "minecraft:golden_hoe");
			JaumlConfigLib.addStringToArray(directory, filename, "hoe_harvest_whitelist", "minecraft:diamond_hoe");
			JaumlConfigLib.addStringToArray(directory, filename, "hoe_harvest_whitelist", "minecraft:netherite_hoe");
		}
		if (!JaumlConfigLib.arrayKeyExists(directory, filename, "trampling_protection")) {
			JaumlConfigLib.setBooleanValue(directory, filename, "trampling_protection", true);
		}
		if (!JaumlConfigLib.arrayKeyExists(directory, filename, "show_harvest_indicators")) {
			JaumlConfigLib.setBooleanValue(directory, filename, "show_harvest_indicators", true);
		}
	}
}