package tn.nightbeam.betterfarmingplus.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import tn.nightbeam.betterfarmingplus.block.FarmingCrateBlockEntity;
import tn.nightbeam.betterfarmingplus.content.ModContent;

/** The crate's 27 slots, then the player inventory. The data carries the harvest counter, the status and the radius. */
public class FarmingCrateMenu extends AbstractContainerMenu {
    public static final int DATA_COUNT = 4;
    public static final int SLOTS_Y = 18;
    public static final int INVENTORY_Y = 96;

    private final Container container;
    private final ContainerData data;

    public FarmingCrateMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(FarmingCrateBlockEntity.SIZE), new SimpleContainerData(DATA_COUNT));
    }

    public FarmingCrateMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(ModContent.FARMING_CRATE_MENU.get(), containerId);
        checkContainerSize(container, FarmingCrateBlockEntity.SIZE);
        checkContainerDataCount(data, DATA_COUNT);
        this.container = container;
        this.data = data;
        container.startOpen(inventory.player);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(container, col + row * 9, 8 + col * 18, SLOTS_Y + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, INVENTORY_Y + 58));
        }
        addDataSlots(data);
    }

    public int harvested() {
        return (data.get(0) & 0xFFFF) | (data.get(1) << 16);
    }

    public int status() {
        return data.get(2);
    }

    public int radius() {
        return data.get(3);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int crate = FarmingCrateBlockEntity.SIZE;
        if (index < crate) {
            if (!moveItemStackTo(stack, crate, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, crate, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
