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

package dk.magnusjensen.customchestmenus;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dk.magnusjensen.customchestmenus.models.MenuDefinition;
import dk.magnusjensen.customchestmenus.models.MenuValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MenuDefinitionTest {

    @Test
    void testInvalidSizeThrows() {
        // Example JSON input for testing
        JsonObject json = new JsonObject();
        json.addProperty("format_version", 1);
        json.addProperty("id", "test_menu");
        json.addProperty("name", "Test Menu");
        json.addProperty("size", "9x3");

        // Parse using the codec
        var error = Assertions.assertThrows(IllegalArgumentException.class, () -> MenuDefinition.CODEC.decode(JsonOps.INSTANCE, json));
        Assertions.assertEquals("No enum constant dk.magnusjensen.customchestmenus.models.MenuSize.9X3", error.getMessage());
    }

    @Test
    void testTooManyItemsPerPageThrows() {
        JsonObject json = new JsonObject();
        json.addProperty("format_version", 1);
        json.addProperty("id", "test_menu");
        json.addProperty("name", "Test Menu");
        json.addProperty("size", "single");
        var page = new JsonObject();
        page.addProperty("title", "Page 1");
        var items = new com.google.gson.JsonArray();
        for (int i = 0; i < 30; i++) {
            var item = new JsonObject();
            item.addProperty("slot", i);
            item.addProperty("name", "Item " + i);
            item.addProperty("item", "minecraft:stone");
            item.add("action", TestUtils.NoopAction());
            items.add(item);
        }
        page.add("items", items);
        var pagesArray = new com.google.gson.JsonArray();
        pagesArray.add(page);
        json.add("pages", pagesArray);

        var error = Assertions.assertThrows(MenuValidationException.class, () -> MenuDefinition.CODEC.decode(JsonOps.INSTANCE, json));
        Assertions.assertEquals("Validation errors:\nPage[0] errors:\nSlot 27 out of bounds (0, 27)\nSlot 28 out of bounds (0, 27)\nSlot 29 out of bounds (0, 27)\n'Page 1' has 30 items, which exceeds the maximum of 27 for menu size SINGLE\n", error.getMessage());
    }

    @Test
    void testSameSlotPerPageThrows() {
        JsonObject json = new JsonObject();
        json.addProperty("format_version", 1);
        json.addProperty("id", "test_menu");
        json.addProperty("name", "Test Menu");
        json.addProperty("size", "single");
        var page = new JsonObject();
        page.addProperty("title", "Page 1");
        var items = new com.google.gson.JsonArray();
        for (int i = 0; i < 2; i++) {
            var item = new JsonObject();
            item.addProperty("slot", 0);
            item.addProperty("name", "Item " + i);
            item.addProperty("item", "minecraft:stone");
            item.add("action", TestUtils.NoopAction());
            items.add(item);
        }
        page.add("items", items);
        var pagesArray = new com.google.gson.JsonArray();
        pagesArray.add(page);
        json.add("pages", pagesArray);

        var error = Assertions.assertThrows(MenuValidationException.class, () -> MenuDefinition.CODEC.decode(JsonOps.INSTANCE, json));
        Assertions.assertEquals("Validation errors:\nPage[0] errors:\nSlot 0 already used\n", error.getMessage());
    }
}