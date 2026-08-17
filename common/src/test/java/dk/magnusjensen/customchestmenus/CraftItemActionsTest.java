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

package dk.magnusjensen.customchestmenus;

import dk.magnusjensen.customchestmenus.models.BaseItem;
import dk.magnusjensen.customchestmenus.models.actions.CraftItemsAction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class CraftItemsActionsTest {

    private static final Identifier DIAMOND = Identifier.withDefaultNamespace("diamond");
    private static final Identifier IRON_INGOT = Identifier.withDefaultNamespace("iron_ingot");
    private static final Identifier DIAMOND_BLOCK = Identifier.withDefaultNamespace("diamond_block");

    @BeforeAll
    static void bootstrap() {
        TestUtils.bootstrapMinecraft();
    }

    private static BaseItem item(Identifier id, int count) {
        return new BaseItem(id, Component.empty(), count, Map.of());
    }

    private static ItemStack stack(Identifier id, int count) {
        Item entry = BuiltInRegistries.ITEM.getValue(id);
        return new ItemStack(entry, count);
    }

    @Test
    void inputSpanningMoreThanOneStackUsesEveryMatchingSlot() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));
        inventory.setItem(1, stack(DIAMOND, 64));
        inventory.setItem(2, stack(DIAMOND, 10));

        // 128 diamonds does not fit in a single slot, so the inputs have to be gathered across slots.
        CraftItemsAction action = new CraftItemsAction(
            List.of(item(DIAMOND, 128)),
            List.of(item(DIAMOND_BLOCK, 1)),
            false
        );

        Assertions.assertTrue(action.canCraft(inventory));
        Assertions.assertEquals(Map.of(0, 64, 1, 64), action.getInputSlots(inventory));
    }

    @Test
    void inputSpanningPartiallyFilledStacksUsesEveryMatchingSlot() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(3, stack(DIAMOND, 40));
        inventory.setItem(7, stack(IRON_INGOT, 64));
        inventory.setItem(9, stack(DIAMOND, 40));
        inventory.setItem(20, stack(DIAMOND, 40));

        CraftItemsAction action = new CraftItemsAction(
            List.of(item(DIAMOND, 100)),
            List.of(item(DIAMOND_BLOCK, 1)),
            false
        );

        // Only what is needed is taken from the last slot, and unrelated items are left alone.
        Assertions.assertEquals(Map.of(3, 40, 9, 40, 20, 20), action.getInputSlots(inventory));
    }

    @Test
    void notEnoughItemsAcrossAllSlotsCannotCraft() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));
        inventory.setItem(1, stack(DIAMOND, 63));

        CraftItemsAction action = new CraftItemsAction(
            List.of(item(DIAMOND, 128)),
            List.of(item(DIAMOND_BLOCK, 1)),
            false
        );

        Assertions.assertFalse(action.canCraft(inventory));
        Assertions.assertEquals(Map.of(CraftItemsAction.MISSING_SLOT, 0), action.getInputSlots(inventory));
    }

    @Test
    void twoInputsOfTheSameItemDoNotShareTheSameSlot() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));
        inventory.setItem(1, stack(DIAMOND, 64));

        CraftItemsAction action = new CraftItemsAction(
            List.of(item(DIAMOND, 64), item(DIAMOND, 64)),
            List.of(item(DIAMOND_BLOCK, 1)),
            false
        );

        Assertions.assertEquals(Map.of(0, 64, 1, 64), action.getInputSlots(inventory));
    }

    @Test
    void craftingConsumesInputsFromEverySlotAndGivesTheOutput() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));
        inventory.setItem(1, stack(DIAMOND, 64));
        inventory.setItem(2, stack(DIAMOND, 10));
        inventory.setItem(5, stack(IRON_INGOT, 70));

        CraftItemsAction action = new CraftItemsAction(
            List.of(item(DIAMOND, 128), item(IRON_INGOT, 70)),
            List.of(item(DIAMOND_BLOCK, 2)),
            false
        );

        Assertions.assertTrue(action.canCraft(inventory));
        List<ItemStack> leftovers = ActionExecutor.craftItems(inventory, action);

        Assertions.assertTrue(leftovers.isEmpty());
        Assertions.assertEquals(10, countOf(inventory, DIAMOND)); // Untouched remainder.
        Assertions.assertEquals(0, countOf(inventory, IRON_INGOT));
        Assertions.assertEquals(2, countOf(inventory, DIAMOND_BLOCK));
    }

    private static int countOf(Inventory inventory, Identifier id) {
        Item item = BuiltInRegistries.ITEM.getValue(id);
        int count = 0;
        for (int slot = 0; slot < inventory.getNonEquipmentItems().size(); slot++) {
            ItemStack inSlot = inventory.getItem(slot);
            if (inSlot.is(item)) {
                count += inSlot.getCount();
            }
        }
        return count;
    }
}