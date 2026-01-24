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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dk.magnusjensen.customchestmenus.models.BaseItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record CraftItemsAction(List<BaseItem> inputs, List<BaseItem> outputs, boolean hideText) implements MenuAction {


    public static final MapCodec<CraftItemsAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        BaseItem.CODEC.listOf().fieldOf("inputs").forGetter(CraftItemsAction::inputs),
        BaseItem.CODEC.listOf().fieldOf("outputs").forGetter(CraftItemsAction::outputs),
        Codec.BOOL.optionalFieldOf("hide_text", false).forGetter(CraftItemsAction::hideText)
    ).apply(i, CraftItemsAction::new));

    public boolean canCraft(ServerPlayer player) {
        for (Integer slot : getInputSlots(player).keySet()) {
            if (slot == -1) {
                return false;
            }
        }
        return true;
    }


    /**
     * These methods find the slot numbers for the inputs required to craft the items.
     * @param player
     * @return A map with slot number -> quantity of the item.
     */
    public Map<Integer, Integer> getInputSlots(ServerPlayer player) {
        Map<Integer, Integer> inputSlots = new HashMap<>();
        for (BaseItem item : inputs) {
            ItemStack toFind = new ItemStack(BuiltInRegistries.ITEM.get(item.item()), item.count());

            // findSlotMatchingItem does not match on itemstack count.
            int slot = player.getInventory().findSlotMatchingItem(toFind);
            if (slot != -1) {
                if (player.getInventory().getItem(slot).getCount() < item.count()) {
                    inputSlots.put(-1, 0); // Short circuit if not enough items in the slot
                    break;
                }
            }
            inputSlots.put(slot, item.count());
        }
        return inputSlots;
    }

    @Override
    public MenuActionTypeUnified type() {
        return MenuActionTypeUnified.CRAFT_ITEMS;
    }

    @Override
    public MenuActionTypeV1 typeV1() {
        return null;
    }
}
