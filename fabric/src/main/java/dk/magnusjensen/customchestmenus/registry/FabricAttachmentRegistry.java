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

package dk.magnusjensen.customchestmenus.registry;

import dk.magnusjensen.customchestmenus.Constants;
import dk.magnusjensen.customchestmenus.data.PlayerDataAttachment;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class FabricAttachmentRegistry {
    public static final DataComponentType<PlayerDataAttachment> PLAYER_DATA = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        ResourceLocation.tryBuild(Constants.MOD_ID, "player_data"),
        DataComponentType.<PlayerDataAttachment>builder().build()
    );

    public static <T> Optional<DataComponentType<T>> findByClass(Class<T> clazz) {
        return BuiltInRegistries.DATA_COMPONENT_TYPE.stream()
            .filter(dataComponentType -> {
                // Use reflection to check if the generic type matches
                try {
                    return clazz.isAssignableFrom(dataComponentType.getClass().getGenericSuperclass().getClass());
                } catch (Exception e) {
                    return false;
                }
            })
            .map(dataComponentType -> (DataComponentType<T>) dataComponentType)
            .findFirst();
    }

    public static void register() {
        Constants.LOGGER.info("Registering menus for Fabric.");
    }
}
