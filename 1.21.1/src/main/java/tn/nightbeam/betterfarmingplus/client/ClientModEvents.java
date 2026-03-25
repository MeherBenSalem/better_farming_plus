package tn.nightbeam.betterfarmingplus.client;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = tn.nightbeam.betterfarmingplus.BetterFarmingPlusMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    public static boolean showHarvestIndicators = true;

    private static final ResourceLocation[] INDICATOR_MODELS = {
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/wheat_stage7"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/carrots_stage3"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/potatoes_stage3"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/beetroots_stage3"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/nether_wart_stage2"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/sweet_berry_bush_stage3"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/melon_stem_fruit_north"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/melon_stem_fruit_east"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/melon_stem_fruit_south"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/melon_stem_fruit_west"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/pumpkin_stem_fruit_north"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/pumpkin_stem_fruit_east"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/pumpkin_stem_fruit_south"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/pumpkin_stem_fruit_west"),
    };

    @SubscribeEvent
    public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
        showHarvestIndicators = JaumlConfigLib.getBooleanValue("betterfarming", "global", "show_harvest_indicators");

        if (!showHarvestIndicators) {
            Set<String> indicatorModelIds = new HashSet<>();
            for (ResourceLocation loc : INDICATOR_MODELS) {
                indicatorModelIds.add(loc.toString());
            }

            Map<?, BakedModel> models = event.getModels();
            for (Map.Entry<?, BakedModel> entry : models.entrySet()) {
                String key = String.valueOf(entry.getKey());
                int variantSeparator = key.indexOf('#');
                String modelId = variantSeparator >= 0 ? key.substring(0, variantSeparator) : key;
                if (indicatorModelIds.contains(modelId)) {
                    BakedModel existing = entry.getValue();
                    if (existing != null && !(existing instanceof HarvestIndicatorModel)) {
                        entry.setValue(new HarvestIndicatorModel(existing));
                    }
                }
            }
        }
    }
}
