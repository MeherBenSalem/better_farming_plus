package tn.nightbeam.betterfarmingplus.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tn.nightbeam.betterfarmingplus.procedures.GriefProtection;

@Mixin(FarmBlock.class)
public class FarmlandBlockMixin {
	@Inject(method = "fallOn", at = @At("HEAD"), cancellable = true)
	private void betterfarmingplus$preventTrample(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance, CallbackInfo ci) {
		if (GriefProtection.shouldCancelTrample(entity)) {
			ci.cancel();
		}
	}
}
