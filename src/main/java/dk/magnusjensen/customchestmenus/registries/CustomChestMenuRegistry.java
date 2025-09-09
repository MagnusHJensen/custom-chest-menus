package dk.magnusjensen.customchestmenus.registries;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dk.magnusjensen.customchestmenus.CustomChestMenus;
import dk.magnusjensen.customchestmenus.models.MenuDefinition;
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
                if (loadMenuFile(p)) loaded++;
            }
        } catch (IOException e) {
            CustomChestMenus.LOGGER.error("Failed to list {}", customMenusPath.toAbsolutePath(), e);
        }

        CustomChestMenus.LOGGER.info("Loaded {} menu(s) from {}", loaded, customMenusPath.toAbsolutePath());
    }

    private static boolean loadMenuFile(Path path) {
        try {
            String raw = Files.readString(path);
            JsonElement element = JsonParser.parseString(raw);

            var result = MenuDefinition.CODEC.parse(JsonOps.INSTANCE, element)
                .resultOrPartial(err ->
                    CustomChestMenus.LOGGER.warn("Menu parse error in {}: {}", path.getFileName(), err)
                );

            if (result.isEmpty()) {
                CustomChestMenus.LOGGER.warn("Skipping {}, failed to parse.", path.getFileName());
                return false;
            }

            MenuDefinition menu = result.get();

            // Collision policy: overwrite with a warning (or flip logic to "keep first and skip" if you prefer)
            MenuDefinition previous = MENUS.put(menu.id(), menu);
            if (previous != null) {
                CustomChestMenus.LOGGER.warn(
                    "Duplicate menu id '{}' detected. Overwriting previous definition from another file with {}.",
                    menu.id(), path.getFileName()
                );
            } else {
                CustomChestMenus.LOGGER.debug("Loaded menu '{}' from {}", menu.id(), path.getFileName());
            }
            return true;

        } catch (Exception e) {
            CustomChestMenus.LOGGER.error("Exception while loading {}", path.toAbsolutePath(), e);
            return false;
        }
    }

    // --- Accessors ---
    public static MenuDefinition get(String id) { return MENUS.get(id); }
    public static Collection<MenuDefinition> all() { return Collections.unmodifiableCollection(MENUS.values()); }
}
