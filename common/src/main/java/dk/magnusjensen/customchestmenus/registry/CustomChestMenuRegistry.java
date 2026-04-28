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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dk.magnusjensen.customchestmenus.Constants;
import dk.magnusjensen.customchestmenus.models.MenuDefinition;
import dk.magnusjensen.customchestmenus.models.MenuValidationException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CustomChestMenuRegistry {
    private static final Map<String, MenuDefinition> MENUS = new HashMap<>();


    private static Path worldMenuDir(MinecraftServer server) throws IOException {
        // World root (e.g., ./world or ./saves/MyWorld)
        Path root = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
        Path dir  = root.resolve("serverconfig").resolve("customchestmenus");
        Files.createDirectories(dir);
        return dir;
    }

    /**
     * Load menus also function as reload, since it clears the MENUS map on each load.
     * @param server
     */
    public static void loadMenus(MinecraftServer server) {
        MENUS.clear();

        Path customMenusPath;
        try {
            customMenusPath = worldMenuDir(server);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int loaded = 0;
        try (Stream<Path> files = Files.list(customMenusPath)) {
            for (Path p : (Iterable<Path>) files.filter(f -> f.toString().endsWith(".json"))::iterator) {
                if (loadMenuFile(p, server.registryAccess())) loaded++;
            }
        } catch (IOException e) {
            Constants.LOGGER.error("Failed to list {}", customMenusPath.toAbsolutePath(), e);
        }

        Constants.LOGGER.info("Loaded {} menu(s) from {}", loaded, customMenusPath.toAbsolutePath());
    }

    private static boolean loadMenuFile(Path path, RegistryAccess.Frozen registryAccess) {
        try {
            String raw = Files.readString(path);
            JsonElement element = JsonParser.parseString(raw);

            // TODO: Validate in terms of duplicate slots

            MenuDefinition menu;
            try {
                var result = MenuDefinition.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, registryAccess), element)
                    .resultOrPartial(err ->
                        Constants.LOGGER.warn("Menu parse error in {}: {}", path.getFileName(), err)
                    );

                if (result.isEmpty()) {
                    Constants.LOGGER.warn("Skipping {}, failed to parse.", path.getFileName());
                    return false;
                }

                menu = result.get();
            } catch (MenuValidationException ex) {
                Constants.LOGGER.warn("Menu validation error in {}: {}", path.getFileName(), ex.getMessage());
                return false;
            }

            // Collision policy: overwrite with a warning (or flip logic to "keep first and skip" if you prefer)
            MenuDefinition previous = MENUS.put(menu.id(), menu);
            if (previous != null) {
                Constants.LOGGER.warn(
                    "Duplicate menu id '{}' detected. Overwriting previous definition from another file with {}.",
                    menu.id(), path.getFileName()
                );
            } else {
                Constants.LOGGER.debug("Loaded menu '{}' from {}", menu.id(), path.getFileName());
            }
            return true;

        } catch (Exception e) {
            Constants.LOGGER.error("Exception while loading {}", path.toAbsolutePath(), e);
            return false;
        }
    }

    // --- Accessors ---
    public static MenuDefinition get(String id) { return MENUS.get(id); }
    public static Collection<MenuDefinition> all() { return Collections.unmodifiableCollection(MENUS.values()); }
}
