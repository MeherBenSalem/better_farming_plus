package tn.nightbeam.betterfarmingplus.init;

import tn.nightbeam.betterfarmingplus.block.entity.FarmingCrateBlockEntity;
import tn.nightbeam.betterfarmingplus.world.inventory.FarmingCrateGUIMenu;

import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModRegistry {
	public static Block FARMING_CRATE;
	public static BlockEntityType<? extends FarmingCrateBlockEntity> FARMING_CRATE_BE;
	public static MenuType<FarmingCrateGUIMenu> FARMING_CRATE_GUI;
	public static Item FARMING_CRATE_ITEM;
}
