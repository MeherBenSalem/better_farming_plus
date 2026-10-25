package tn.nightbeam.betterfarmingplus.forge;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import tn.nightbeam.betterfarmingplus.BetterFarmingPlus;
import tn.nightbeam.betterfarmingplus.Constants;
import tn.nightbeam.betterfarmingplus.client.FarmingCrateScreen;
import tn.nightbeam.betterfarmingplus.content.ModContent;
import tn.nightbeam.betterfarmingplus.core.FarmingConfig;

final class BetterFarmingPlusForgeClient {
    private static final String PACK_ID = Constants.MOD_ID + ":" + BetterFarmingPlus.INDICATOR_PACK;

    private BetterFarmingPlusForgeClient() {
    }

    static void init(IEventBus modBus) {
        modBus.addListener((FMLClientSetupEvent event) ->
                event.enqueueWork(() -> MenuScreens.register(ModContent.FARMING_CRATE_MENU.get(), FarmingCrateScreen::new)));
        modBus.addListener((AddPackFindersEvent event) -> {
            if (event.getPackType() != PackType.CLIENT_RESOURCES || !FarmingConfig.get().harvestIndicators()) {
                return;
            }
            var root = ModList.get().getModFileById(Constants.MOD_ID).getFile().findResource("resourcepacks/" + BetterFarmingPlus.INDICATOR_PACK);
            Pack pack = Pack.readMetaAndCreate(PACK_ID, Component.translatable("pack.better_farming_plus.harvest_indicators"), false,
                    id -> new PathPackResources(id, root, true), PackType.CLIENT_RESOURCES, Pack.Position.TOP, PackSource.BUILT_IN);
            if (pack != null) {
                event.addRepositorySource(consumer -> consumer.accept(pack));
                FirstRunPacks.enableOnce(FMLPaths.CONFIGDIR.get(), PACK_ID);
            }
        });
    }
}
