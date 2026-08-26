package tn.nightbeam.betterfarmingplus.platform;

import tn.nightbeam.betterfarmingplus.block.entity.FarmingCrateBlockEntity;
import tn.nightbeam.betterfarmingplus.platform.services.IPlatformHelper;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class FabricPlatformHelper implements IPlatformHelper {
	@Override
	public String getPlatformName() {
		return "Fabric";
	}

	@Override
	public boolean isModLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	@Override
	public String getItemId(Item item) {
		return BuiltInRegistries.ITEM.getKey(item).toString();
	}

	@Override
	public void openMenu(ServerPlayer player, MenuProvider menuProvider, Consumer<FriendlyByteBuf> extraDataWriter) {
		player.openMenu(new ExtendedScreenHandlerFactory() {
			@Override
			public AbstractContainerMenu createMenu(int syncId, net.minecraft.world.entity.player.Inventory inv, net.minecraft.world.entity.player.Player player) {
				return menuProvider.createMenu(syncId, inv, player);
			}

			@Override
			public net.minecraft.network.chat.Component getDisplayName() {
				return menuProvider.getDisplayName();
			}

			@Override
			public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
				extraDataWriter.accept(buf);
			}
		});
	}

	@Override
	public BlockEntity createCrateBlockEntity(BlockPos pos, BlockState state) {
		return new FarmingCrateBlockEntity(pos, state);
	}
}
