package tn.nightbeam.betterfarmingplus.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import tn.nightbeam.betterfarmingplus.Constants;

/**
 * {@code config/better_farming_plus.json}. Read once at startup. On first start the 0.5.0 file
 * ({@code config/betterfarming/global.json}) is migrated: its tool list, trampling switch and indicator switch carry over.
 */
public final class FarmingConfig {
    public static final String FILE = "better_farming_plus.json";
    public static final String LEGACY_FILE = "betterfarming/global.json";
    public static final String HOES = "#minecraft:hoes";

    private static final List<String> VANILLA_HOES = List.of("minecraft:wooden_hoe", "minecraft:stone_hoe", "minecraft:iron_hoe",
            "minecraft:golden_hoe", "minecraft:diamond_hoe", "minecraft:netherite_hoe");

    private static volatile Values values = Values.defaults();

    private FarmingConfig() {
    }

    /** Who may trample farmland: nobody, only those not holding a harvest tool (0.5.0 behaviour), or as vanilla. */
    public enum Trampling {
        NEVER, WITH_TOOL, VANILLA;

        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static Trampling byId(String id, Trampling fallback) {
            for (Trampling mode : values()) {
                if (mode.id().equalsIgnoreCase(id)) {
                    return mode;
                }
            }
            return fallback;
        }
    }

    public record Harvest(boolean enabled, boolean emptyHand, List<String> tools, Map<String, Integer> sweep, boolean sneakForSingle,
                          int toolDamage) {
    }

    public record Crate(int radius, int intervalTicks) {
    }

    public record Sprinkler(int radius, int intervalTicks, double growthChance, boolean needsWater, boolean hydrateFarmland) {
    }

    public record Values(Harvest harvest, Trampling trampling, Crate crate, Sprinkler sprinkler, boolean harvestIndicators) {
        public static Values defaults() {
            return read(spec().defaults());
        }
    }

    public static CommentedJson spec() {
        JsonObject sweep = Json.obj("minecraft:iron_hoe", 1, "minecraft:golden_hoe", 1, "minecraft:diamond_hoe", 2, "minecraft:netherite_hoe", 3);
        JsonObject root = Json.obj(
                "harvest", Json.obj("enabled", true, "empty_hand", true, "tools", Json.arr(HOES), "sweep_radius", sweep,
                        "sneak_for_single", true, "tool_damage", 1),
                "trampling", Trampling.NEVER.id(),
                "farming_crate", Json.obj("radius", 4, "interval_ticks", 20),
                "sprinkler", Json.obj("radius", 4, "interval_ticks", 100, "growth_chance", 0.15, "needs_water", true, "hydrate_farmland", true),
                "harvest_indicators", true);
        return new CommentedJson(root)
                .header("""
                        Better Farming++. Changes apply after a restart.
                        Item and block lists accept ids (minecraft:iron_hoe) and tags (#minecraft:hoes).""")
                .comment("harvest", "Right-click a ripe crop to harvest it and replant it with a seed from its drops.")
                .comment("harvest.empty_hand", "Harvest with an empty hand (one crop at a time).")
                .comment("harvest.tools", "Items that harvest. Tools also sweep when listed in sweep_radius.")
                .comment("harvest.sweep_radius", """
                        Area harvested by a tool: 1 is 3x3, 2 is 5x5, 3 is 7x7. Tools not listed harvest one crop.""")
                .comment("harvest.sneak_for_single", "Sneak while harvesting to only take the crop you clicked.")
                .comment("harvest.tool_damage", "Durability a tool loses per crop harvested (0 to disable).")
                .comment("trampling", """
                        "never": no player or mob tramples farmland. "with_tool": only someone holding a harvest tool
                        is stopped (the 0.5.0 behaviour). "vanilla": trampling as usual.""")
                .comment("farming_crate", """
                        The crate harvests ripe crops in a square of this radius around it (4 = 9x9), on its own layer
                        and one above and below, every interval_ticks (20 = one second).""")
                .comment("sprinkler", """
                        Every interval_ticks the sprinkler waters the farmland in a square of this radius around it and
                        gives each growing crop there growth_chance (0 to 1) of an extra growth tick. With needs_water it
                        only works with water up to 4 blocks straight below it (hidden under the field) or beside it.""")
                .comment("harvest_indicators", """
                        Add the "Harvest Indicators" resource pack (a marker above ripe crops). It can also be turned off
                        in the Resource Packs screen.""");
    }

    public static Values read(JsonObject root) {
        List<String> tools = Json.strings(root, "harvest.tools");
        Map<String, Integer> sweep = new LinkedHashMap<>();
        Json.numbers(root, "harvest.sweep_radius").forEach((id, radius) -> sweep.put(id, clamp((int) Math.round(radius), 0, 8)));
        Harvest harvest = new Harvest(Json.bool(root, "harvest.enabled", true), Json.bool(root, "harvest.empty_hand", true),
                List.copyOf(tools), Map.copyOf(sweep), Json.bool(root, "harvest.sneak_for_single", true),
                clamp(Json.integer(root, "harvest.tool_damage", 1), 0, 64));
        Crate crate = new Crate(clamp(Json.integer(root, "farming_crate.radius", 4), 0, 8),
                clamp(Json.integer(root, "farming_crate.interval_ticks", 20), 1, 1200));
        double chance = Json.num(root, "sprinkler.growth_chance", 0.15);
        Sprinkler sprinkler = new Sprinkler(clamp(Json.integer(root, "sprinkler.radius", 4), 0, 8),
                clamp(Json.integer(root, "sprinkler.interval_ticks", 100), 1, 1200),
                Double.isNaN(chance) ? 0.15 : Math.max(0.0, Math.min(1.0, chance)),
                Json.bool(root, "sprinkler.needs_water", true), Json.bool(root, "sprinkler.hydrate_farmland", true));
        return new Values(harvest, Trampling.byId(Json.str(root, "trampling", "never"), Trampling.NEVER), crate, sprinkler,
                Json.bool(root, "harvest_indicators", true));
    }

    /**
     * The 0.5.0 values as a partial config: {@code hoe_harvest_whitelist} becomes the tool list (the six vanilla hoes are
     * covered by {@code #minecraft:hoes}), {@code trampling_protection: false} keeps vanilla trampling and
     * {@code show_harvest_indicators} carries over.
     */
    public static JsonObject migrate(JsonObject legacy) {
        JsonObject out = new JsonObject();
        if (legacy.has("hoe_harvest_whitelist")) {
            List<String> tools = new ArrayList<>(List.of(HOES));
            for (String id : Json.strings(legacy, "hoe_harvest_whitelist")) {
                if (!VANILLA_HOES.contains(id) && !tools.contains(id)) {
                    tools.add(id);
                }
            }
            out.add("harvest", Json.obj("tools", Json.arr(tools.toArray())));
        }
        if (legacy.has("trampling_protection") && !Json.bool(legacy, "trampling_protection", true)) {
            out.addProperty("trampling", Trampling.VANILLA.id());
        }
        if (legacy.has("show_harvest_indicators")) {
            out.addProperty("harvest_indicators", Json.bool(legacy, "show_harvest_indicators", true));
        }
        return out;
    }

    public static void load(Path configDir) {
        Path file = configDir.resolve(FILE);
        Path legacy = configDir.resolve(LEGACY_FILE);
        if (!Files.exists(file) && Files.exists(legacy)) {
            try {
                JsonElement parsed = CommentedJson.parse(Files.readString(legacy, StandardCharsets.UTF_8));
                if (parsed != null && parsed.isJsonObject()) {
                    CommentedJson spec = spec();
                    Files.createDirectories(file.getParent());
                    Files.writeString(file, spec.write(CommentedJson.mergeOver(spec.defaults(), migrate(parsed.getAsJsonObject()))),
                            StandardCharsets.UTF_8);
                    Constants.LOG.info("Migrated {} to {}", legacy, file);
                }
            } catch (IOException | RuntimeException e) {
                Constants.LOG.warn("Could not migrate {}: {}", legacy, e.getMessage());
            }
        }
        values = read(spec().load(file));
    }

    public static Values get() {
        return values;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
