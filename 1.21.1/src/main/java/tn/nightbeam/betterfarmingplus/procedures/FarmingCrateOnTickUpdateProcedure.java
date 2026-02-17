package tn.nightbeam.betterfarmingplus.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Comparator;

public class FarmingCrateOnTickUpdateProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		boolean found = false;
		double sx = 0;
		double sy = 0;
		double sz = 0;
		sx = -3;
		found = false;
		for (int index0 = 0; index0 < 6; index0++) {
			sz = -3;
			for (int index1 = 0; index1 < 6; index1++) {
				if ((world.getBlockState(BlockPos.containing(x + sx, y, z + sz))).getBlock() instanceof CropBlock) {
					if (((world.getBlockState(BlockPos.containing(x + sx, y, z + sz))).getBlock().getStateDefinition().getProperty("age") instanceof IntegerProperty _getip3
							? (world.getBlockState(BlockPos.containing(x + sx, y, z + sz))).getValue(_getip3)
							: -1) == ((world.getBlockState(BlockPos.containing(x + sx, y, z + sz))).getBlock().getStateDefinition().getProperty("age") instanceof IntegerProperty _max5
									? _max5.getPossibleValues().stream().max(Integer::compareTo).get()
									: -1)) {
						world.setBlock(BlockPos.containing(x + sx, y + 1, z + sz), (world.getBlockState(BlockPos.containing(x + sx, y, z + sz))), 3);
						{
							BlockPos _pos = BlockPos.containing(x + sx, y + 1, z + sz);
							Block.dropResources(world.getBlockState(_pos), world, BlockPos.containing(x + sx, y + 1, z + sz), null);
							world.destroyBlock(_pos, false);
						}
						{
							int _value = 0;
							BlockPos _pos = BlockPos.containing(x + sx, y, z + sz);
							BlockState _bs = world.getBlockState(_pos);
							if (_bs.getBlock().getStateDefinition().getProperty("age") instanceof IntegerProperty _integerProp && _integerProp.getPossibleValues().contains(_value))
								world.setBlock(_pos, _bs.setValue(_integerProp, _value), 3);
						}
						{
							final Vec3 _center = new Vec3(x, y, z);
							List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(6 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
							for (Entity entityiterator : _entfound) {
								if (entityiterator instanceof ItemEntity) {
									if (!entityiterator.level().isClientSide())
										entityiterator.discard();
									int insertCount = 1;
									// (entityiterator instanceof ItemEntity _itemEnt ? _itemEnt.getItem() : ItemStack.EMPTY) is already an ItemStack
									ItemStack stackToInsert = (entityiterator instanceof ItemEntity _itemEnt ? _itemEnt.getItem() : ItemStack.EMPTY).copy(); // make a copy to avoid modifying original
									stackToInsert.setCount(1); // we handle count separately
									BlockPos pos = new BlockPos((int) (x), (int) (y), (int) (z));
									BlockEntity entity = world.getBlockEntity(pos);
									if (entity instanceof BaseContainerBlockEntity blockInv) {
										int size = blockInv.getContainerSize();
										for (int slot = 0; slot < size && insertCount > 0; slot++) {
											ItemStack slotStack = blockInv.getItem(slot);
											// CASE 1: Empty slot → add items
											if (slotStack.isEmpty()) {
												int amount = Math.min(insertCount, stackToInsert.getMaxStackSize());
												ItemStack toInsert = stackToInsert.copy();
												toInsert.setCount(amount);
												blockInv.setItem(slot, toInsert);
												insertCount -= amount;
												continue; // move to next slot
											}
											// CASE 2: Same item + space available → merge
											if (ItemStack.isSameItemSameComponents(slotStack, stackToInsert) && slotStack.getCount() < slotStack.getMaxStackSize()) {
												int room = slotStack.getMaxStackSize() - slotStack.getCount();
												int amountToAdd = Math.min(room, insertCount);
												slotStack.grow(amountToAdd);
												insertCount -= amountToAdd;
											}
										}
										// Mark the block entity as changed
										blockInv.setChanged();
										if (world instanceof net.minecraft.world.level.Level _level)
											_level.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
									}
								}
							}
						}
					}
				}
				sz = sz + 1;
			}
			sx = sx + 1;
		}
	}
}