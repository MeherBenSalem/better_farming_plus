package tn.nightbeam.betterfarmingplus.procedures;

import tn.naizo.jauml.JaumlConfigLib;
import tn.nightbeam.betterfarmingplus.platform.Services;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class RightClickHarvest {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		ItemStack held = entity instanceof LivingEntity living ? living.getMainHandItem() : ItemStack.EMPTY;
		if (JaumlConfigLib.stringExistsInArray("betterfarming", "global", "hoe_harvest_whitelist", Services.PLATFORM.getItemId(held.getItem()))) {
			if ((world.getBlockState(BlockPos.containing(x, y, z))).getBlock() instanceof CropBlock) {
				if (((world.getBlockState(BlockPos.containing(x, y, z))).getBlock().getStateDefinition().getProperty("age") instanceof IntegerProperty ageProperty
						? (world.getBlockState(BlockPos.containing(x, y, z))).getValue(ageProperty)
						: -1) == ((world.getBlockState(BlockPos.containing(x, y, z))).getBlock().getStateDefinition().getProperty("age") instanceof IntegerProperty maxProperty
								? maxProperty.getPossibleValues().stream().max(Integer::compareTo).orElse(-1)
								: -1)) {
					if (entity instanceof LivingEntity livingEntity)
						livingEntity.swing(InteractionHand.MAIN_HAND, true);
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
						if (_bs.getBlock().getStateDefinition().getProperty("age") instanceof IntegerProperty integerProp && integerProp.getPossibleValues().contains(_value))
							world.setBlock(_pos, _bs.setValue(integerProp, _value), 3);
					}
				}
			}
		}
	}
}
