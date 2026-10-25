package tn.nightbeam.betterfarmingplus.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/** The Minecraft-free parts of harvesting: who may harvest, how far a sweep reaches and where the drops go. */
public final class Rules {
    private Rules() {
    }

    /**
     * Whether an item matches an id/tag list such as {@code ["#minecraft:hoes", "mymod:scythe"]}. {@code tags} are the
     * item's tags without the leading {@code #}.
     */
    public static boolean matches(List<String> entries, String itemId, Collection<String> tags) {
        for (String entry : entries) {
            if (entry.startsWith("#") ? tags.contains(entry.substring(1)) : entry.equals(itemId)) {
                return true;
            }
        }
        return false;
    }

    /** May this hand harvest? {@code itemId} is null for an empty hand. */
    public static boolean mayHarvest(FarmingConfig.Harvest config, String itemId, Collection<String> tags) {
        if (!config.enabled()) {
            return false;
        }
        return itemId == null ? config.emptyHand() : matches(config.tools(), itemId, tags);
    }

    /** Sweep radius for a harvest: 0 harvests only the clicked crop. */
    public static int sweepRadius(FarmingConfig.Harvest config, String itemId, Collection<String> tags, boolean sneaking) {
        if (itemId == null || (sneaking && config.sneakForSingle()) || !matches(config.tools(), itemId, tags)) {
            return 0;
        }
        Integer radius = config.sweep().get(itemId);
        if (radius != null) {
            return radius;
        }
        for (String tag : tags) {
            Integer byTag = config.sweep().get("#" + tag);
            if (byTag != null) {
                return byTag;
            }
        }
        return 0;
    }

    /** Offsets {dx, dz} of a square of the given radius, the centre first and then ring by ring. */
    public static List<int[]> square(int radius) {
        List<int[]> offsets = new ArrayList<>();
        offsets.add(new int[] {0, 0});
        for (int ring = 1; ring <= radius; ring++) {
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dz = -ring; dz <= ring; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) == ring) {
                        offsets.add(new int[] {dx, dz});
                    }
                }
            }
        }
        return offsets;
    }

    /** A stack: what it is (compared with {@code same}), how many and how many fit in one slot. */
    public record Stack<K>(K kind, int count, int max) {
    }

    /**
     * Removes one item matching {@code isSeed} from the drops (the seed used to replant). Returns false when no drop is a
     * seed, in which case the drops are unchanged and the crop is not replanted.
     */
    public static <K> boolean takeSeed(List<Stack<K>> drops, Predicate<K> isSeed) {
        for (int i = 0; i < drops.size(); i++) {
            Stack<K> drop = drops.get(i);
            if (drop.count() > 0 && isSeed.test(drop.kind())) {
                if (drop.count() == 1) {
                    drops.remove(i);
                } else {
                    drops.set(i, new Stack<>(drop.kind(), drop.count() - 1, drop.max()));
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Whether all of {@code incoming} fits into {@code slots} (null kinds are empty slots), merging into matching stacks
     * first, as inserting would. Nothing is modified.
     */
    public static <K> boolean fits(List<Stack<K>> slots, List<Stack<K>> incoming, BiPredicate<K, K> same) {
        List<Stack<K>> sim = new ArrayList<>(slots);
        for (Stack<K> stack : incoming) {
            int left = stack.count();
            for (int i = 0; i < sim.size() && left > 0; i++) {
                Stack<K> slot = sim.get(i);
                if (slot.kind() != null && slot.count() > 0 && same.test(slot.kind(), stack.kind())) {
                    int move = Math.min(left, Math.min(slot.max(), stack.max()) - slot.count());
                    if (move > 0) {
                        sim.set(i, new Stack<>(slot.kind(), slot.count() + move, slot.max()));
                        left -= move;
                    }
                }
            }
            for (int i = 0; i < sim.size() && left > 0; i++) {
                Stack<K> slot = sim.get(i);
                if (slot.kind() == null || slot.count() <= 0) {
                    int move = Math.min(left, stack.max());
                    sim.set(i, new Stack<>(stack.kind(), move, stack.max()));
                    left -= move;
                }
            }
            if (left > 0) {
                return false;
            }
        }
        return true;
    }
}
