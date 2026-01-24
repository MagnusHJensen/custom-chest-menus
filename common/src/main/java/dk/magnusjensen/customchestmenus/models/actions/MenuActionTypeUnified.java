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

package dk.magnusjensen.customchestmenus.models.actions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import java.util.Optional;

public enum MenuActionTypeUnified {
    NOOP("noop", NoopAction.CODEC),
    CLOSE("close", CloseAction.CODEC),
    TELEPORT("teleport", TeleportAction.CODEC),
    NEXT_PAGE("next_page", PageAction.NEXT_CODEC),
    PREVIOUS_PAGE("previous_page", PageAction.PREVIOUS_CODEC),
    JUMP_TO_PAGE("jump_to_page", PageAction.JUMP_TO_PAGE_CODEC),
    COMMAND("command", CommandAction.CODEC),
    CRAFT_ITEMS("craft_items", CraftItemsAction.CODEC); // unified

    private final String id;
    private final MapCodec<? extends MenuAction> codec;

    MenuActionTypeUnified(String id, MapCodec<? extends MenuAction> codec) {
        this.id = id;
        this.codec = codec;
    }

    public String id() {
        return id;
    }

    public static final Codec<MenuActionTypeUnified> TYPE_CODEC = Codec.STRING.flatXmap(
        s -> byId(s).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown action type: " + s)),
        t -> DataResult.success(t.id)
    );

    public static Optional<MenuActionTypeUnified> byId(String id) {
        for (var t : values()) if (t.id.equals(id)) return Optional.of(t);
        return Optional.empty();
    }

    // Used by dispatch(...) -> must return a MapCodec
    public MapCodec<? extends MenuAction> subCodec() {
        return this.codec;
    }
}