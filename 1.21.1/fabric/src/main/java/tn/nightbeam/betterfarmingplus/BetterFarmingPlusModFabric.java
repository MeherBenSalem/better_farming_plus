package tn.nightbeam.betterfarmingplus;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.core.BlockPos;
import tn.nightbeam.betterfarmingplus.block.FarmingCrateBlock;
import tn.nightbeam.betterfarmingplus.block.entity.FarmingCrateBlockEntity;
import tn.nightbeam.betterfarmingplus.events.FabricModEvents;
import tn.nightbeam.betterfarmingplus.init.ModRegistry;
import tn.nightbeam.betterfarmingplus.world.inventory.FarmingCrateGUIMenu;

public class BetterFarmingPlusModFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		BetterFarmingPlusMod.init();

		Block farmingCrate = Registry.register(BuiltInRegistries.BLOCK,
				ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "farming_crate"),
				new FarmingCrateBlock());
		Item farmingCrateItem = Registry.register(BuiltInRegistries.ITEM,
				ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "farming_crate"),
				new BlockItem(farmingCrate, new Item.Properties()));
		BlockEntityType<FarmingCrateBlockEntity> farmingCrateBe = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
				ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "farming_crate"),
				BlockEntityType.Builder.of(FarmingCrateBlockEntity::new, farmingCrate).build(null));
		MenuType<FarmingCrateGUIMenu> farmingCrateGui = Registry.register(BuiltInRegistries.MENU,
				ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "farming_crate_gui"),
				new ExtendedScreenHandlerType<>((syncId, inventory, data) -> new FarmingCrateGUIMenu(syncId, inventory, data), BlockPos.STREAM_CODEC));

		ModRegistry.FARMING_CRATE = farmingCrate;
		ModRegistry.FARMING_CRATE_ITEM = farmingCrateItem;
		ModRegistry.FARMING_CRATE_BE = farmingCrateBe;
		ModRegistry.FARMING_CRATE_GUI = farmingCrateGui;

		net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.REDSTONE_BLOCKS)
				.register(entries -> entries.accept(farmingCrateItem));

		FabricModEvents.register();
	}
}
