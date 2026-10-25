package tn.nightbeam.betterfarmingplus.forge;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import tn.nightbeam.betterfarmingplus.BetterFarmingPlus;
import tn.nightbeam.betterfarmingplus.Constants;
import tn.nightbeam.betterfarmingplus.content.ModContent;
import tn.nightbeam.betterfarmingplus.farming.Harvesting;
import tn.nightbeam.betterfarmingplus.farming.Trampling;
import tn.nightbeam.betterfarmingplus.platform.Registrar;

@Mod(Constants.MOD_ID)
public final class BetterFarmingPlusForge {
    private final Map<ResourceKey<?>, DeferredRegister<?>> registers = new HashMap<>();

    public BetterFarmingPlusForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModContent.CRATE_FACTORY = ForgeFarmingCrateBlockEntity::new;
        BetterFarmingPlus.init(FMLPaths.CONFIGDIR::get, new Registrar() {
            @Override
            public <R, T extends R> Supplier<T> register(ResourceKey<? extends Registry<R>> registry, String path, Function<ResourceLocation, T> factory) {
                ResourceLocation id = BetterFarmingPlus.id(path);
                return deferred(registry).register(path, () -> factory.apply(id));
            }
        }, new ModContent.MenuTypeFactory() {
            @Override
            public <T extends AbstractContainerMenu> MenuType<T> create(ModContent.MenuConstructor<T> constructor) {
                return new MenuType<>(constructor::create, FeatureFlags.VANILLA_SET);
            }
        });
        registers.values().forEach(deferred -> deferred.register(modBus));
        modBus.addListener((BuildCreativeModeTabContentsEvent event) -> {
            if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                ModContent.tabItems().forEach(event::accept);
            }
        });
        MinecraftForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
            if (event.getEntity() instanceof ServerPlayer player && Harvesting.onUse(player, event.getHand(), event.getPos()) > 0) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        });
        MinecraftForge.EVENT_BUS.addListener((BlockEvent.FarmlandTrampleEvent event) -> {
            if (Trampling.prevent(event.getEntity())) {
                event.setCanceled(true);
            }
        });
        DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> BetterFarmingPlusForgeClient.init(modBus));
    }

    @SuppressWarnings("unchecked")
    private <R> DeferredRegister<R> deferred(ResourceKey<? extends Registry<R>> registry) {
        return (DeferredRegister<R>) registers.computeIfAbsent(registry,
                key -> DeferredRegister.create((ResourceKey<? extends Registry<R>>) key, Constants.MOD_ID));
    }
}
