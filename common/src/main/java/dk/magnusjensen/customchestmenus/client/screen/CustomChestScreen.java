/*
 *     Custom Chest Menus, a Minecraft mod that allows servers to create custom chest menus.
 *     Copyright (c) 2025  legenden (MagnusHJensen)
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dk.magnusjensen.customchestmenus.client.screen;

import dk.magnusjensen.customchestmenus.menu.CustomChestMenu;
import dk.magnusjensen.customchestmenus.menu.MenuLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CustomChestScreen extends AbstractContainerScreen<CustomChestMenu> {
    // Vanilla GUI palette
    private static final int PANEL_BLACK = 0xFF000000;
    private static final int PANEL_FACE = 0xFFC6C6C6;
    private static final int PANEL_LIGHT = 0xFFFFFFFF;
    private static final int PANEL_SHADOW = 0xFF555555;
    private static final int SLOT_DARK = 0xFF373737;
    private static final int SLOT_FACE = 0xFF8B8B8B;

    private final MenuLayout layout;
    private Component dynamicTitle;

    public CustomChestScreen(CustomChestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.layout = menu.layout;
        this.imageWidth = layout.imageWidth();
        this.imageHeight = layout.imageHeight();
        this.inventoryLabelY = layout.invY() - 11;
        this.dynamicTitle = title;


        if (!menu.background.isDefault()) {
            // Update imageHeight and imageWidth according to the custom background size
            // this size would be the entire texture and also include space for player inventory if shown
            this.imageWidth = menu.background.size().x();
            this.imageHeight = menu.background.size().y();

            menu.background.titleLocation().ifPresent(vec -> {
                this.titleLabelX = vec.x();
                this.titleLabelY = vec.y();
            });
        }
    }

    @Override
    public void render(GuiGraphics p_282060_, int p_282533_, int p_281661_, float p_281873_) {
        super.render(p_282060_, p_282533_, p_281661_, p_281873_);
        this.renderTooltip(p_282060_, p_282533_, p_281661_);
    }

    @Override
    protected void renderBg(GuiGraphics p_283694_, float p_282334_, int p_282603_, int p_282158_) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        if (menu.background.isDefault()) {
            // Draw a panel and one frame per slot, so any rows/columns combination looks right
            drawPanel(p_283694_, i, j, this.imageWidth, this.imageHeight);
            for (int row = 0; row < menu.menuSize.getRows(); row++) {
                for (int col = 0; col < menu.menuSize.getColumns(); col++) {
                    drawSlotFrame(p_283694_, i + MenuLayout.SLOT_START + col * MenuLayout.SLOT, j + MenuLayout.GRID_Y + row * MenuLayout.SLOT);
                }
            }
            if (layout.showPlayerInventory()) {
                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 9; col++) {
                        drawSlotFrame(p_283694_, i + MenuLayout.SLOT_START + col * MenuLayout.SLOT, j + layout.invY() + row * MenuLayout.SLOT);
                    }
                }
                for (int col = 0; col < 9; col++) {
                    drawSlotFrame(p_283694_, i + MenuLayout.SLOT_START + col * MenuLayout.SLOT, j + layout.hotbarY());
                }
            }
        } else {
            // Custom background specified, blit the entire thing, but we still expect the same size.
            p_283694_.blit(menu.background.texture(), i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.dynamicTitle, this.titleLabelX, this.titleLabelY, -12566464, false);
        if (layout.showPlayerInventory()) {
            guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
        }
    }

    /** Vanilla style bevelled panel with cut corners. */
    private static void drawPanel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x + 1, y, x + w - 1, y + h, PANEL_BLACK);
        g.fill(x, y + 1, x + w, y + h - 1, PANEL_BLACK);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, PANEL_FACE);
        g.fill(x + 1, y + 1, x + w - 2, y + 3, PANEL_LIGHT);
        g.fill(x + 1, y + 1, x + 3, y + h - 2, PANEL_LIGHT);
        g.fill(x + 2, y + h - 3, x + w - 1, y + h - 1, PANEL_SHADOW);
        g.fill(x + w - 3, y + 2, x + w - 1, y + h - 1, PANEL_SHADOW);
    }

    /** Inset slot frame, x/y is the slot's item position, the frame extends 1px around it. */
    private static void drawSlotFrame(GuiGraphics g, int slotX, int slotY) {
        int x = slotX - 1;
        int y = slotY - 1;
        g.fill(x, y, x + 18, y + 18, PANEL_LIGHT);
        g.fill(x, y, x + 17, y + 17, SLOT_DARK);
        g.fill(x + 1, y + 1, x + 17, y + 17, SLOT_FACE);
    }

    public void setDynamicTitle(Component newTitle) {
        this.dynamicTitle = newTitle;
    }
}
