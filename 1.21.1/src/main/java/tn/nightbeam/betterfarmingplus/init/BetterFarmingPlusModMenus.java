/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package tn.nightbeam.betterfarmingplus.init;

import tn.nightbeam.betterfarmingplus.world.inventory.FarmingCrateGUIMenu;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

import tn.nightbeam.betterfarmingplus.BetterFarmingPlusMod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public class BetterFarmingPlusModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.MENU, BetterFarmingPlusMod.MODID);
	public static final DeferredHolder<MenuType<?>, MenuType<FarmingCrateGUIMenu>> FARMING_CRATE_GUI = REGISTRY.register("farming_crate_gui",
			() -> IMenuTypeExtension.create(FarmingCrateGUIMenu::new));

	public interface MenuAccessor {
		Map<String, Object> getMenuState();

		Map<Integer, Slot> getSlots();

		default void sendMenuStateUpdate(Player player, int elementType, String name, Object elementState, boolean needClientUpdate) {
			getMenuState().put(elementType + ":" + name, elementState);
			if (player.level().isClientSide && needClientUpdate && net.minecraft.client.Minecraft.getInstance().screen instanceof BetterFarmingPlusModScreens.ScreenAccessor accessor)
				accessor.updateMenuState(elementType, name, elementState);
		}

		default <T> T getMenuState(int elementType, String name, T defaultValue) {
			try {
				return (T) getMenuState().getOrDefault(elementType + ":" + name, defaultValue);
			} catch (ClassCastException e) {
				return defaultValue;
			}
		}
	}
}