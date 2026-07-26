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

package dk.magnusjensen.customchestmenus.platform;

import dk.magnusjensen.customchestmenus.Constants;
import dk.magnusjensen.customchestmenus.permissions.CCMPermission;
import dk.magnusjensen.customchestmenus.platform.services.IPermissionHelper;
import net.fabricmc.fabric.api.permission.v1.PermissionPredicates;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;

import java.util.function.Predicate;

public class FabricPermissionHelper implements IPermissionHelper {
    @Override
    public Predicate<CommandSourceStack> getCommandPermissionPredicate(CCMPermission permission) {
        return PermissionPredicates.require(Identifier.fromNamespaceAndPath(Constants.MOD_ID, permission.getNode()), permission.getDefaultLevel());
    }
}
