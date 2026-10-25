package tn.nightbeam.betterfarmingplus.content;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import tn.nightbeam.betterfarmingplus.BetterFarmingPlus;

public final class ModTags {
    /** Harvested and replanted besides every {@code CropBlock} (default: nether wart, cocoa). */
    public static final TagKey<Block> HARVESTABLE = TagKey.create(Registries.BLOCK, BetterFarmingPlus.id("harvestable"));
    /** Never harvested even if they look like crops (default: the torchflower crop, which turns into a flower). */
    public static final TagKey<Block> NOT_HARVESTABLE = TagKey.create(Registries.BLOCK, BetterFarmingPlus.id("not_harvestable"));
    /** Plants the sprinkler gives extra growth ticks to. */
    public static final TagKey<Block> SPRINKLER_BOOSTED = TagKey.create(Registries.BLOCK, BetterFarmingPlus.id("sprinkler_boosted"));

    private ModTags() {
    }
}
