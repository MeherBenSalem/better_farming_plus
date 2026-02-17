/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package tn.nightbeam.betterfarmingplus.init;

import tn.nightbeam.betterfarmingplus.block.FarmingCrateBlock;
import tn.nightbeam.betterfarmingplus.BetterFarmingPlusMod;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

public class BetterFarmingPlusModBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(BuiltInRegistries.BLOCK, BetterFarmingPlusMod.MODID);
	public static final DeferredHolder<Block, Block> FARMING_CRATE = REGISTRY.register("farming_crate", () -> new FarmingCrateBlock());
	// Start of user code block custom blocks
	// End of user code block custom blocks
}