package tn.nightbeam.betterfarmingplus.platform;

import java.nio.file.Path;

/** What common code needs from the loader. Each loader passes an implementation to {@code BetterFarmingPlus.init}. */
public interface Platform {
    Path configDir();
}
