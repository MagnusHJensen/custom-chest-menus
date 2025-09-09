package dk.magnusjensen.customchestmenus;

import com.mojang.logging.LogUtils;
import dk.magnusjensen.customchestmenus.menu.CustomChestMenu;
import dk.magnusjensen.customchestmenus.models.MenuDefinition;
import dk.magnusjensen.customchestmenus.models.PagePayload;
import dk.magnusjensen.customchestmenus.registries.CustomChestMenuRegistry;
import dk.magnusjensen.customchestmenus.registries.MenuRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CustomChestMenus.MODID)
@EventBusSubscriber(modid = CustomChestMenus.MODID)
public class CustomChestMenus {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "customchestmenus";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public CustomChestMenus(IEventBus modEventBus, ModContainer modContainer) {
        MenuRegistry.MENUS.register(modEventBus);
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        CustomChestMenuRegistry.loadMenus(event.getServer());
    }

    @SubscribeEvent
    public static void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getSide().isServer() && event.getItemStack().getItem() == Items.STICK) {
            openMenu((ServerPlayer) event.getEntity(), "test_menu", 0);
        }
    }

    public static void openMenu(ServerPlayer player, MenuDefinition def, int pageIndex) {
        PagePayload payload = def.build(def, pageIndex);

        MenuProvider provider = new MenuProvider() {
            @Override public Component getDisplayName() { return payload.title(); }

            @Override
            public CustomChestMenu createMenu(int windowId, Inventory inv, Player p) {
                // Server container with authoritative contents:
                CustomChestMenu cont = new CustomChestMenu(windowId, inv, def.id(), payload);
                cont.populateFromDefinition(def, pageIndex);  // Fill backing SimpleContainer on server
                return cont;
            }
        };

        player.openMenu(provider, buf -> {
            buf.writeUtf(def.id());
            payload.write(buf);
        });
    }

    public static void openMenu(ServerPlayer player, String menuId, int pageIndex) {
        MenuDefinition def = CustomChestMenuRegistry.get(menuId);
        if (def == null) return;

        openMenu(player, def, pageIndex);
    }
}
