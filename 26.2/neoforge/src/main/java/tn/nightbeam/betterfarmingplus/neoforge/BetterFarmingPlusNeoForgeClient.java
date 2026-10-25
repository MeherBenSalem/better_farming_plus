package tn.nightbeam.betterfarmingplus.neoforge;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import tn.nightbeam.betterfarmingplus.BetterFarmingPlus;
import tn.nightbeam.betterfarmingplus.Constants;
import tn.nightbeam.betterfarmingplus.client.FarmingCrateScreen;
import tn.nightbeam.betterfarmingplus.content.ModContent;
import tn.nightbeam.betterfarmingplus.core.FarmingConfig;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public final class BetterFarmingPlusNeoForgeClient {
    public BetterFarmingPlusNeoForgeClient(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> event.register(ModContent.FARMING_CRATE_MENU.get(), FarmingCrateScreen::new));
        modBus.addListener((AddPackFindersEvent event) -> {
            if (event.getPackType() == PackType.CLIENT_RESOURCES && FarmingConfig.get().harvestIndicators()) {
                Identifier location = BetterFarmingPlus.id("resourcepacks/" + BetterFarmingPlus.INDICATOR_PACK);
                event.addPackFinders(location, PackType.CLIENT_RESOURCES, Component.translatable("pack.better_farming_plus.harvest_indicators"),
                        PackSource.BUILT_IN, false, Pack.Position.TOP);
                FirstRunPacks.enableOnce(FMLPaths.CONFIGDIR.get(), "mod/" + location);
            }
        });
    }
}
