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

package dk.magnusjensen.customchestmenus.permissions;

import net.minecraft.server.permissions.PermissionLevel;

public enum CCMPermission {
    OPEN("ccm.open", PermissionLevel.ALL),
    OPEN_OTHERS("ccm.open.others", PermissionLevel.ADMINS),

    BIND("ccm.bind", PermissionLevel.ADMINS),
    UNBIND("ccm.unbind", PermissionLevel.ADMINS),
    STOP_BIND("ccm.stop-binding", PermissionLevel.ADMINS),
    LIST_BINDS("ccm.list-binds", PermissionLevel.GAMEMASTERS),
    BIND_OVERLAY("ccm.bind-overlay", PermissionLevel.ADMINS),

    RELOAD("ccm.reload", PermissionLevel.ADMINS);


    private final String node;
    private final PermissionLevel defaultLevel;

    CCMPermission(String node, PermissionLevel defaultLevel) {
        this.node = node;
        this.defaultLevel = defaultLevel;
    }

    public String getNode() {
        return node;
    }

    public PermissionLevel getDefaultLevel() {
        return defaultLevel;
    }
}
