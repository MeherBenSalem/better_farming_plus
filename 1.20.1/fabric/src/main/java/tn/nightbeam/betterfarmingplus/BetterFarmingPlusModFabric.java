package tn.nightbeam.betterfarmingplus;

import tn.nightbeam.betterfarmingplus.block.FarmingCrateBlock;
import tn.nightbeam.betterfarmingplus.block.entity.FarmingCrateBlockEntity;
import tn.nightbeam.betterfarmingplus.events.FabricModEvents;
import tn.nightbeam.betterfarmingplus.init.ModRegistry;
import tn.nightbeam.betterfarmingplus.world.inventory.FarmingCrateGUIMenu;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BetterFarmingPlusModFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		BetterFarmingPlusMod.init();

		Block farmingCrate = Registry.register(BuiltInRegistries.BLOCK,
				new ResourceLocation(Constants.MOD_ID, "farming_crate"), new FarmingCrateBlock());
		Item farmingCrateItem = Registry.register(BuiltInRegistries.ITEM,
				new ResourceLocation(Constants.MOD_ID, "farming_crate"), new BlockItem(farmingCrate, new Item.Properties()));
		BlockEntityType<FarmingCrateBlockEntity> farmingCrateBe = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
				new ResourceLocation(Constants.MOD_ID, "farming_crate"),
				BlockEntityType.Builder.of(FarmingCrateBlockEntity::new, farmingCrate).build(null));
		MenuType<FarmingCrateGUIMenu> farmingCrateGui = Registry.register(BuiltInRegistries.MENU,
				new ResourceLocation(Constants.MOD_ID, "farming_crate_gui"),
				new ExtendedScreenHandlerType<>(FarmingCrateGUIMenu::new));

		ModRegistry.FARMING_CRATE = farmingCrate;
		ModRegistry.FARMING_CRATE_ITEM = farmingCrateItem;
		ModRegistry.FARMING_CRATE_BE = farmingCrateBe;
		ModRegistry.FARMING_CRATE_GUI = farmingCrateGui;

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(entries -> entries.accept(farmingCrateItem));

		FabricModEvents.register();
	}
}
