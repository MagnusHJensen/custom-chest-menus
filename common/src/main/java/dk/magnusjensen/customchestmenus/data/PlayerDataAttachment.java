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

package dk.magnusjensen.customchestmenus.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dk.magnusjensen.customchestmenus.Utils;
import dk.magnusjensen.customchestmenus.client.ClientMemory;
import dk.magnusjensen.customchestmenus.models.interactivity.BindingMode;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores and serializes per-player data related to custom chest menus.
 */
public class PlayerDataAttachment {
    public static final ResourceLocation ID = Utils.modLoc("player_data");

    // Server saving serializing/deserializing CODECS
    public static final MapCodec<PlayerDataAttachment> MAP_CODEC = MapCodec.unit(new PlayerDataAttachment());
    /*public static final MapCodec<PlayerDataAttachment> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.BOOL.fieldOf("inBindingMode").forGetter(PlayerDataAttachment::isInBindingMode)
    ).apply(instance, PlayerDataAttachment::new));*/
    public static final Codec<PlayerDataAttachment> CODEC = MAP_CODEC.codec();

    // Server <-> Client syncing CODECS
    public static final Codec<PlayerDataAttachment> SYNC_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("hasOverlay").forGetter(PlayerDataAttachment::hasOverlay),
        BlockPos.CODEC.listOf().fieldOf("bound_blocks").forGetter(PlayerDataAttachment::boundBlocks),
        Codec.INT.listOf().fieldOf("bound_entities").forGetter(PlayerDataAttachment::boundEntities)
        ).apply(instance, PlayerDataAttachment::new));


    //region Server only fields
    /**
     * Whether the player is currently in binding mode, which means:
     * Right-clicking on any block or Entity will bind a menu to it.
     * If the same menu is already bound to that target, the binding is removed instead.
     * If another menu is bound, then it prompts a message and does nothing.
     */
    private BindingMode inBindingMode = BindingMode.NONE;
    private String menuToBind = null;
    //endregion

    //region Client synced fields
    private List<BlockPos> boundBlocks = new ArrayList<>();
    private List<Integer> boundEntities = new ArrayList<>(); // List of entity ID's
    private boolean hasOverlay = false;
    //endregion

    public PlayerDataAttachment() {
    }

    // Client constructor
    public PlayerDataAttachment(boolean hasOverlay, List<BlockPos> boundBlocks, List<Integer> boundEntities) {
        this.hasOverlay = hasOverlay;
        this.boundBlocks = boundBlocks;
        this.boundEntities = boundEntities;

        // Also set ClientMemory to be compatible with NeoForge.
        ClientMemory.getInstance(hasOverlay, boundBlocks, boundEntities);
    }

    public ResourceLocation getId() {
        return ID;
    }

    //region Server only methods
    public boolean isBindingMode() {
        return inBindingMode == BindingMode.BIND;
    }

    public boolean isUnbindingMode() {
        return inBindingMode == BindingMode.UNBIND;
    }

    public BindingMode getBindingMode() {
        return inBindingMode;
    }

    public String getMenuToBind() {
        return menuToBind;
    }

    public void activateBindingMode(String menuId) {
        this.inBindingMode = BindingMode.BIND;
        this.menuToBind = menuId;
    }

    public void activateUnbindingMode() {
        this.inBindingMode = BindingMode.UNBIND;
        this.menuToBind = null;
    }

    public void disableBindingMode() {
        this.inBindingMode = BindingMode.NONE;
        this.menuToBind = null;
    }

    // Setters for client methods, so that it syncs the values when we set the data attachment.
    public void setBoundBlocks(List<BlockPos> boundBlocks) {
        this.boundBlocks = boundBlocks;
    }

    public void setBoundEntities(List<Integer> boundEntities) {
        this.boundEntities = boundEntities;
    }

    public void resetServerSideVersionData() {
        this.boundBlocks = new ArrayList<>();
        this.boundEntities = new ArrayList<>();
    }

    // Only copies server attributes
    public void copyFrom(PlayerDataAttachment existing) {
        this.inBindingMode = existing.getBindingMode();
        this.menuToBind = existing.getMenuToBind();
        this.hasOverlay = existing.hasOverlay();
        this.boundBlocks = existing.boundBlocks;
        this.boundEntities = existing.boundEntities;
    }
    //endregion

    //region Client available methods
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
    //endregion



}
