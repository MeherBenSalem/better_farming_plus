package tn.nightbeam.betterfarmingplus.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import tn.nightbeam.betterfarmingplus.block.entity.FarmingCrateBlockEntity;

public class FabricPlatformHelper implements tn.nightbeam.betterfarmingplus.platform.services.IPlatformHelper {
	@Override
	public String getPlatformName() {
		return "Fabric";
	}

	@Override
	public boolean isModLoaded(String modId) {
		return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return net.fabricmc.loader.api.FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	@Override
	public String getItemId(Item item) {
		return BuiltInRegistries.ITEM.getKey(item).toString();
	}

	@Override
	public void openMenu(ServerPlayer player, MenuProvider menuProvider, BlockPos pos) {
		player.openMenu(new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory<BlockPos>() {
			@Override
			public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int syncId, net.minecraft.world.entity.player.Inventory inv, net.minecraft.world.entity.player.Player player) {
				return menuProvider.createMenu(syncId, inv, player);
			}

			@Override
			public net.minecraft.network.chat.Component getDisplayName() {
				return menuProvider.getDisplayName();
			}

			@Override
			public BlockPos getScreenOpeningData(ServerPlayer player) {
				return pos;
			}
		});
	}

	@Override
	public BlockEntity createCrateBlockEntity(BlockPos pos, BlockState state) {
		return new FarmingCrateBlockEntity(pos, state);
	}
}
