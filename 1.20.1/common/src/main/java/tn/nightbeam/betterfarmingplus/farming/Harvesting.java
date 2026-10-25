package tn.nightbeam.betterfarmingplus.farming;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;
import tn.nightbeam.betterfarmingplus.content.ModTags;
import tn.nightbeam.betterfarmingplus.core.FarmingConfig;
import tn.nightbeam.betterfarmingplus.core.Rules;

/** Harvest-and-replant, shared by right-click, the hoe sweep and the Farming Crate. */
public final class Harvesting {
    private static final int BREAK_EFFECTS = 2001;

    private Harvesting() {
    }

    /** The crop's {@code age} property, or null when it has none. */
    public static @Nullable IntegerProperty age(BlockState state) {
        Property<?> property = state.getBlock().getStateDefinition().getProperty("age");
        return property instanceof IntegerProperty age ? age : null;
    }

    /** A crop that can be harvested and replanted: it is fully grown and replants from its own item. */
    public static boolean isHarvestable(BlockState state) {
        IntegerProperty age = age(state);
        if (age == null || state.is(ModTags.NOT_HARVESTABLE) || state.getBlock().asItem() == Items.AIR) {
            return false;
        }
        if (!(state.getBlock() instanceof CropBlock) && !state.is(ModTags.HARVESTABLE)) {
            return false;
        }
        int max = age.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
        return state.getValue(age) == max;
    }

    /**
     * Harvests the ripe crop at {@code pos}: computes its drops with the tool (Fortune counts), takes one seed from them
     * and replants at age 0. Returns the remaining drops, which the caller stores or drops; the world is only changed
     * when {@code accept} agrees to take them (the crate refuses when its storage is full).
     */
    public static @Nullable List<ItemStack> harvest(ServerLevel level, BlockPos pos, BlockState state, @Nullable Entity entity, ItemStack tool,
                                                    java.util.function.Predicate<List<ItemStack>> accept) {
        if (!isHarvestable(state)) {
            return null;
        }
        IntegerProperty age = age(state);
        List<ItemStack> drops = new ArrayList<>(Block.getDrops(state, level, pos, null, entity, tool));
        Item seed = state.getBlock().asItem();
        List<Rules.Stack<ItemStack>> stacks = new ArrayList<>();
        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                stacks.add(new Rules.Stack<>(drop, drop.getCount(), drop.getMaxStackSize()));
            }
        }
        boolean replant = Rules.takeSeed(stacks, stack -> stack.is(seed));
        List<ItemStack> remaining = new ArrayList<>();
        for (Rules.Stack<ItemStack> stack : stacks) {
            remaining.add(stack.kind().copyWithCount(stack.count()));
        }
        if (!accept.test(remaining)) {
            return null;
        }
        level.levelEvent(BREAK_EFFECTS, pos, Block.getId(state));
        if (replant) {
            level.setBlock(pos, state.setValue(age, 0), Block.UPDATE_ALL);
        } else {
            level.removeBlock(pos, false);
        }
        level.gameEvent(entity, GameEvent.BLOCK_CHANGE, pos);
        return remaining;
    }

    /**
     * A player's right-click on {@code pos}. Harvests the clicked crop and, with a sweeping tool, every ripe crop in the
     * square around it; drops fall where each crop stood. Returns how many crops were harvested.
     */
    public static int onUse(ServerPlayer player, InteractionHand hand, BlockPos pos) {
        if (hand != InteractionHand.MAIN_HAND || player.isSpectator()) {
            return 0;
        }
        ServerLevel level = player.serverLevel();
        BlockState clicked = level.getBlockState(pos);
        if (!isHarvestable(clicked)) {
            return 0;
        }
        FarmingConfig.Harvest config = FarmingConfig.get().harvest();
        ItemStack tool = player.getMainHandItem();
        String itemId = tool.isEmpty() ? null : itemId(tool);
        Set<String> tags = tags(tool);
        if (!Rules.mayHarvest(config, itemId, tags)) {
            return 0;
        }
        int radius = Rules.sweepRadius(config, itemId, tags, player.isShiftKeyDown());
        int harvested = 0;
        for (int[] offset : Rules.square(radius)) {
            BlockPos target = pos.offset(offset[0], 0, offset[1]);
            BlockState state = level.getBlockState(target);
            List<ItemStack> drops = harvest(level, target, state, player, tool, all -> true);
            if (drops == null) {
                continue;
            }
            drops.forEach(drop -> Block.popResource(level, target, drop));
            harvested++;
            if (!tool.isEmpty() && config.toolDamage() > 0 && tool.isDamageableItem()) {
                tool.hurtAndBreak(config.toolDamage(), player, broken -> broken.broadcastBreakEvent(EquipmentSlot.MAINHAND));
                if (tool.isEmpty()) {
                    break;
                }
            }
        }
        if (harvested > 0) {
            player.swing(hand, true);
        }
        return harvested;
    }

    public static boolean isHarvestTool(ItemStack stack) {
        return !stack.isEmpty() && Rules.matches(FarmingConfig.get().harvest().tools(), itemId(stack), tags(stack));
    }

    private static String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private static Set<String> tags(ItemStack stack) {
        return stack.getTags().map(tag -> tag.location().toString()).collect(Collectors.toSet());
    }
}
