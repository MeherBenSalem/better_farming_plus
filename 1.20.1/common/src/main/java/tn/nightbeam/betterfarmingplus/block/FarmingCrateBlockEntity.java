package tn.nightbeam.betterfarmingplus.block;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tn.nightbeam.betterfarmingplus.content.ModContent;
import tn.nightbeam.betterfarmingplus.core.FarmingConfig;
import tn.nightbeam.betterfarmingplus.core.Rules;
import tn.nightbeam.betterfarmingplus.farming.Harvesting;
import tn.nightbeam.betterfarmingplus.menu.FarmingCrateMenu;

/**
 * Harvests the ripe crops around it, replants them and stores the drops. A crop is only harvested when all of its
 * drops fit, so a full crate leaves the field alone instead of losing items. Hoppers can take from every face; nothing
 * is pushed in.
 */
public class FarmingCrateBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int SIZE = 27;
    public static final int STATUS_IDLE = 0;
    public static final int STATUS_WORKING = 1;
    public static final int STATUS_FULL = 2;
    private static final int[] SLOTS = IntStream.range(0, SIZE).toArray();

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private int harvested;
    private int status = STATUS_IDLE;

    /** Synced to the open menu: harvested count (low and high 16 bits), status, radius. */
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> harvested & 0xFFFF;
                case 1 -> harvested >>> 16;
                case 2 -> status;
                case 3 -> FarmingConfig.get().crate().radius();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return FarmingCrateMenu.DATA_COUNT;
        }
    };

    public FarmingCrateBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.FARMING_CRATE_ENTITY.get(), pos, state);
    }

    public int harvested() {
        return harvested;
    }

    public int status() {
        return status;
    }

    public void serverTick(ServerLevel level, BlockPos pos) {
        FarmingConfig.Crate config = FarmingConfig.get().crate();
        if (Math.floorMod(level.getGameTime() + pos.asLong(), config.intervalTicks()) != 0) {
            return;
        }
        int before = harvested;
        boolean full = false;
        for (int[] offset : Rules.square(config.radius())) {
            for (int dy = -1; dy <= 1; dy++) {
                BlockPos target = pos.offset(offset[0], dy, offset[1]);
                if (target.equals(pos)) {
                    continue;
                }
                BlockState state = level.getBlockState(target);
                if (!Harvesting.isHarvestable(state)) {
                    continue;
                }
                List<ItemStack> drops = Harvesting.harvest(level, target, state, null, ItemStack.EMPTY, this::fits);
                if (drops == null) {
                    full = true;
                    continue;
                }
                drops.forEach(this::insert);
                harvested++;
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5,
                        4, 0.3, 0.3, 0.3, 0.0);
            }
        }
        status = harvested > before ? STATUS_WORKING : full ? STATUS_FULL : STATUS_IDLE;
        if (harvested != before) {
            setChanged();
        }
    }

    private boolean fits(List<ItemStack> drops) {
        List<Rules.Stack<ItemStack>> slots = new ArrayList<>(SIZE);
        for (ItemStack stack : items) {
            slots.add(stack.isEmpty() ? new Rules.Stack<>(null, 0, 64) : new Rules.Stack<>(stack, stack.getCount(), stack.getMaxStackSize()));
        }
        List<Rules.Stack<ItemStack>> incoming = new ArrayList<>();
        for (ItemStack drop : drops) {
            incoming.add(new Rules.Stack<>(drop, drop.getCount(), drop.getMaxStackSize()));
        }
        return Rules.fits(slots, incoming, ItemStack::isSameItemSameTags);
    }

    private void insert(ItemStack drop) {
        ItemStack remaining = drop.copy();
        for (int slot = 0; slot < SIZE && !remaining.isEmpty(); slot++) {
            ItemStack existing = items.get(slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameTags(existing, remaining)) {
                int move = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
                if (move > 0) {
                    existing.grow(move);
                    remaining.shrink(move);
                }
            }
        }
        for (int slot = 0; slot < SIZE && !remaining.isEmpty(); slot++) {
            if (items.get(slot).isEmpty()) {
                items.set(slot, remaining.split(Math.min(remaining.getCount(), remaining.getMaxStackSize())));
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
        harvested = tag.getInt("Harvested");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("Harvested", harvested);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.better_farming_plus.farming_crate");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new FarmingCrateMenu(containerId, inventory, this, data);
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return true;
    }
}
