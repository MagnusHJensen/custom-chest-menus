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

package dk.magnusjensen.customchestmenus.registry;

import dk.magnusjensen.customchestmenus.Constants;
import dk.magnusjensen.customchestmenus.data.PlayerDataAttachment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class NeoforgeAttachmentRegistry {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Constants.MOD_ID);

    private static final Map<Class<?>, Supplier<? extends AttachmentType<?>>> TYPE_MAP = new HashMap<>();

    public static final Supplier<AttachmentType<PlayerDataAttachment>> PLAYER_DATA = registerAttachment(PlayerDataAttachment.class, "player_data",
        () -> AttachmentType.builder(() -> new PlayerDataAttachment()).build());

    private static <T> Supplier<AttachmentType<T>> registerAttachment(
        Class<T> clazz, String name, Supplier<AttachmentType<T>> factory) {
        Supplier<AttachmentType<T>> supplier = ATTACHMENT_TYPES.register(name, factory);
        TYPE_MAP.put(clazz, supplier);
        return supplier;
    }

    public static <T> Optional<AttachmentType<T>> findByClass(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        Supplier<AttachmentType<T>> supplier = (Supplier<AttachmentType<T>>) TYPE_MAP.get(clazz);
        return Optional.ofNullable(supplier).map(Supplier::get);
    }
}
