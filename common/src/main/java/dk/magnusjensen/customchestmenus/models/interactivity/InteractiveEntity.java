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

package dk.magnusjensen.customchestmenus.models.interactivity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/**
 * Data class representing an entity that can be interacted with to open a custom chest menu.
 * @param entityUUID The UUID of the entity the menu is bound to.
 * @param lastSeen The block position of the entity when it was last seen, this might not be entirely correct, but we do our best efforts to keep it in sync.
 * @param entityType A resource location of the entity type bound to, e.g., "minecraft:villager".
 */
public record InteractiveEntity(
    String menuId,
    UUID entityUUID,
    BlockPos lastSeen,
    Identifier entityType
){

    public static final Codec<InteractiveEntity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("menuId").forGetter(InteractiveEntity::menuId),
        UUIDUtil.CODEC.fieldOf("entityUUID").forGetter(InteractiveEntity::entityUUID),
        BlockPos.CODEC.fieldOf("lastSeen").forGetter(InteractiveEntity::lastSeen),
        Identifier.CODEC.fieldOf("entityType").forGetter(InteractiveEntity::entityType)
    ).apply(instance, InteractiveEntity::new));

    public InteractiveEntity setLastSeen(BlockPos lastSeen) {
        return new InteractiveEntity(menuId, entityUUID, lastSeen, entityType);
    }
}
