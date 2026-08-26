package tn.nightbeam.betterfarmingplus;

import tn.nightbeam.betterfarmingplus.block.FarmingCrateBlock;
import tn.nightbeam.betterfarmingplus.block.entity.ForgeFarmingCrateBlockEntity;
import tn.nightbeam.betterfarmingplus.init.ModRegistry;
import tn.nightbeam.betterfarmingplus.world.inventory.FarmingCrateGUIMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(Constants.MOD_ID)
public class BetterFarmingPlusModForge {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.MOD_ID);
	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Constants.MOD_ID);

	public static final RegistryObject<Block> FARMING_CRATE_BLOCK = BLOCKS.register("farming_crate", FarmingCrateBlock::new);
	public static final RegistryObject<Item> FARMING_CRATE_ITEM = ITEMS.register("farming_crate", () -> new BlockItem(FARMING_CRATE_BLOCK.get(), new Item.Properties()));
	public static final RegistryObject<BlockEntityType<ForgeFarmingCrateBlockEntity>> FARMING_CRATE_BE = BLOCK_ENTITIES.register("farming_crate",
			() -> BlockEntityType.Builder.of(ForgeFarmingCrateBlockEntity::new, FARMING_CRATE_BLOCK.get()).build(null));
	public static final RegistryObject<MenuType<FarmingCrateGUIMenu>> FARMING_CRATE_GUI = MENUS.register("farming_crate_gui",
			() -> IForgeMenuType.create(FarmingCrateGUIMenu::new));

	public BetterFarmingPlusModForge() {
		BetterFarmingPlusMod.init();
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		BLOCKS.register(bus);
		ITEMS.register(bus);
		BLOCK_ENTITIES.register(bus);
		MENUS.register(bus);
		bus.addListener(this::commonSetup);
	}

	private void commonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			ModRegistry.FARMING_CRATE = FARMING_CRATE_BLOCK.get();
			ModRegistry.FARMING_CRATE_ITEM = FARMING_CRATE_ITEM.get();
			ModRegistry.FARMING_CRATE_BE = FARMING_CRATE_BE.get();
			ModRegistry.FARMING_CRATE_GUI = FARMING_CRATE_GUI.get();
		});
	}

	@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class CreativeTabEvents {
		@SubscribeEvent
		public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
			if (tabData.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
				tabData.accept(FARMING_CRATE_ITEM.get());
			}
		}
	}
}
