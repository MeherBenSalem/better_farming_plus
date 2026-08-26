package tn.nightbeam.betterfarmingplus.platform;

import tn.nightbeam.betterfarmingplus.block.entity.ForgeFarmingCrateBlockEntity;
import tn.nightbeam.betterfarmingplus.platform.services.IPlatformHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

public class ForgePlatformHelper implements IPlatformHelper {
	@Override
	public String getPlatformName() {
		return "Forge";
	}

	@Override
	public boolean isModLoaded(String modId) {
		return FMLLoader.getLoadingModList().getModFileById(modId) != null;
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLLoader.isProduction();
	}

	@Override
	public String getItemId(Item item) {
		return ForgeRegistries.ITEMS.getKey(item).toString();
	}

	@Override
	public void openMenu(ServerPlayer player, MenuProvider menuProvider, Consumer<FriendlyByteBuf> extraDataWriter) {
		NetworkHooks.openScreen(player, menuProvider, extraDataWriter);
	}

	@Override
	public BlockEntity createCrateBlockEntity(BlockPos pos, BlockState state) {
		return new ForgeFarmingCrateBlockEntity(pos, state);
	}
}
