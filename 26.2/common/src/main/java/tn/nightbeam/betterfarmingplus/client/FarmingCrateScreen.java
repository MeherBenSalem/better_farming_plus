package tn.nightbeam.betterfarmingplus.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import tn.nightbeam.betterfarmingplus.BetterFarmingPlus;
import tn.nightbeam.betterfarmingplus.block.FarmingCrateBlockEntity;
import tn.nightbeam.betterfarmingplus.menu.FarmingCrateMenu;

public class FarmingCrateScreen extends AbstractContainerScreen<FarmingCrateMenu> {
    private static final Identifier TEXTURE = BetterFarmingPlus.id("textures/gui/container/farming_crate.png");

    public FarmingCrateScreen(FarmingCrateMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 178);
        this.inventoryLabelY = FarmingCrateMenu.INVENTORY_Y - 11;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        // Status lamp: green while harvesting, red when the storage is full, dim when there is nothing ripe.
        float lamp = switch (menu.status()) {
            case FarmingCrateBlockEntity.STATUS_WORKING -> 0.0F;
            case FarmingCrateBlockEntity.STATUS_FULL -> 8.0F;
            default -> 16.0F;
        };
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 8, topPos + 75, 176.0F, lamp, 8, 8, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        Component status = CrateText.status(menu.status());
        graphics.text(font, status, 20, 75, CrateText.statusColor(menu.status()), false);
        Component harvested = CrateText.harvested(menu.harvested());
        graphics.text(font, harvested, imageWidth - 8 - font.width(harvested), 6, 0xFF3F6B2A, false);
        Component area = CrateText.area(menu.radius());
        graphics.text(font, area, imageWidth - 8 - font.width(area), 75, 0xFF404040, false);
    }
}
