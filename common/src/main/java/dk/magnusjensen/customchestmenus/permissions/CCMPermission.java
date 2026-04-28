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

public enum CCMPermission {
    OPEN("ccm.open", 0),
    OPEN_OTHERS("ccm.open.others", 4),

    BIND("ccm.bind", 4),
    UNBIND("ccm.unbind", 4),
    STOP_BIND("ccm.stop-binding", 4),
    LIST_BINDS("ccm.list-binds", 2),
    BIND_OVERLAY("ccm.bind-overlay", 4),

    RELOAD("ccm.reload", 4);


    private final String node;
    private final int defaultLevel;

    CCMPermission(String node, int defaultLevel) {
        this.node = node;
        this.defaultLevel = defaultLevel;
    }

    public String getNode() {
        return node;
    }

    public int getDefaultLevel() {
        return defaultLevel;
    }
}
