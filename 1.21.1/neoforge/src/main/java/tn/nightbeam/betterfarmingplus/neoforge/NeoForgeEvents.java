package tn.nightbeam.betterfarmingplus.neoforge;

import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import tn.nightbeam.betterfarmingplus.procedures.GriefProtection;
import tn.nightbeam.betterfarmingplus.procedures.RightClickHarvest;

public class NeoForgeEvents {
	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (event.getHand() != InteractionHand.MAIN_HAND)
			return;
		RightClickHarvest.execute(event.getLevel(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), event.getEntity());
	}

	@SubscribeEvent
	public static void onFarmlandTrampled(BlockEvent.FarmlandTrampleEvent event) {
		if (GriefProtection.shouldCancelTrample(event.getEntity())) {
			event.setCanceled(true);
		}
	}
}
