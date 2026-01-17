/*
 *     Custom Chest Menus, a Minecraft mod that allows servers to create custom chest menus.
 *     Copyright (c) 2026  legenden (MagnusHJensen)
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

package dk.magnusjensen.customchestmenus.client;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class ClientMemory {

    private static final ClientMemory INSTANCE = new ClientMemory(false, new ArrayList<>(), new ArrayList<>());

    private List<BlockPos> boundBlocks = new ArrayList<>();
    private List<Integer> boundEntities = new ArrayList<>(); // List of entity ID's
    private boolean hasOverlay = false;

    private ClientMemory(boolean hasOverlay, List<BlockPos> boundBlocks, List<Integer> boundEntities) {
        this.hasOverlay = hasOverlay;
        this.boundBlocks = boundBlocks;
        this.boundEntities = boundEntities;
    }

    public static ClientMemory getInstance() {
        return INSTANCE;
    }

    public static ClientMemory getInstance(boolean hasOverlay, List<BlockPos> boundBlocks, List<Integer> boundEntities) {
        INSTANCE.hasOverlay = hasOverlay;
        INSTANCE.boundBlocks = boundBlocks;
        INSTANCE.boundEntities = boundEntities;
        return INSTANCE;
    }


    public List<BlockPos> boundBlocks() {
        return new ArrayList<>(boundBlocks);
    }

    public List<Integer> boundEntities() {
        return new ArrayList<>(boundEntities);
    }

    public void setHasOverlay(boolean hasOverlay) {
        this.hasOverlay = hasOverlay;
    }

    public boolean hasOverlay() {
        return hasOverlay;
    }
}
