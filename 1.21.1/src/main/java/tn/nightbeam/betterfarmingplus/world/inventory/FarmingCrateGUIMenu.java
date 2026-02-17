package tn.nightbeam.betterfarmingplus.world.inventory;

import tn.nightbeam.betterfarmingplus.init.BetterFarmingPlusModBlocks;
import tn.nightbeam.betterfarmingplus.init.BetterFarmingPlusModMenus;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

import java.util.function.Supplier;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class FarmingCrateGUIMenu extends AbstractContainerMenu implements BetterFarmingPlusModMenus.MenuAccessor {
	public final Map<String, Object> menuState = new HashMap<>() {
		@Override
		public Object put(String key, Object value) {
			if (!this.containsKey(key) && this.size() >= 27)
				return null;
			return super.put(key, value);
		}
	};
	public final Level world;
	public final Player entity;
	public int x, y, z;
	private ContainerLevelAccess access = ContainerLevelAccess.NULL;
	private Container internal;
	private final Map<Integer, Slot> customSlots = new HashMap<>();
	private BlockEntity boundBlockEntity = null;

	public FarmingCrateGUIMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
		super(BetterFarmingPlusModMenus.FARMING_CRATE_GUI.get(), id);
		this.entity = inv.player;
		this.world = inv.player.level();
		this.internal = new SimpleContainer(27);
		BlockPos pos = null;
		if (extraData != null) {
			pos = extraData.readBlockPos();
			this.x = pos.getX();
			this.y = pos.getY();
			this.z = pos.getZ();
			access = ContainerLevelAccess.create(world, pos);
		}
		if (pos != null) {
			boundBlockEntity = this.world.getBlockEntity(pos);
			if (boundBlockEntity instanceof Container container)
				this.internal = container;
		}
		this.customSlots.put(0, this.addSlot(new Slot(internal, 0, 8, 11) {
			private final int slot = 0;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(1, this.addSlot(new Slot(internal, 1, 26, 11) {
			private final int slot = 1;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(2, this.addSlot(new Slot(internal, 2, 44, 11) {
			private final int slot = 2;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(3, this.addSlot(new Slot(internal, 3, 62, 11) {
			private final int slot = 3;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(4, this.addSlot(new Slot(internal, 4, 80, 11) {
			private final int slot = 4;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(5, this.addSlot(new Slot(internal, 5, 98, 11) {
			private final int slot = 5;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(6, this.addSlot(new Slot(internal, 6, 116, 11) {
			private final int slot = 6;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(7, this.addSlot(new Slot(internal, 7, 134, 11) {
			private final int slot = 7;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(8, this.addSlot(new Slot(internal, 8, 152, 11) {
			private final int slot = 8;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(9, this.addSlot(new Slot(internal, 9, 8, 29) {
			private final int slot = 9;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(10, this.addSlot(new Slot(internal, 10, 26, 29) {
			private final int slot = 10;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(11, this.addSlot(new Slot(internal, 11, 44, 29) {
			private final int slot = 11;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(12, this.addSlot(new Slot(internal, 12, 62, 29) {
			private final int slot = 12;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(13, this.addSlot(new Slot(internal, 13, 80, 29) {
			private final int slot = 13;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(14, this.addSlot(new Slot(internal, 14, 98, 29) {
			private final int slot = 14;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(15, this.addSlot(new Slot(internal, 15, 116, 29) {
			private final int slot = 15;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(16, this.addSlot(new Slot(internal, 16, 134, 29) {
			private final int slot = 16;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(17, this.addSlot(new Slot(internal, 17, 152, 29) {
			private final int slot = 17;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(18, this.addSlot(new Slot(internal, 18, 8, 47) {
			private final int slot = 18;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(19, this.addSlot(new Slot(internal, 19, 26, 47) {
			private final int slot = 19;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(20, this.addSlot(new Slot(internal, 20, 44, 47) {
			private final int slot = 20;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(21, this.addSlot(new Slot(internal, 21, 62, 47) {
			private final int slot = 21;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(22, this.addSlot(new Slot(internal, 22, 80, 47) {
			private final int slot = 22;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(23, this.addSlot(new Slot(internal, 23, 98, 47) {
			private final int slot = 23;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(24, this.addSlot(new Slot(internal, 24, 116, 47) {
			private final int slot = 24;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(25, this.addSlot(new Slot(internal, 25, 134, 47) {
			private final int slot = 25;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		this.customSlots.put(26, this.addSlot(new Slot(internal, 26, 152, 47) {
			private final int slot = 26;
			private int x = FarmingCrateGUIMenu.this.x;
			private int y = FarmingCrateGUIMenu.this.y;
		}));
		for (int si = 0; si < 3; ++si)
			for (int sj = 0; sj < 9; ++sj)
				this.addSlot(new Slot(inv, sj + (si + 1) * 9, 0 + 8 + sj * 18, -10 + 84 + si * 18));
		for (int si = 0; si < 9; ++si)
			this.addSlot(new Slot(inv, si, 0 + 8 + si * 18, -10 + 142));
	}

	@Override
	public boolean stillValid(Player player) {
		return AbstractContainerMenu.stillValid(this.access, player, BetterFarmingPlusModBlocks.FARMING_CRATE.get());
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = (Slot) this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index < 27) {
				if (!this.moveItemStackTo(itemstack1, 27, this.slots.size(), true))
					return ItemStack.EMPTY;
				slot.onQuickCraft(itemstack1, itemstack);
			} else if (!this.moveItemStackTo(itemstack1, 0, 27, false)) {
				if (index < 27 + 27) {
					if (!this.moveItemStackTo(itemstack1, 27 + 27, this.slots.size(), true))
						return ItemStack.EMPTY;
				} else {
					if (!this.moveItemStackTo(itemstack1, 27, 27 + 27, false))
						return ItemStack.EMPTY;
				}
				return ItemStack.EMPTY;
			}
			if (itemstack1.getCount() == 0)
				slot.set(ItemStack.EMPTY);
			else
				slot.setChanged();
			if (itemstack1.getCount() == itemstack.getCount())
				return ItemStack.EMPTY;
			slot.onTake(playerIn, itemstack1);
		}
		return itemstack;
	}

	@Override
	protected boolean moveItemStackTo(ItemStack p_38904_, int p_38905_, int p_38906_, boolean p_38907_) {
		boolean flag = false;
		int i = p_38905_;
		if (p_38907_) {
			i = p_38906_ - 1;
		}
		if (p_38904_.isStackable()) {
			while (!p_38904_.isEmpty()) {
				if (p_38907_) {
					if (i < p_38905_) {
						break;
					}
				} else if (i >= p_38906_) {
					break;
				}
				Slot slot = this.slots.get(i);
				ItemStack itemstack = slot.getItem();
				if (slot.mayPlace(itemstack) && !itemstack.isEmpty() && ItemStack.isSameItemSameComponents(p_38904_, itemstack)) {
					int j = itemstack.getCount() + p_38904_.getCount();
					int maxSize = Math.min(slot.getMaxStackSize(), p_38904_.getMaxStackSize());
					if (j <= maxSize) {
						p_38904_.setCount(0);
						itemstack.setCount(j);
						slot.set(itemstack);
						flag = true;
					} else if (itemstack.getCount() < maxSize) {
						p_38904_.shrink(maxSize - itemstack.getCount());
						itemstack.setCount(maxSize);
						slot.set(itemstack);
						flag = true;
					}
				}
				if (p_38907_) {
					--i;
				} else {
					++i;
				}
			}
		}
		if (!p_38904_.isEmpty()) {
			if (p_38907_) {
				i = p_38906_ - 1;
			} else {
				i = p_38905_;
			}
			while (true) {
				if (p_38907_) {
					if (i < p_38905_) {
						break;
					}
				} else if (i >= p_38906_) {
					break;
				}
				Slot slot1 = this.slots.get(i);
				ItemStack itemstack1 = slot1.getItem();
				if (itemstack1.isEmpty() && slot1.mayPlace(p_38904_)) {
					if (p_38904_.getCount() > slot1.getMaxStackSize()) {
						slot1.setByPlayer(p_38904_.split(slot1.getMaxStackSize()));
					} else {
						slot1.setByPlayer(p_38904_.split(p_38904_.getCount()));
					}
					slot1.setChanged();
					flag = true;
					break;
				}
				if (p_38907_) {
					--i;
				} else {
					++i;
				}
			}
		}
		return flag;
	}

	@Override
	public void removed(Player playerIn) {
		super.removed(playerIn);
		if (this.internal instanceof SimpleContainer simpleContainer && playerIn instanceof ServerPlayer serverPlayer) {
			if (!serverPlayer.isAlive() || serverPlayer.hasDisconnected()) {
				for (int j = 0; j < simpleContainer.getContainerSize(); ++j)
					playerIn.drop(simpleContainer.getItem(j), false);
			} else {
				for (int i = 0; i < simpleContainer.getContainerSize(); ++i)
					playerIn.getInventory().placeItemBackInInventory(simpleContainer.getItem(i));
			}
		}
	}

	@Override
	public Map<Integer, Slot> getSlots() {
		return Collections.unmodifiableMap(customSlots);
	}

	@Override
	public Map<String, Object> getMenuState() {
		return menuState;
	}
}