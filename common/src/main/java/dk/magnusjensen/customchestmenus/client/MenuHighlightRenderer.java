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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dk.magnusjensen.customchestmenus.data.PlayerDataAttachment;
import dk.magnusjensen.customchestmenus.platform.Services;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MenuHighlightRenderer {
    private static final double MAX_DIST_SQR = 64.0 * 64.0;

    public static void renderLevelOverlay(
        PoseStack pose
    ) {
        if (pose == null) {
            return;
        }
        var playerData = Services.ATTACHMENT.<PlayerDataAttachment>getPlayerAttachment(Minecraft.getInstance().player, PlayerDataAttachment.ID);

        if (!playerData.hasOverlay()) {
            return;
        }

        var boundBlocks = playerData.boundBlocks();
        var boundEntities = playerData.boundEntities();


        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        var buffers = Minecraft.getInstance().renderBuffers().bufferSource();

        Vec3 cam = camera.getPosition();

        // -------- Block wire boxes --------
        VertexConsumer lineConsumer = buffers.getBuffer(RenderType.lines());

        for (var pos : boundBlocks) {

            // distance culling
            double dx = pos.getX() + 0.5 - cam.x;
            double dy = pos.getY() + 0.5 - cam.y;
            double dz = pos.getZ() + 0.5 - cam.z;
            if (dx * dx + dy * dy + dz * dz > MAX_DIST_SQR) continue;

            // move box into camera-relative space
            AABB box = new AABB(pos).inflate(0.002).move(-cam.x, -cam.y, -cam.z);

            ShapeRenderer.renderLineBox(pose.last(), lineConsumer, box,
                0.0f, 1.0f, 1.0f, 1.0f); // cyan
        }

        // -------- Entity outlines --------

        for (var entityID : boundEntities) {

            Entity entity = Minecraft.getInstance().level.getEntity(entityID);
            if (entity == null) continue; // Ensure the entity exists

            double distSq = entity.distanceToSqr(cam.x, cam.y, cam.z);
            if (distSq > MAX_DIST_SQR) continue; // Distance culling

            // Get the entity's bounding box
            AABB entityBox = entity.getBoundingBox().inflate(0.1); // Slightly inflate for better visibility
            AABB relativeBox = entityBox.move(-cam.x, -cam.y, -cam.z);

            // Render the outline

            VertexConsumer outlineConsumer = buffers.getBuffer(RenderType.lines());
            ShapeRenderer.renderLineBox(pose.last(), outlineConsumer, relativeBox,
                0.0f, 1.0f, 1.0f, 1.0f); // Cyan outline
        }
    }
}
