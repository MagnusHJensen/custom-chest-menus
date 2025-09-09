package dk.magnusjensen.customchestmenus.menu;

import dk.magnusjensen.customchestmenus.ActionExecutor;
import dk.magnusjensen.customchestmenus.models.MenuDefinition;
import dk.magnusjensen.customchestmenus.models.MenuItem;
import dk.magnusjensen.customchestmenus.models.MenuSize;
import dk.magnusjensen.customchestmenus.models.PagePayload;
import dk.magnusjensen.customchestmenus.registries.MenuRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CustomChestMenu extends AbstractContainerMenu {

    private final SimpleContainer backing;
    private final int slotCount;
    private final String customChestMenuId;
    private int pageIndex;

    // Client constructor
    public CustomChestMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readUtf(), PagePayload.read(extraData));
    }

    // Server constructor
    public CustomChestMenu(int containerId, Inventory playerInventory, String customChestMenuId, PagePayload payload) {
        super(MenuRegistry.CUSTOM_CHEST_MENU.get(), containerId);

        this.slotCount = payload.size() == MenuSize.SINGLE ? 27 : 54;
        this.backing = new SimpleContainer(slotCount);
        this.customChestMenuId = customChestMenuId;


        addGridSlots(backing, this.getRowCount());

        addPlayerInventoryLocked(playerInventory, this.getRowCount());
    }

    private void addGridSlots(Container container, int rows) {
        // Vanilla chest layout: left margin 8px, top margin 18px, step 18px
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                int index = row * 9 + col;
                int x = 8 + col * 18;
                int y = 18 + row * 18;
                addSlot(new LockedSlot(container, index, x, y));
            }
        }
    }

    // Optional: show player inventory but keep it locked (coords match vanilla)
    private void addPlayerInventoryLocked(Inventory inv, int gridRows) {
        // Baseline is just below the chest rows with a small gap.
        int baseY = 18 + gridRows * 18 + 14;   // 84 for 3 rows, ~140 for 6 rows
        // Main inventory (3 rows)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int x = 8 + col * 18;
                int y = baseY + row * 18;
                addSlot(new LockedSlot(inv, col + row * 9 + 9, x, y));
            }
        }
        // Hotbar
        int hotbarY = baseY + 58;              // 142-ish for 3 rows, 198-ish for 6 rows
        for (int col = 0; col < 9; ++col) {
            int x = 8 + col * 18;
            addSlot(new LockedSlot(inv, col, x, hotbarY));
        }
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        // Block all transfer-y click types outright
        if (clickType == ClickType.QUICK_MOVE   // shift-click
            || clickType == ClickType.SWAP         // number keys
            || clickType == ClickType.THROW        // Q
            || clickType == ClickType.QUICK_CRAFT  // drag paint
            || clickType == ClickType.PICKUP_ALL   // double-click collect to cursor
            || clickType == ClickType.CLONE) {     // middle click in creative
            return;
        }

        if (slotId >= 0 && slotId < this.slotCount && player instanceof ServerPlayer sp) {
            ActionExecutor.onClick(sp, customChestMenuId, this.pageIndex, slotId); // your action resolver (next/prev/teleport/close)
        }
        // Do NOT call super.clicked(...) or items will try to move.
    }

    /** Disable shift-click routing entirely. */
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    /** Disable all transfers (used by quickMoveStack and others). */
    @Override protected boolean moveItemStackTo(ItemStack stack, int start, int end, boolean reverse) { return false; }

    /** Disable double-click "pick all". */
    @Override public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) { return false; }

    /** Disable dragging items across slots to place. */
    @Override public boolean canDragTo(Slot slot) { return false; }

    @Override public boolean stillValid(Player player) { return true; }

    public void populateFromDefinition(MenuDefinition def, int pageIdx) {
        // clamp page index
        int pageCount = def.pages().size();
        if (pageCount == 0) {
            this.backing.clearContent();
            broadcastChanges();
            return;
        }
        this.pageIndex = Math.max(0, Math.min(pageIdx, pageCount - 1));

        // compute slot count for this menu
        int slotCount = sizeFor(def.size()); // 27 for SINGLE, 54 for DOUBLE

        // 1) clear & (optionally) lay down filler
        this.backing.clearContent();

        def.filler().ifPresent(f -> {
            ItemStack filler = makeStack(f.item(), f.name(), f.lore().orElse(List.of()));
            for (int i = 0; i < slotCount; i++) {
                this.backing.setItem(i, filler.copy());
            }
        });

        // 2) overwrite with explicit items on this page
        var page = def.pages().get(this.pageIndex);
        for (MenuItem it : page.items()) {
            int slot = it.slot();
            if (!inBounds(slot, slotCount)) {
                // out-of-bounds item; ignore but you can log if you want:
                // CustomChestMenus.LOGGER.warn("Item slot {} out of bounds (0..{}), menu={}, page={}", slot, slotCount-1, def.id(), this.pageIndex);
                continue;
            }
            ItemStack stack = makeStack(it.item(), it.name(), it.lore().orElse(List.of()));
            this.backing.setItem(slot, stack);
        }

        // 3) push changes to client
        broadcastChanges();
    }

    private static boolean inBounds(int slot, int slotCount) {
        return slot >= 0 && slot < slotCount;
    }

    private static int sizeFor(MenuSize size) {
        return (size == MenuSize.SINGLE) ? 27 : 54;
    }

    private ItemStack makeStack(ResourceLocation itemId, String name, List<String> lore) {
        // Resolve item, falling back to barrier if unknown
        Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(itemId)
            .orElse(net.minecraft.world.item.Items.BARRIER);

        ItemStack stack = new ItemStack(item);

        // Custom name
        if (name != null && !name.isEmpty()) {
            stack.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal(name));
        }

        // Lore (plug in your target-version implementation)
        applyLore(stack, lore);

        return stack;
    }

    /**
     * Apply lore lines to an ItemStack.
     * Implement this for your target MC version.
     *
     * For 1.21.x (Data Components), it roughly looks like:
     *   stack.set(DataComponents.LORE, new ItemLore(List<Component> lines, boolean alwaysShow));
     * For 1.20.4-and-earlier, you'd use the 'display' NBT tag with "Lore" (stringified JSON).
     */
    private static void applyLore(ItemStack stack, List<String> loreLines) {
        if (loreLines == null || loreLines.isEmpty()) return;

        // ---- 1.21.x (data components) example (fill in exact classes for your setup) ----
        // var components = loreLines.stream().map(Component::literal).toList();
        // stack.set(DataComponents.LORE, new ItemLore(components, false));

        // ---- Simple, version-agnostic fallback: add italics-gray via name hint (optional) ----
        // If you don't want to depend on version specifics yet, you can skip lore until you wire the proper API.
    }

    private static final class LockedSlot extends Slot {
        LockedSlot(Container container, int index, int x, int y) { super(container, index, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
        @Override public int getMaxStackSize(ItemStack stack) { return 0; }           // belt-and-suspenders
        @Override public boolean isActive() { return true; }                          // clickable area remains active
        @Override public boolean isHighlightable() { return false; }                  // prevent highlight flicker (1.21+)
    }

    public int getRowCount() { return (this.slotCount == 27) ? 3 : 6; }

    public String menuId() { return this.customChestMenuId; }
}
