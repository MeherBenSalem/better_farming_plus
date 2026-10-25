package tn.nightbeam.betterfarmingplus.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;
import tn.nightbeam.betterfarmingplus.BetterFarmingPlus;
import tn.nightbeam.betterfarmingplus.Constants;

/**
 * Makes sure the Harvest Indicators pack ends up switched on after the first launch with the mod. Fabric enables a
 * default pack in memory and remembers it offered it, but nothing saves options.txt, so a game closed before the
 * options are saved would come back without the pack. This selects it if needed and saves the options, once; turning
 * the pack off in the Resource Packs screen afterwards sticks.
 */
public final class IndicatorPack {
    /** The pack's id on Fabric. */
    public static final String FABRIC_ID = Constants.MOD_ID + ":" + BetterFarmingPlus.INDICATOR_PACK;
    private static final String MARKER = Constants.MOD_ID + ".indicators-enabled";

    private IndicatorPack() {
    }

    public static void selectOnce(Minecraft minecraft, Path configDir, String packId) {
        Path marker = configDir.resolve(MARKER);
        PackRepository packs = minecraft.getResourcePackRepository();
        if (Files.exists(marker) || !packs.getAvailableIds().contains(packId)) {
            return;
        }
        List<String> selected = new ArrayList<>(packs.getSelectedIds());
        if (!selected.contains(packId)) {
            selected.add(packId);
            packs.setSelected(selected);
        }
        // Saves options.txt; reloads the resources only if the selection changed.
        minecraft.options.updateResourcePacks(packs);
        try {
            Files.createDirectories(configDir);
            Files.writeString(marker, "The Harvest Indicators resource pack was switched on once; delete this file to switch it on again.\n");
        } catch (IOException e) {
            Constants.LOG.warn("Could not write {}: {}", marker, e.getMessage());
        }
    }
}
