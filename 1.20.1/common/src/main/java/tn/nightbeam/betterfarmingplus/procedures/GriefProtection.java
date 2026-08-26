package tn.nightbeam.betterfarmingplus.procedures;

import tn.naizo.jauml.JaumlConfigLib;
import tn.nightbeam.betterfarmingplus.platform.Services;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class GriefProtection {
	public static boolean shouldCancelTrample(Entity entity) {
		if (entity == null)
			return false;
		if (!JaumlConfigLib.getBooleanValue("betterfarming", "global", "trampling_protection"))
			return false;
		ItemStack held = entity instanceof LivingEntity living ? living.getMainHandItem() : ItemStack.EMPTY;
		return JaumlConfigLib.stringExistsInArray("betterfarming", "global", "hoe_harvest_whitelist", Services.PLATFORM.getItemId(held.getItem()));
	}
}
