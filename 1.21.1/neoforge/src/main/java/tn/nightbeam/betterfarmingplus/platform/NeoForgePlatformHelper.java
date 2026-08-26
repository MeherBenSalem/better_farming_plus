package tn.nightbeam.betterfarmingplus.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.loading.FMLLoader;
import tn.nightbeam.betterfarmingplus.block.entity.FarmingCrateBlockEntity;

public class NeoForgePlatformHelper implements tn.nightbeam.betterfarmingplus.platform.services.IPlatformHelper {
	@Override
	public String getPlatformName() {
		return "NeoForge";
	}

	@Override
	public boolean isModLoaded(String modId) {
		return net.neoforged.fml.ModList.get().isLoaded(modId);
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLLoader.isProduction();
	}

	@Override
	public String getItemId(Item item) {
		return BuiltInRegistries.ITEM.getKey(item).toString();
	}

	@Override
	public void openMenu(ServerPlayer player, MenuProvider menuProvider, BlockPos pos) {
		player.openMenu(menuProvider, pos);
	}

	@Override
	public BlockEntity createCrateBlockEntity(BlockPos pos, BlockState state) {
		return new FarmingCrateBlockEntity(pos, state);
	}
}
