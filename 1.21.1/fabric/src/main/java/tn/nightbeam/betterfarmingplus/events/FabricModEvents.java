package tn.nightbeam.betterfarmingplus.events;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import tn.nightbeam.betterfarmingplus.procedures.RightClickHarvest;

public class FabricModEvents {
	public static void register() {
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (hand != InteractionHand.MAIN_HAND)
				return InteractionResult.PASS;
			if (!world.isClientSide()) {
				RightClickHarvest.execute(world, hitResult.getBlockPos().getX(), hitResult.getBlockPos().getY(), hitResult.getBlockPos().getZ(), player);
			}
			return InteractionResult.PASS;
		});
	}
}
