package tn.nightbeam.betterfarmingplus.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.registries.BuiltInRegistries;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = tn.nightbeam.betterfarmingplus.BetterFarmingPlusMod.MODID)
public class GriefProtectionProcedure {
	@SubscribeEvent
	public static void onFarmlandTrampled(BlockEvent.FarmlandTrampleEvent event) {
		execute(event, event.getEntity());
	}

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(@Nullable BlockEvent.FarmlandTrampleEvent event, Entity entity) {
		if (entity == null)
			return;
		if (JaumlConfigLib.getBooleanValue("betterfarming", "global", "trampling_protection")) {
			if (JaumlConfigLib.stringExistsInArray("betterfarming", "global", "hoe_harvest_whitelist", (BuiltInRegistries.ITEM.getKey((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem()).toString()))) {
				if (event != null) {
					event.setCanceled(true);
				}
			}
		}
	}
}