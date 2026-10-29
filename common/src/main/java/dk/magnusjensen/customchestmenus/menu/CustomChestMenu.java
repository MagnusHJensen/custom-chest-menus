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

package dk.magnusjensen.customchestmenus.menu;

import dk.magnusjensen.customchestmenus.ActionExecutor;
import dk.magnusjensen.customchestmenus.models.*;
import dk.magnusjensen.customchestmenus.models.v2.MenuSize;
import dk.magnusjensen.customchestmenus.platform.Services;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CustomChestMenu extends AbstractContainerMenu {

    private final SimpleContainer backing;
    private final int slotCount;
    private final String customChestMenuId;
    private int pageIndex;
    public final MenuBackground background;
    public final MenuSize menuSize;
    public final MenuLayout layout;

    // Client constructor
    public CustomChestMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, Payload.STREAM_CODEC.decode(extraData));
    }

    public CustomChestMenu(int containerId, Inventory playerInventory, Payload extraData) {
        this(containerId, playerInventory, extraData.customChestMenuId(), extraData.pagePayload());
    }

    // Server constructor
    public CustomChestMenu(int containerId, Inventory playerInventory, String customChestMenuId, PagePayload payload) {
        super(Services.REGISTRY.getCustomChestMenuType(), containerId);

        this.slotCount = payload.size().slotCount();
        this.backing = new SimpleContainer(slotCount);
        this.customChestMenuId = customChestMenuId;
        this.background = payload.background();
        this.menuSize = payload.size();
        this.layout = MenuLayout.of(payload.size(), payload.background());

        addGridSlots(backing, payload.size().getRows(), payload.size().getColumns());

        if (layout.showPlayerInventory()) {
            // Only add slots if we show player inventory
            addPlayerInventoryLocked(playerInventory);
        }
    }

    private void addGridSlots(Container container, int rows, int columns) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int x = MenuLayout.SLOT_START + col * MenuLayout.SLOT;
                int y = MenuLayout.GRID_Y + row * MenuLayout.SLOT;
                addSlot(new LockedSlot(container, row * columns + col, x, y));
            }
        }
    }

    // Optional: show player inventory but keep it locked (coords match vanilla)
    private void addPlayerInventoryLocked(Inventory inv) {
        // Main inventory (3 rows)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int x = MenuLayout.SLOT_START + col * MenuLayout.SLOT;
                int y = layout.invY() + row * MenuLayout.SLOT;
                addSlot(new LockedSlot(inv, col + row * 9 + 9, x, y));
            }
        }
        // Hotbar
        for (int col = 0; col < 9; ++col) {
            int x = MenuLayout.SLOT_START + col * MenuLayout.SLOT;
            addSlot(new LockedSlot(inv, col, x, layout.hotbarY()));
        }
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        // Shift-click is not a transfer here, it is the craft all signal, so it is let through.
        if (clickType != ClickType.PICKUP && clickType != ClickType.QUICK_MOVE) {
            // Block all non pickup interactions
            return;
        }

        if (slotId >= 0 && slotId < this.slotCount && player instanceof ServerPlayer sp) {
            ActionExecutor.onClick(sp, customChestMenuId, this.pageIndex, slotId, clickType == ClickType.QUICK_MOVE);
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
        int slotCount = def.size().slotCount();

        // 1) clear & (optionally) lay down filler
        this.backing.clearContent();

        def.filler().ifPresent(f -> {
            ItemStack filler = f.makeItemStack();
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
            ItemStack stack = it.makeItemStack();
            this.backing.setItem(slot, stack);
        }

        // 3) push changes to client
        broadcastChanges();
    }

    private static boolean inBounds(int slot, int slotCount) {
        return slot >= 0 && slot < slotCount;
    }

    private static final class LockedSlot extends Slot {
        LockedSlot(Container container, int index, int x, int y) { super(container, index, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
        @Override public int getMaxStackSize(ItemStack stack) { return 0; }           // belt-and-suspenders
        @Override public boolean isActive() { return true; }                          // clickable area remains active
        @Override public boolean isHighlightable() { return false; }                  // prevent highlight flicker (1.21+)
    }

    public String menuId() { return this.customChestMenuId; }

    public record Payload(String customChestMenuId, PagePayload pagePayload) {
        public static StreamCodec<RegistryFriendlyByteBuf, Payload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            Payload::customChestMenuId,
            PagePayload.STREAM_CODEC,
            Payload::pagePayload,
            Payload::new
        );
    }
}
