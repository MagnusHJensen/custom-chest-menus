/*
 *     Custom Chest Menus, a Minecraft mod that allows servers to create custom chest menus.
 *     Copyright (c) 2025-2026  legenden (MagnusHJensen)
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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dk.magnusjensen.customchestmenus.models.BaseItem;

import java.util.List;
import java.util.Optional;

public record CraftItemsActionV1(List<CraftItem> inputs, List<CraftItem> outputs) implements MenuAction {


    public static final MapCodec<CraftItemsActionV1> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        CraftItem.CODEC.listOf().fieldOf("inputs").forGetter(CraftItemsActionV1::inputs),
        CraftItem.CODEC.listOf().fieldOf("outputs").forGetter(CraftItemsActionV1::outputs)
    ).apply(i, CraftItemsActionV1::new));

    @Override
    public MenuActionTypeUnified type() {
        return null;
    }

    @Override
    public MenuActionTypeV1 typeV1() {
        return MenuActionTypeV1.CRAFT_ITEMS;
    }

    public CraftItemsAction toUnified() {
        List<BaseItem> mappedInputs = this.inputs.stream().map(CraftItem::toBaseItem).toList();
        List<BaseItem> mappedOutputs = this.outputs.stream().map(CraftItem::toBaseItem).toList();
        return new CraftItemsAction(mappedInputs, mappedOutputs, false, Optional.empty());
    }
}
