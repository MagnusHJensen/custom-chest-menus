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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CustomChestScreen extends AbstractContainerScreen<CustomChestMenu> {
    private static final ResourceLocation DEFAULT_BACKGROUND = ResourceLocation.tryParse("textures/gui/container/generic_54.png");
    /**
     * Window height is calculated with these values the more rows, the higher
     */
    private final int containerRows;
    private Component dynamicTitle;

    public CustomChestScreen(CustomChestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.containerRows = menu.getRowCount();
        this.imageHeight = 114 + this.containerRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
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
            // Render the chest background in the default way
            p_283694_.blit(DEFAULT_BACKGROUND, i, j, 0.0F, 0.0F, this.imageWidth, this.containerRows * 18 + 17, 256, 256);
            p_283694_.blit(DEFAULT_BACKGROUND, i, j + this.containerRows * 18 + 17, 0.0F, 126.0F, this.imageWidth, 96, 256, 256);
        } else {
            // Custom background specified, blit the entire thing, but we still expect the same size.
            p_283694_.blit(menu.background.texture(), i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.dynamicTitle, this.titleLabelX, this.titleLabelY, -12566464, false);
        if (menu.background.showPlayerInventory()) {
            guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
        }
    }

    public void setDynamicTitle(Component newTitle) {
        this.dynamicTitle = newTitle;
    }
}
