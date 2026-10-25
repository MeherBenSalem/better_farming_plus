package tn.nightbeam.betterfarmingplus.forge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import tn.nightbeam.betterfarmingplus.Constants;

/**
 * Forge lists a mod's built-in resource pack but leaves it off. The Harvest Indicators pack is switched on once, on
 * the first launch with the mod; turning it off in the Resource Packs screen afterwards sticks.
 */
final class FirstRunPacks {
    private static final String MARKER = Constants.MOD_ID + ".indicators-enabled";

    private FirstRunPacks() {
    }

    static void enableOnce(Path configDir, String packId) {
        Path marker = configDir.resolve(MARKER);
        Options options = Minecraft.getInstance() == null ? null : Minecraft.getInstance().options;
        if (Files.exists(marker) || options == null) {
            return;
        }
        if (!options.resourcePacks.contains(packId)) {
            options.resourcePacks.add(packId);
            options.save();
        }
        try {
            Files.createDirectories(configDir);
            Files.writeString(marker, "The Harvest Indicators resource pack was switched on once; delete this file to switch it on again.\n");
        } catch (IOException e) {
            Constants.LOG.warn("Could not write {}: {}", marker, e.getMessage());
        }
    }
}
