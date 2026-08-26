package tn.nightbeam.betterfarmingplus;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tn.nightbeam.betterfarmingplus.block.FarmingCrateBlock;
import tn.nightbeam.betterfarmingplus.block.entity.FarmingCrateBlockEntity;
import tn.nightbeam.betterfarmingplus.init.ModRegistry;
import tn.nightbeam.betterfarmingplus.neoforge.NeoForgeEvents;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import tn.nightbeam.betterfarmingplus.world.inventory.FarmingCrateGUIMenu;

@Mod(Constants.MOD_ID)
public class BetterFarmingPlusModNeoForge {
	public static final Logger LOGGER = LogManager.getLogger(BetterFarmingPlusModNeoForge.class);

	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, Constants.MOD_ID);
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Constants.MOD_ID);
	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);
	private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, Constants.MOD_ID);

	public static final DeferredHolder<Block, Block> FARMING_CRATE = BLOCKS.register("farming_crate", FarmingCrateBlock::new);
	public static final DeferredHolder<Item, Item> FARMING_CRATE_ITEM = ITEMS.register("farming_crate", () -> new BlockItem(FARMING_CRATE.get(), new Item.Properties()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FarmingCrateBlockEntity>> FARMING_CRATE_BE = BLOCK_ENTITIES.register("farming_crate",
			() -> BlockEntityType.Builder.of(FarmingCrateBlockEntity::new, FARMING_CRATE.get()).build(null));
	public static final DeferredHolder<MenuType<?>, MenuType<FarmingCrateGUIMenu>> FARMING_CRATE_GUI = MENUS.register("farming_crate_gui",
			() -> IMenuTypeExtension.create(FarmingCrateGUIMenu::new));

	public BetterFarmingPlusModNeoForge(IEventBus modEventBus) {
		BetterFarmingPlusMod.init();

		BLOCKS.register(modEventBus);
		ITEMS.register(modEventBus);
		BLOCK_ENTITIES.register(modEventBus);
		MENUS.register(modEventBus);

		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(this::buildTabContents);

		NeoForge.EVENT_BUS.register(NeoForgeEvents.class);
	}

	private void commonSetup(FMLCommonSetupEvent event) {
		ModRegistry.FARMING_CRATE = FARMING_CRATE.get();
		ModRegistry.FARMING_CRATE_ITEM = FARMING_CRATE_ITEM.get();
		ModRegistry.FARMING_CRATE_BE = FARMING_CRATE_BE.get();
		ModRegistry.FARMING_CRATE_GUI = FARMING_CRATE_GUI.get();
	}

	private void buildTabContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
			event.accept(FARMING_CRATE_ITEM.get());
		}
	}
}
