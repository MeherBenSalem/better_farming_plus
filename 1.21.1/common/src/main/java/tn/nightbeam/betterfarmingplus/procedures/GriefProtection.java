package tn.nightbeam.betterfarmingplus.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.registries.BuiltInRegistries;

public class GriefProtection {
	public static boolean shouldCancelTrample(Entity entity) {
		if (entity == null)
			return false;
		if (JaumlConfigLib.getBooleanValue("betterfarming", "global", "trampling_protection")) {
			return JaumlConfigLib.stringExistsInArray("betterfarming", "global", "hoe_harvest_whitelist",
					BuiltInRegistries.ITEM.getKey((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem()).toString());
		}
		return false;
	}
}
