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

package dk.magnusjensen.customchestmenus.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dk.magnusjensen.customchestmenus.Constants;
import dk.magnusjensen.customchestmenus.Utils;
import dk.magnusjensen.customchestmenus.models.interactivity.InteractiveBlock;
import dk.magnusjensen.customchestmenus.models.interactivity.InteractiveEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChestMenuSavedData extends SavedData {

    public static final Codec<Map<BlockPos, InteractiveBlock>> MENU_BLOCKS_CODEC = Codec.unboundedMap(
        Codec.STRING.xmap(
            // Decode: "x,y,z" -> BlockPos
            s -> {
                try {
                    String[] parts = s.split(",");
                    return new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                } catch (Exception e) {
                    return BlockPos.ZERO; // fallback
                }
            },
            // Encode: BlockPos -> "x,y,z"
            pos -> pos.getX() + "," + pos.getY() + "," + pos.getZ()
        ),
        InteractiveBlock.CODEC
    );

    public static final Codec<Map<UUID, InteractiveEntity>> MENU_ENTITIES_CODEC = Codec.unboundedMap(
        Codec.STRING.xmap(
            // Decode: uuid string -> UUID
            s -> {
                try {
                    return UUID.fromString(s);
                } catch (Exception e) {
                    Constants.LOGGER.warn("Failed to parse UUID in saved data", e);
                    return UUID.fromString("00000000-0000-0000-0000-00000000000");
                    // 628b90e2-118a-43cf-8cca-0fa0a98f04de
                }
            },
            // Encode: UUID -> uuid string
            UUID::toString
        ),
        InteractiveEntity.CODEC
    );

    public static final Codec<ChestMenuSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        MENU_BLOCKS_CODEC
            .fieldOf("menuBlocks")
            .forGetter(ChestMenuSavedData::getMenuBlocks),
        MENU_ENTITIES_CODEC
            .fieldOf("menuEntities")
            .forGetter(ChestMenuSavedData::getMenuEntities)
    ).apply(instance, ChestMenuSavedData::new));

    public static final SavedDataType<ChestMenuSavedData> ID = new SavedDataType<>(
        Utils.modLoc("custom_chest_menus"),
        ChestMenuSavedData::new,
        CODEC,
        null
    );

    // Map of block positions to InteractiveBlock data classes, that when interacted with will open the menu
    private final Map<BlockPos, InteractiveBlock> menuBlocks = new HashMap<>();

    private final Map<UUID, InteractiveEntity> menuEntities = new HashMap<>();

    public ChestMenuSavedData(Map<BlockPos, InteractiveBlock> menuBlocks, Map<UUID, InteractiveEntity> menuEntities) {
        this.menuBlocks.putAll(menuBlocks);
        this.menuEntities.putAll(menuEntities);
    }

    public ChestMenuSavedData() {
    }

    public Map<BlockPos, InteractiveBlock> getMenuBlocks() {
        return Map.copyOf(menuBlocks);
    }

    public void addMenuBlock(InteractiveBlock block) {
        menuBlocks.put(block.pos(), block);
        setDirty();
    }

    public void removeMenuBlock(BlockPos pos) {
        menuBlocks.remove(pos);
        setDirty();
    }

    public Map<UUID, InteractiveEntity> getMenuEntities() {
        return Map.copyOf(menuEntities);
    }

    public void addMenuEntity(InteractiveEntity entity) {
        menuEntities.put(entity.entityUUID(), entity);
        setDirty();
    }

    public void removeMenuEntity(UUID entityUUID) {
        menuEntities.remove(entityUUID);
        setDirty();
    }

}
