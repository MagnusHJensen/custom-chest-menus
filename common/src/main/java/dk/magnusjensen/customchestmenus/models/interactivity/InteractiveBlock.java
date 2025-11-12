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
import net.minecraft.resources.ResourceLocation;

/**
 * Data class representing a block that can be interacted with to open a custom chest menu.
 * @param blockType A resource location of the block type bound to, e.g., "minecraft:chest".
 */
public record InteractiveBlock(String menuId, BlockPos pos, ResourceLocation blockType){
    public static final Codec<InteractiveBlock> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("menuId").forGetter(InteractiveBlock::menuId),
        BlockPos.CODEC.fieldOf("pos").forGetter(InteractiveBlock::pos),
        ResourceLocation.CODEC.fieldOf("blockType").forGetter(InteractiveBlock::blockType)
    ).apply(instance, InteractiveBlock::new));
}
