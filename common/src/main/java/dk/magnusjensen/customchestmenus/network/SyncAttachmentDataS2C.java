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

package dk.magnusjensen.customchestmenus.network;


import dk.magnusjensen.customchestmenus.Utils;
import dk.magnusjensen.customchestmenus.data.PlayerDataAttachment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class SyncAttachmentDataS2C implements CustomPacket {

    public static final ResourceLocation ID = Utils.modLoc("sync_attachment_data");
    public final PlayerDataAttachment attachment;

    public SyncAttachmentDataS2C(PlayerDataAttachment attachment) {
        this.attachment = attachment;
    }
    public SyncAttachmentDataS2C(final FriendlyByteBuf buf) {
        var compoundTag = buf.readNbt();
        var tag = compoundTag.get("encoded");
        var dataAttachment = PlayerDataAttachment.SYNC_CODEC.decode(NbtOps.INSTANCE, tag)
            .result()
            .orElseThrow(() -> new IllegalStateException("Failed to decode PlayerDataAttachment from packet."));
        this.attachment = dataAttachment.getFirst();
    }

    public void write(FriendlyByteBuf buffer) {
        var encoded = PlayerDataAttachment.SYNC_CODEC.encodeStart(NbtOps.INSTANCE, this.attachment)
            .result()
            .orElseThrow(() -> new IllegalStateException("Failed to encode PlayerDataAttachment for packet."));

        var compoundTag = new CompoundTag();
        compoundTag.put("encoded", encoded);
        buffer.writeNbt(compoundTag);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
