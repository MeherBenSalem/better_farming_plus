package tn.nightbeam.betterfarmingplus.init;

import net.minecraft.world.inventory.Slot;

import java.util.Map;

public interface MenuAccessor {
	Map<String, Object> getMenuState();

	Map<Integer, Slot> getSlots();
}
