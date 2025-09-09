package dk.magnusjensen.customchestmenus.registries;

import dk.magnusjensen.customchestmenus.CustomChestMenus;
import dk.magnusjensen.customchestmenus.menu.CustomChestMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MenuRegistry {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, CustomChestMenus.MODID);

    public static final Supplier<MenuType<CustomChestMenu>> CUSTOM_CHEST_MENU = MENUS.register("custom_chest_menu", () -> IMenuTypeExtension.create(CustomChestMenu::new));
}
