/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package tn.nightbeam.betterfarmingplus.init;

import tn.nightbeam.betterfarmingplus.BetterFarmingPlusMod;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

public class BetterFarmingPlusModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(BuiltInRegistries.ITEM, BetterFarmingPlusMod.MODID);
	public static final DeferredHolder<Item, Item> FARMING_CRATE = block(BetterFarmingPlusModBlocks.FARMING_CRATE);

	// Start of user code block custom items
	// End of user code block custom items
	private static DeferredHolder<Item, Item> block(DeferredHolder<Block, Block> block) {
		return block(block, new Item.Properties());
	}

	private static DeferredHolder<Item, Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
	}
}