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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.mockito.Mockito;

public class TestUtils {

    private static boolean bootstrapped = false;

    public static JsonElement NoopAction() {

        var action = new JsonObject();
        action.addProperty("type", "noop");

        return action;
    }

    /**
     * Brings up just enough of Minecraft for tests to be able to work with items.
     * <p>
     * Item data components are bound when a server loads its datapacks, so on top of the regular
     * bootstrap the vanilla registry contents have to be baked in, otherwise creating an
     * {@link net.minecraft.world.item.ItemStack} fails with "Components not bound yet".
     */
    public static synchronized void bootstrapMinecraft() {
        if (bootstrapped) return;
        bootstrapped = true;

        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(VanillaRegistries.createLookup())
            .forEach(DataComponentInitializers.PendingComponents::apply);
    }

    /**
     * An inventory detached from any player. Only the slot storage is usable, anything that would
     * talk back to the player (syncing, dropping, creative mode checks) is not.
     */
    public static Inventory emptyInventory() {
        Player player = Mockito.mock(Player.class);   // hasInfiniteMaterials() defaults to false
        return new Inventory(player, new EntityEquipment());
    }
}
