package tn.nightbeam.betterfarmingplus.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface IPlatformHelper {
	String getPlatformName();

	boolean isModLoaded(String modId);

	boolean isDevelopmentEnvironment();

	String getItemId(Item item);

	void openMenu(ServerPlayer player, MenuProvider menuProvider, BlockPos pos);

	BlockEntity createCrateBlockEntity(BlockPos pos, BlockState state);
}
