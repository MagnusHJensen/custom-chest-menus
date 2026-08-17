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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record CraftItemsAction(List<BaseItem> inputs, List<BaseItem> outputs, boolean hideText) implements MenuAction {


    public static final MapCodec<CraftItemsAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        BaseItem.CODEC.listOf().fieldOf("inputs").forGetter(CraftItemsAction::inputs),
        BaseItem.CODEC.listOf().fieldOf("outputs").forGetter(CraftItemsAction::outputs),
        Codec.BOOL.optionalFieldOf("hide_text", false).forGetter(CraftItemsAction::hideText)
    ).apply(i, CraftItemsAction::new));

    /**
     * The key used to signal that the inputs cannot be satisfied by an inventory.
     */
    public static final int MISSING_SLOT = -1;

    public boolean canCraft(ServerPlayer player) {
        return canCraft(player.getInventory());
    }

    public boolean canCraft(Inventory inventory) {
        return !getInputSlots(inventory).containsKey(MISSING_SLOT);
    }

    public Map<Integer, Integer> getInputSlots(ServerPlayer player) {
        return getInputSlots(player.getInventory());
    }

    /**
     * These methods find the slot numbers for the inputs required to craft the items.
     * <p>
     * An input may require more items than a single stack can hold (or be spread over several
     * partially filled stacks), so every matching slot is considered until the required count is
     * covered. Slots already reserved by an earlier input are not handed out twice.
     *
     * @param inventory the inventory to take the inputs from.
     * @return A map with slot number -> quantity of the item, or {@code {MISSING_SLOT: 0}} if the
     *         inventory does not hold enough of every input.
     */
    public Map<Integer, Integer> getInputSlots(Inventory inventory) {
        Map<Integer, Integer> inputSlots = new LinkedHashMap<>();
        // Equipment slots are not valid crafting inputs, so only the main inventory is scanned.
        int slotCount = inventory.items.size();

        for (BaseItem item : inputs) {
            var toFind = item.makeItemStack();
            int remaining = item.count();

            for (int slot = 0; slot < slotCount && remaining > 0; slot++) {
                ItemStack inSlot = inventory.getItem(slot);
                // isSameItemSameComponents does not match on itemstack count.
                if (inSlot.isEmpty() || !ItemStack.isSameItemSameComponents(toFind, inSlot)) continue;

                int available = inSlot.getCount() - inputSlots.getOrDefault(slot, 0);
                if (available <= 0) continue;

                int taken = Math.min(available, remaining);
                inputSlots.merge(slot, taken, Integer::sum);
                remaining -= taken;
            }

            if (remaining > 0) {
                return Map.of(MISSING_SLOT, 0); // Short circuit if not enough items in the inventory
            }

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
