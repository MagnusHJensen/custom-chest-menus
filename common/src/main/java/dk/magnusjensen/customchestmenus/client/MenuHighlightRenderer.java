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

package dk.magnusjensen.customchestmenus.client;

import dk.magnusjensen.customchestmenus.data.PlayerDataAttachment;
import dk.magnusjensen.customchestmenus.platform.Services;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MenuHighlightRenderer {
    private static final double MAX_DIST_SQR = 64.0 * 64.0;

    public static void renderLevelOverlay() {
        var playerData = Services.ATTACHMENT.<PlayerDataAttachment>getPlayerAttachment(Minecraft.getInstance().player, PlayerDataAttachment.ID);

        if (!playerData.hasOverlay()) {
            return;
        }

        var boundBlocks = playerData.boundBlocks();
        var boundEntities = playerData.boundEntities();


        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        Vec3 cam = camera.position();

        for (var pos : boundBlocks) {

            // distance culling
            double dx = pos.getX() + 0.5 - cam.x;
            double dy = pos.getY() + 0.5 - cam.y;
            double dz = pos.getZ() + 0.5 - cam.z;
            if (dx * dx + dy * dy + dz * dz > MAX_DIST_SQR) continue;

            // move box into camera-relative space
            var cyan = ARGB.color(255, 0, 255, 255);
            var blockGizmo = Gizmos.cuboid(pos, GizmoStyle.stroke(cyan));
            blockGizmo.setAlwaysOnTop();
        }

        // -------- Entity outlines --------

        for (var entityID : boundEntities) {

            Entity entity = Minecraft.getInstance().level.getEntity(entityID);
            if (entity == null) continue; // Ensure the entity exists

            double distSq = entity.distanceToSqr(cam.x, cam.y, cam.z);
            if (distSq > MAX_DIST_SQR) continue; // Distance culling

            // Get the entity's bounding box
            AABB entityBox = entity.getBoundingBox();

            // Render the outline
            var cyan = ARGB.color(255, 0, 255, 255);
            var entityGizmo = Gizmos.cuboid(entityBox, GizmoStyle.stroke(cyan));
            entityGizmo.setAlwaysOnTop();
        }
    }
}
