/*
 *     Custom Chest Menus, a Minecraft mod that allows servers to create custom chest menus.
 *     Copyright (c) 2026  legenden (MagnusHJensen)
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

package dk.magnusjensen.customchestmenus.menu;

import dk.magnusjensen.customchestmenus.models.MenuBackground;
import dk.magnusjensen.customchestmenus.models.v2.MenuSize;

/**
 * Pixel layout shared by the menu (slot positions) and the screen (background rendering).
 * All coordinates are relative to the top left corner of the screen image.
 * <p>
 * The player inventory is only ever shown for shorthand sizes (single/double, always 9 columns wide),
 * a custom rows/columns size never shows it.
 *
 * @param showPlayerInventory whether the player inventory slots exist and are rendered
 * @param invY                y of the first player inventory row's item area (x is always {@link #SLOT_START})
 */
public record MenuLayout(int imageWidth, int imageHeight, boolean showPlayerInventory, int invY) {
    public static final int SLOT = 18;
    /** x/y of the first slot's item area, same as vanilla chests. */
    public static final int SLOT_START = 8;
    /** y of the first container row's item area, below the title. */
    public static final int GRID_Y = 18;
    private static final int VANILLA_WIDTH = 176;
    private static final int PANEL_PADDING = 7;

    public static MenuLayout of(MenuSize size, MenuBackground background) {
        int rows = size.getRows();
        boolean showInventory = background.showPlayerInventory() && size.isShorthand();
        int invY = GRID_Y + rows * SLOT + 13;

        if (!background.isDefault()) {
            // Custom textures define the whole image, including the player inventory if shown
            return new MenuLayout(background.size().x(), background.size().y(), showInventory, invY);
        }
        if (showInventory) {
            return new MenuLayout(VANILLA_WIDTH, 114 + rows * SLOT, true, invY);
        }

        // Container only: the panel hugs the slot grid
        return new MenuLayout(size.getColumns() * SLOT + PANEL_PADDING * 2, 17 + rows * SLOT + PANEL_PADDING, false, invY);
    }

    public int hotbarY() {
        return invY + 58;
    }
}
