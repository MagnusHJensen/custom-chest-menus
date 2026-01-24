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

public sealed interface MenuAction permits NoopAction, CloseAction, TeleportAction, PageAction, CommandAction, CommandActionV1, CraftItemsAction, CraftItemsActionV1 {

    MenuActionTypeUnified type();
    MenuActionTypeV1 typeV1();

    Codec<MenuAction> CODEC_UNIFIED =
        MenuActionTypeUnified.TYPE_CODEC.dispatch("type", MenuAction::type, MenuActionTypeUnified::subCodec);

    Codec<MenuAction> CODEC_V1 =
        MenuActionTypeV1.TYPE_CODEC.dispatch("type", MenuAction::typeV1, MenuActionTypeV1::subCodec);

}