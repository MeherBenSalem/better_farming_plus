package tn.nightbeam.betterfarmingplus.neoforge;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import tn.nightbeam.betterfarmingplus.BetterFarmingPlus;
import tn.nightbeam.betterfarmingplus.Constants;
import tn.nightbeam.betterfarmingplus.content.ModContent;
import tn.nightbeam.betterfarmingplus.farming.Harvesting;
import tn.nightbeam.betterfarmingplus.farming.Trampling;
import tn.nightbeam.betterfarmingplus.platform.Registrar;

@Mod(Constants.MOD_ID)
public final class BetterFarmingPlusNeoForge {
    private static final ResourceKey<net.minecraft.world.item.CreativeModeTab> FUNCTIONAL_BLOCKS =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("functional_blocks"));

    private final Map<ResourceKey<?>, DeferredRegister<?>> registers = new HashMap<>();

    public BetterFarmingPlusNeoForge(IEventBus modBus) {
        BetterFarmingPlus.init(FMLPaths.CONFIGDIR::get, new Registrar() {
            @Override
            public <R, T extends R> Supplier<T> register(ResourceKey<? extends Registry<R>> registry, String path, Function<Identifier, T> factory) {
                return deferred(registry).register(path, factory);
            }
        }, new ModContent.MenuTypeFactory() {
            @Override
            public <T extends AbstractContainerMenu> MenuType<T> create(ModContent.MenuConstructor<T> constructor) {
                return new MenuType<>(constructor::create, FeatureFlags.VANILLA_SET);
            }
        });
        registers.values().forEach(deferred -> deferred.register(modBus));
        modBus.addListener((BuildCreativeModeTabContentsEvent event) -> {
            if (event.getTabKey() == FUNCTIONAL_BLOCKS) {
                ModContent.tabItems().forEach(event::accept);
            }
        });
        modBus.addListener((RegisterCapabilitiesEvent event) ->
                event.registerBlockEntity(Capabilities.Item.BLOCK, ModContent.FARMING_CRATE_ENTITY.get(), WorldlyContainerWrapper::new));
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
            if (event.getEntity() instanceof ServerPlayer player && Harvesting.onUse(player, event.getHand(), event.getPos()) > 0) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        });
        NeoForge.EVENT_BUS.addListener((BlockEvent.FarmlandTrampleEvent event) -> {
            if (Trampling.prevent(event.getEntity())) {
                event.setCanceled(true);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <R> DeferredRegister<R> deferred(ResourceKey<? extends Registry<R>> registry) {
        return (DeferredRegister<R>) registers.computeIfAbsent(registry,
                key -> DeferredRegister.create((ResourceKey<? extends Registry<R>>) key, Constants.MOD_ID));
    }
}
