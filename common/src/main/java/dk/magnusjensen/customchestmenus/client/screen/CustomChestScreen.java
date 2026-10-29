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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class CustomChestScreen extends AbstractContainerScreen<CustomChestMenu> {
    /** Vanilla's 18x18 inset slot frame. */
    private static final Identifier SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot");

    // Vanilla GUI palette, there is no panel sprite to reuse
    private static final int PANEL_BLACK = 0xFF000000;
    private static final int PANEL_FACE = 0xFFC6C6C6;
    private static final int PANEL_LIGHT = 0xFFFFFFFF;
    private static final int PANEL_SHADOW = 0xFF555555;

    private final MenuLayout layout;
    private Component dynamicTitle;

    public CustomChestScreen(CustomChestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, menu.layout.imageWidth(), menu.layout.imageHeight());
        this.layout = menu.layout;
        this.inventoryLabelY = layout.invY() - 11;
        this.dynamicTitle = title;

        if (!menu.background.isDefault()) {
            menu.background.titleLocation().ifPresent(vec -> {
                this.titleLabelX = vec.x();
                this.titleLabelY = vec.y();
            });
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);

        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        if (menu.background.isDefault()) {
            // Draw a panel and one frame per slot, so any rows/columns combination looks right
            drawPanel(graphics, i, j, this.imageWidth, this.imageHeight);
            for (int row = 0; row < menu.menuSize.getRows(); row++) {
                for (int col = 0; col < menu.menuSize.getColumns(); col++) {
                    drawSlotFrame(graphics, i + MenuLayout.SLOT_START + col * MenuLayout.SLOT, j + MenuLayout.GRID_Y + row * MenuLayout.SLOT);
                }
            }
            if (layout.showPlayerInventory()) {
                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 9; col++) {
                        drawSlotFrame(graphics, i + MenuLayout.SLOT_START + col * MenuLayout.SLOT, j + layout.invY() + row * MenuLayout.SLOT);
                    }
                }
                for (int col = 0; col < 9; col++) {
                    drawSlotFrame(graphics, i + MenuLayout.SLOT_START + col * MenuLayout.SLOT, j + layout.hotbarY());
                }
            }
        } else {
            // Custom background specified, blit the entire thing, but we still expect the same size.
            graphics.blit(RenderPipelines.GUI_TEXTURED, menu.background.texture(), i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, -12566464, false);
        if (layout.showPlayerInventory())
            graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
    }

    /** Vanilla style bevelled panel with cut corners. */
    private static void drawPanel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x + 1, y, x + w - 1, y + h, PANEL_BLACK);
        g.fill(x, y + 1, x + w, y + h - 1, PANEL_BLACK);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, PANEL_FACE);
        g.fill(x + 1, y + 1, x + w - 2, y + 3, PANEL_LIGHT);
        g.fill(x + 1, y + 1, x + 3, y + h - 2, PANEL_LIGHT);
        g.fill(x + 2, y + h - 3, x + w - 1, y + h - 1, PANEL_SHADOW);
        g.fill(x + w - 3, y + 2, x + w - 1, y + h - 1, PANEL_SHADOW);
    }

    /** x/y is the slot's item position, the frame extends 1px around it. */
    private static void drawSlotFrame(GuiGraphicsExtractor g, int slotX, int slotY) {
        g.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, slotX - 1, slotY - 1, 18, 18);
    }

    public void setDynamicTitle(Component newTitle) {
        this.dynamicTitle = newTitle;
    }
}
