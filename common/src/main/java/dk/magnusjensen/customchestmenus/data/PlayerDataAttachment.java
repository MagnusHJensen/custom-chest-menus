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

package dk.magnusjensen.customchestmenus.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dk.magnusjensen.customchestmenus.models.interactivity.BindingMode;

/**
 * Stores and serializes per-player data related to custom chest menus.
 */
public class PlayerDataAttachment {


    public static final MapCodec<PlayerDataAttachment> MAP_CODEC = MapCodec.unit(new PlayerDataAttachment());
    /*public static final MapCodec<PlayerDataAttachment> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.BOOL.fieldOf("inBindingMode").forGetter(PlayerDataAttachment::isInBindingMode)
    ).apply(instance, PlayerDataAttachment::new));*/
    public static final Codec<PlayerDataAttachment> CODEC = MAP_CODEC.codec();

    /**
     * Whether the player is currently in binding mode, which means:
     * Right-clicking on any block or Entity will bind a menu to it.
     * If the same menu is already bound to that target, the binding is removed instead.
     * If another menu is bound, then it prompts a message and does nothing.
     */
    private BindingMode inBindingMode = BindingMode.NONE;
    private String menuToBind = null;

    public PlayerDataAttachment() {
    }

    public boolean isBindingMode() {
        return inBindingMode == BindingMode.BIND;
    }

    public boolean isUnbindingMode() {
        return inBindingMode == BindingMode.UNBIND;
    }

    public BindingMode getBindingMode() {
        return inBindingMode;
    }

    public String getMenuToBind() {
        return menuToBind;
    }

    public void activateBindingMode(String menuId) {
        this.inBindingMode = BindingMode.BIND;
        this.menuToBind = menuId;
    }

    public void activateUnbindingMode() {
        this.inBindingMode = BindingMode.UNBIND;
        this.menuToBind = null;
    }

    public void disableBindingMode() {
        this.inBindingMode = BindingMode.NONE;
        this.menuToBind = null;
    }
}
