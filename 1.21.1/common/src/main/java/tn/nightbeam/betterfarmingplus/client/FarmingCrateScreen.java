package tn.nightbeam.betterfarmingplus.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import tn.nightbeam.betterfarmingplus.BetterFarmingPlus;
import tn.nightbeam.betterfarmingplus.block.FarmingCrateBlockEntity;
import tn.nightbeam.betterfarmingplus.menu.FarmingCrateMenu;

public class FarmingCrateScreen extends AbstractContainerScreen<FarmingCrateMenu> {
    private static final ResourceLocation TEXTURE = BetterFarmingPlus.id("textures/gui/container/farming_crate.png");

    public FarmingCrateScreen(FarmingCrateMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 178;
        this.inventoryLabelY = FarmingCrateMenu.INVENTORY_Y - 11;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        // Status lamp: green while harvesting, red when the storage is full, dim when there is nothing ripe.
        int lamp = switch (menu.status()) {
            case FarmingCrateBlockEntity.STATUS_WORKING -> 0;
            case FarmingCrateBlockEntity.STATUS_FULL -> 8;
            default -> 16;
        };
        graphics.blit(TEXTURE, leftPos + 8, topPos + 75, 176, lamp, 8, 8, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        Component status = CrateText.status(menu.status());
        graphics.drawString(font, status, 20, 75, CrateText.statusColor(menu.status()), false);
        Component harvested = CrateText.harvested(menu.harvested());
        graphics.drawString(font, harvested, imageWidth - 8 - font.width(harvested), 6, 0xFF3F6B2A, false);
        Component area = CrateText.area(menu.radius());
        graphics.drawString(font, area, imageWidth - 8 - font.width(area), 75, 0xFF404040, false);
    }
}
