package tn.nightbeam.betterfarmingplus.mixin;

import tn.nightbeam.betterfarmingplus.procedures.GriefProtection;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmBlock.class)
public class FarmlandBlockMixin {
	@Inject(method = "fallOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;F)V", at = @At("HEAD"), cancellable = true)
	private void betterFarmingPlus$cancelTrample(Level level, BlockPos pos, Entity entity, float fallDistance, CallbackInfo ci) {
		if (GriefProtection.shouldCancelTrample(entity)) {
			ci.cancel();
		}
	}
}
