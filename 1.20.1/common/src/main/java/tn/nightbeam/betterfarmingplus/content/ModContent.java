package tn.nightbeam.betterfarmingplus.content;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import tn.nightbeam.betterfarmingplus.block.FarmingCrateBlock;
import tn.nightbeam.betterfarmingplus.block.FarmingCrateBlockEntity;
import tn.nightbeam.betterfarmingplus.block.SprinklerBlock;
import tn.nightbeam.betterfarmingplus.block.SprinklerBlockEntity;
import tn.nightbeam.betterfarmingplus.menu.FarmingCrateMenu;
import tn.nightbeam.betterfarmingplus.platform.Registrar;

/** Everything the mod registers. The Farming Crate keeps its 0.5.0 block, item and block entity ids. */
public final class ModContent {
    public static Supplier<Block> FARMING_CRATE;
    public static Supplier<Block> SPRINKLER;
    public static Supplier<Item> FARMING_CRATE_ITEM;
    public static Supplier<Item> SPRINKLER_ITEM;
    public static Supplier<BlockEntityType<FarmingCrateBlockEntity>> FARMING_CRATE_ENTITY;
    public static Supplier<BlockEntityType<SprinklerBlockEntity>> SPRINKLER_ENTITY;
    public static Supplier<MenuType<FarmingCrateMenu>> FARMING_CRATE_MENU;

    /** Creates the crate's block entity: Forge supplies a subclass that exposes a sided item handler to hoppers. */
    public static BlockEntityType.BlockEntitySupplier<FarmingCrateBlockEntity> CRATE_FACTORY = FarmingCrateBlockEntity::new;

    /** Creates menu types; their constructor is not public in vanilla, so each loader supplies this. */
    public interface MenuTypeFactory {
        <T extends AbstractContainerMenu> MenuType<T> create(MenuConstructor<T> constructor);
    }

    @FunctionalInterface
    public interface MenuConstructor<T extends AbstractContainerMenu> {
        T create(int containerId, Inventory inventory);
    }

    private ModContent() {
    }

    public static void register(Registrar r, MenuTypeFactory menus) {
        FARMING_CRATE = r.register(Registries.BLOCK, "farming_crate", id -> new FarmingCrateBlock(block(id)
                .mapColor(MapColor.WOOD).strength(1.5F, 10.0F).sound(SoundType.WOOD)));
        SPRINKLER = r.register(Registries.BLOCK, "sprinkler", id -> new SprinklerBlock(block(id)
                .mapColor(MapColor.COLOR_ORANGE).strength(1.5F, 6.0F).sound(SoundType.COPPER).noOcclusion()));
        FARMING_CRATE_ITEM = r.register(Registries.ITEM, "farming_crate", id -> new BlockItem(FARMING_CRATE.get(), new Item.Properties()));
        SPRINKLER_ITEM = r.register(Registries.ITEM, "sprinkler", id -> new BlockItem(SPRINKLER.get(), new Item.Properties()));
        FARMING_CRATE_ENTITY = r.register(Registries.BLOCK_ENTITY_TYPE, "farming_crate",
                id -> BlockEntityType.Builder.of(CRATE_FACTORY, FARMING_CRATE.get()).build(null));
        SPRINKLER_ENTITY = r.register(Registries.BLOCK_ENTITY_TYPE, "sprinkler",
                id -> BlockEntityType.Builder.of(SprinklerBlockEntity::new, SPRINKLER.get()).build(null));
        FARMING_CRATE_MENU = r.register(Registries.MENU, "farming_crate", id -> menus.create(FarmingCrateMenu::new));
    }

    /** The items, in creative-tab order (they go into the Functional Blocks tab). */
    public static List<Item> tabItems() {
        return List.of(FARMING_CRATE_ITEM.get(), SPRINKLER_ITEM.get());
    }

    private static BlockBehaviour.Properties block(ResourceLocation id) {
        return BlockBehaviour.Properties.of();
    }
}
