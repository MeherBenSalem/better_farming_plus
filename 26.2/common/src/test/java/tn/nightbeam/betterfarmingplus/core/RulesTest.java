package tn.nightbeam.betterfarmingplus.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RulesTest {
    private static final Set<String> HOE = Set.of("minecraft:hoes");
    private static final FarmingConfig.Harvest DEFAULT = FarmingConfig.Values.defaults().harvest();

    @Test
    void emptyHandAndHoesHarvestOtherItemsDoNot() {
        assertTrue(Rules.mayHarvest(DEFAULT, null, Set.of()));
        assertTrue(Rules.mayHarvest(DEFAULT, "minecraft:wooden_hoe", HOE));
        assertTrue(Rules.mayHarvest(DEFAULT, "othermod:bronze_hoe", HOE));
        assertFalse(Rules.mayHarvest(DEFAULT, "minecraft:stick", Set.of()));
    }

    @Test
    void sweepGrowsWithTheHoe() {
        assertEquals(0, Rules.sweepRadius(DEFAULT, "minecraft:wooden_hoe", HOE, false));
        assertEquals(0, Rules.sweepRadius(DEFAULT, "minecraft:stone_hoe", HOE, false));
        assertEquals(1, Rules.sweepRadius(DEFAULT, "minecraft:iron_hoe", HOE, false));
        assertEquals(1, Rules.sweepRadius(DEFAULT, "minecraft:golden_hoe", HOE, false));
        assertEquals(2, Rules.sweepRadius(DEFAULT, "minecraft:diamond_hoe", HOE, false));
        assertEquals(3, Rules.sweepRadius(DEFAULT, "minecraft:netherite_hoe", HOE, false));
        assertEquals(0, Rules.sweepRadius(DEFAULT, null, Set.of(), false));
    }

    @Test
    void sneakingTakesOneCrop() {
        assertEquals(0, Rules.sweepRadius(DEFAULT, "minecraft:netherite_hoe", HOE, true));
    }

    @Test
    void sweepByTag() {
        FarmingConfig.Harvest config = FarmingConfig.read(com.google.gson.JsonParser.parseString(
                "{\"harvest\": {\"tools\": [\"#c:tools/scythes\"], \"sweep_radius\": {\"#c:tools/scythes\": 4}}}").getAsJsonObject()).harvest();
        assertEquals(4, Rules.sweepRadius(config, "othermod:scythe", Set.of("c:tools/scythes"), false));
        assertFalse(Rules.mayHarvest(config, "minecraft:iron_hoe", HOE));
    }

    @Test
    void squareIsCentredAndComplete() {
        List<int[]> offsets = Rules.square(4);
        assertEquals(81, offsets.size());
        assertEquals(0, offsets.get(0)[0]);
        assertEquals(0, offsets.get(0)[1]);
        Set<String> seen = new HashSet<>();
        for (int[] o : offsets) {
            assertTrue(Math.abs(o[0]) <= 4 && Math.abs(o[1]) <= 4);
            assertTrue(seen.add(o[0] + "," + o[1]));
        }
        assertEquals(1, Rules.square(0).size());
        assertEquals(9, Rules.square(1).size());
    }

    @Test
    void replantTakesExactlyOneSeed() {
        List<Rules.Stack<String>> drops = new ArrayList<>(List.of(new Rules.Stack<>("wheat", 1, 64), new Rules.Stack<>("wheat_seeds", 3, 64)));
        assertTrue(Rules.takeSeed(drops, "wheat_seeds"::equals));
        assertEquals(2, drops.get(1).count());
        List<Rules.Stack<String>> single = new ArrayList<>(List.of(new Rules.Stack<>("carrot", 1, 64)));
        assertTrue(Rules.takeSeed(single, "carrot"::equals));
        assertTrue(single.isEmpty());
    }

    @Test
    void noSeedNoReplant() {
        List<Rules.Stack<String>> drops = new ArrayList<>(List.of(new Rules.Stack<>("wheat", 1, 64)));
        assertFalse(Rules.takeSeed(drops, "wheat_seeds"::equals));
        assertEquals(1, drops.size());
    }

    @Test
    void fitsMergesThenUsesEmptySlots() {
        List<Rules.Stack<String>> slots = new ArrayList<>(List.of(new Rules.Stack<>("wheat", 63, 64), new Rules.Stack<>(null, 0, 64)));
        assertTrue(Rules.fits(slots, List.of(new Rules.Stack<>("wheat", 1, 64), new Rules.Stack<>("seeds", 64, 64)), Objects::equals));
        assertFalse(Rules.fits(slots, List.of(new Rules.Stack<>("wheat", 2, 64), new Rules.Stack<>("seeds", 64, 64)), Objects::equals));
        assertTrue(Rules.fits(slots, List.of(new Rules.Stack<>("wheat", 65, 64)), Objects::equals));
        assertEquals(63, slots.get(0).count(), "fits() must not modify the slots");
    }

    @Test
    void fullStorageRefuses() {
        List<Rules.Stack<String>> slots = List.of(new Rules.Stack<>("stone", 64, 64));
        assertFalse(Rules.fits(slots, List.of(new Rules.Stack<>("wheat", 1, 64)), Objects::equals));
        assertTrue(Rules.fits(slots, List.of(), Objects::equals));
    }
}
