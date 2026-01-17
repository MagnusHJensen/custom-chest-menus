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

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public class PlayerCapability extends PlayerDataAttachment implements ICapabilityProvider {
    public static final Capability<PlayerCapability> PLAYER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    private final LazyOptional<PlayerCapability> playerCapabilityLazyOptional;

    public PlayerCapability() {
        this.playerCapabilityLazyOptional = LazyOptional.of(() -> this);
    }

    @Override
    public  <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction direction) {
        if (capability == PLAYER_CAPABILITY) {
            return playerCapabilityLazyOptional.cast();
        }
        return LazyOptional.empty();
    }
}
