package tn.nightbeam.betterfarmingplus;

import tn.naizo.jauml.JaumlConfigLib;

public class BetterFarmingPlusMod {
	public static void init() {
		String directory = "betterfarming";
		String filename = "global";
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
