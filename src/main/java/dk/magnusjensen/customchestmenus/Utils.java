package dk.magnusjensen.customchestmenus;

import net.minecraft.resources.ResourceLocation;

public class Utils {
    public static ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(CustomChestMenus.MODID, path);
    }
}
