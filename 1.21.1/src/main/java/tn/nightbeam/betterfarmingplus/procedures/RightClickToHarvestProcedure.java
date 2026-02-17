package tn.nightbeam.betterfarmingplus.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = tn.nightbeam.betterfarmingplus.BetterFarmingPlusMod.MODID)
public class RightClickToHarvestProcedure {
	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (event.getHand() != InteractionHand.MAIN_HAND)
			return;
		execute(event.getLevel(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), event.getEntity());
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if (JaumlConfigLib.stringExistsInArray("betterfarming", "global", "hoe_harvest_whitelist", (BuiltInRegistries.ITEM.getKey((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem()).toString()))) {
			if ((world.getBlockState(BlockPos.containing(x, y, z))).getBlock() instanceof CropBlock) {
				if (((world.getBlockState(BlockPos.containing(x, y, z))).getBlock().getStateDefinition().getProperty("age") instanceof IntegerProperty _getip6
						? (world.getBlockState(BlockPos.containing(x, y, z))).getValue(_getip6)
						: -1) == ((world.getBlockState(BlockPos.containing(x, y, z))).getBlock().getStateDefinition().getProperty("age") instanceof IntegerProperty _max8 ? _max8.getPossibleValues().stream().max(Integer::compareTo).get() : -1)) {
					if (entity instanceof LivingEntity _entity)
						_entity.swing(InteractionHand.MAIN_HAND, true);
					world.setBlock(BlockPos.containing(x, y + 1, z), (world.getBlockState(BlockPos.containing(x, y, z))), 3);
					{
						BlockPos _pos = BlockPos.containing(x, y + 1, z);
						Block.dropResources(world.getBlockState(_pos), world, BlockPos.containing(x, y + 1, z), null);
						world.destroyBlock(_pos, false);
					}
					{
						int _value = 0;
						BlockPos _pos = BlockPos.containing(x, y, z);
						BlockState _bs = world.getBlockState(_pos);
						if (_bs.getBlock().getStateDefinition().getProperty("age") instanceof IntegerProperty _integerProp && _integerProp.getPossibleValues().contains(_value))
							world.setBlock(_pos, _bs.setValue(_integerProp, _value), 3);
					}
				}
			}
		}
	}
}