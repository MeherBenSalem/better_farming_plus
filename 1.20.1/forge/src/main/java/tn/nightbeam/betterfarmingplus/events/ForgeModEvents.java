package tn.nightbeam.betterfarmingplus.events;

import tn.nightbeam.betterfarmingplus.procedures.GriefProtection;
import tn.nightbeam.betterfarmingplus.procedures.RightClickHarvest;

import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import tn.nightbeam.betterfarmingplus.Constants;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class ForgeModEvents {
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
