package tn.nightbeam.betterfarmingplus.client;

import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class HarvestIndicators {
	public static boolean showHarvestIndicators = true;

	private static final ResourceLocation[] INDICATOR_MODELS = {
			new ResourceLocation("minecraft", "block/wheat_stage7"),
			new ResourceLocation("minecraft", "block/carrots_stage3"),
			new ResourceLocation("minecraft", "block/potatoes_stage3"),
			new ResourceLocation("minecraft", "block/beetroots_stage3"),
			new ResourceLocation("minecraft", "block/nether_wart_stage2"),
			new ResourceLocation("minecraft", "block/sweet_berry_bush_stage3"),
			new ResourceLocation("minecraft", "block/melon_stem_fruit_north"),
			new ResourceLocation("minecraft", "block/melon_stem_fruit_east"),
			new ResourceLocation("minecraft", "block/melon_stem_fruit_south"),
			new ResourceLocation("minecraft", "block/melon_stem_fruit_west"),
			new ResourceLocation("minecraft", "block/pumpkin_stem_fruit_north"),
			new ResourceLocation("minecraft", "block/pumpkin_stem_fruit_east"),
			new ResourceLocation("minecraft", "block/pumpkin_stem_fruit_south"),
			new ResourceLocation("minecraft", "block/pumpkin_stem_fruit_west"),
	};

	private static final Set<String> INDICATOR_MODEL_IDS = new HashSet<>();

	static {
		for (ResourceLocation loc : INDICATOR_MODELS) {
			INDICATOR_MODEL_IDS.add(loc.toString());
		}
	}

	public static boolean shouldWrap(ResourceLocation id) {
		return INDICATOR_MODEL_IDS.contains(id.toString());
	}

	public static boolean shouldWrap(String modelKey) {
		int variantSeparator = modelKey.indexOf('#');
		String modelId = variantSeparator >= 0 ? modelKey.substring(0, variantSeparator) : modelKey;
		return INDICATOR_MODEL_IDS.contains(modelId);
	}
}
