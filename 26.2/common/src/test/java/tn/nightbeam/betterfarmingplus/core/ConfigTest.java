package tn.nightbeam.betterfarmingplus.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigTest {
    @Test
    void defaults() {
        FarmingConfig.Values v = FarmingConfig.Values.defaults();
        assertTrue(v.harvest().enabled());
        assertTrue(v.harvest().emptyHand());
        assertEquals(List.of("#minecraft:hoes"), v.harvest().tools());
        assertEquals(3, v.harvest().sweep().get("minecraft:netherite_hoe"));
        assertEquals(1, v.harvest().toolDamage());
        assertEquals(FarmingConfig.Trampling.NEVER, v.trampling());
        assertEquals(4, v.crate().radius());
        assertEquals(20, v.crate().intervalTicks());
        assertEquals(4, v.sprinkler().radius());
        assertEquals(100, v.sprinkler().intervalTicks());
        assertEquals(0.15, v.sprinkler().growthChance());
        assertTrue(v.sprinkler().needsWater());
        assertTrue(v.harvestIndicators());
    }

    @Test
    void badValuesAreClampedOrIgnored() {
        JsonObject root = (JsonObject) CommentedJson.parse("""
                // a comment
                {"trampling": "sometimes", "farming_crate": {"radius": 99, "interval_ticks": 0},
                 "sprinkler": {"growth_chance": 3, "radius": "far"}, "harvest": {"tool_damage": -5}}""");
        FarmingConfig.Values v = FarmingConfig.read(root);
        assertEquals(FarmingConfig.Trampling.NEVER, v.trampling());
        assertEquals(8, v.crate().radius());
        assertEquals(1, v.crate().intervalTicks());
        assertEquals(1.0, v.sprinkler().growthChance());
        assertEquals(4, v.sprinkler().radius());
        assertEquals(0, v.harvest().toolDamage());
    }

    @Test
    void legacyValuesMigrate() {
        JsonObject legacy = (JsonObject) CommentedJson.parse("""
                {"hoe_harvest_whitelist": ["minecraft:wooden_hoe", "minecraft:iron_hoe", "othermod:scythe"],
                 "trampling_protection": false, "show_harvest_indicators": false}""");
        FarmingConfig.Values v = FarmingConfig.read(CommentedJson.mergeOver(FarmingConfig.spec().defaults(), FarmingConfig.migrate(legacy)));
        assertEquals(List.of("#minecraft:hoes", "othermod:scythe"), v.harvest().tools());
        assertEquals(FarmingConfig.Trampling.VANILLA, v.trampling());
        assertFalse(v.harvestIndicators());
        assertEquals(2, v.harvest().sweep().get("minecraft:diamond_hoe"));
    }

    @Test
    void legacyProtectionOnBecomesNever() {
        JsonObject legacy = (JsonObject) CommentedJson.parse("{\"trampling_protection\": true}");
        assertFalse(FarmingConfig.migrate(legacy).has("trampling"));
    }

    @Test
    void loadMigratesTheOldFileOnce(@TempDir Path dir) throws IOException {
        Files.createDirectories(dir.resolve("betterfarming"));
        Files.writeString(dir.resolve(FarmingConfig.LEGACY_FILE), "{\"show_harvest_indicators\": false}");
        FarmingConfig.load(dir);
        assertFalse(FarmingConfig.get().harvestIndicators());
        String text = Files.readString(dir.resolve(FarmingConfig.FILE));
        assertTrue(text.startsWith("// Better Farming++."));
        assertTrue(text.contains("\"harvest_indicators\": false"));
        Files.writeString(dir.resolve(FarmingConfig.LEGACY_FILE), "{\"show_harvest_indicators\": true}");
        FarmingConfig.load(dir);
        assertFalse(FarmingConfig.get().harvestIndicators(), "the new file wins once it exists");
        assertEquals(text, Files.readString(dir.resolve(FarmingConfig.FILE)));
    }

    @Test
    void brokenFileFallsBackToDefaultsAndIsLeftAlone(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve(FarmingConfig.FILE), "{ not json");
        FarmingConfig.load(dir);
        assertEquals("{ not json", Files.readString(dir.resolve(FarmingConfig.FILE)));
        assertEquals(4, FarmingConfig.get().crate().radius());
    }
}
