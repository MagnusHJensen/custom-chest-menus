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

package dk.magnusjensen.customchestmenus;

import dk.magnusjensen.customchestmenus.registry.provider.RegistrationProvider;
import dk.magnusjensen.customchestmenus.registry.provider.RegistryObject;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.*;
import java.util.function.Supplier;

public class NeoforgeRegistrationFactory implements RegistrationProvider.Factory {

    @Override
    public <T> RegistrationProvider<T> create(Registry<T> registry, String modId) {
        final Optional<? extends ModContainer> optionalModContainer = ModList.get().getModContainerById(modId);

        if (optionalModContainer.isEmpty()) throw new NullPointerException("Cannot find ModContainer for id: " + modId);

        final ModContainer modContainer = optionalModContainer.get();
        if (modContainer instanceof FMLModContainer fmlModContainer) {
            final DeferredRegister<T> register = DeferredRegister.create(registry.key(), modId);
            register.register(Objects.requireNonNull(fmlModContainer.getEventBus()));
            return new Provider<>(modId, register);

        } else throw new ClassCastException("The Container of the Mod " + modId + " is not a NeoForge one!");
    }

    private static class Provider<T> implements RegistrationProvider<T> {
        private final String modId;
        private final DeferredRegister<T> registry;

        private final Set<RegistryObject<T>> entries = new HashSet<>();
        private final Set<RegistryObject<T>> entriesView = Collections.unmodifiableSet(entries);

        private Provider(String modId, DeferredRegister<T> registry) {
            this.modId = modId;
            this.registry = registry;
        }

        @Override
        public String getModId() {
            return modId;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> supplier) {
            final var obj = registry.<I>register(name, supplier);
            final var ro = new RegistryObject<I>() {

                @Override
                public ResourceKey<I> getResourceKey() {
                    return (ResourceKey<I>) obj.getKey();
                }

                @Override
                public Identifier getId() {
                    return obj.getId();
                }

                @Override
                public I get() {
                    return obj.get();
                }
            };
            entries.add((RegistryObject<T>) ro);
            return ro;
        }

        @Override
        public Set<RegistryObject<T>> getEntries() {
            return entriesView;
        }
    }
}
