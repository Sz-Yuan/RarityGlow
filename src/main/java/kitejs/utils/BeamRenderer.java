package kitejs.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import kitejs.RarityGlow;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class BeamRenderer implements LevelRenderEvents.AfterTranslucentFeatures {

    private record BeamCameraRel(float fx, float fy, float fz, float r, float g, float b) {
        static BeamCameraRel forEntity(ItemEntity entity, float partialTicks, double beamOffset, Vec3 cameraPos) {
            double vx = Mth.lerp(partialTicks, entity.xOld, entity.getX());
            double vy = Mth.lerp(partialTicks, entity.yOld, entity.getY());
            double vz = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
            float ageInTicks = entity.tickCount + partialTicks;
            float bob = Mth.sin(ageInTicks / 10.0F + entity.bobOffs) * 0.1F + 0.1F;
            // Camera-relative coordinates — computed in double, stored as float.
            // Values are small (within render distance), so float precision is fine.
            float fx = (float) (vx - cameraPos.x);
            float fy = (float) (vy - cameraPos.y + beamOffset + bob);
            float fz = (float) (vz - cameraPos.z);

            int packedColor = ItemRarityHelper.getBeamColorIfEnabled(entity.getItem());
            if (packedColor == 0) return null;
            float r = ((packedColor >> 16) & 0xFF) / 255f;
            float g = ((packedColor >> 8) & 0xFF) / 255f;
            float b = (packedColor & 0xFF) / 255f;

            return new BeamCameraRel(fx, fy, fz, r, g, b);
        }
    }

    @Override
    public void afterTranslucentFeatures(@NonNull LevelRenderContext context) {
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) return;

        var config = RarityGlow.CONFIG;
        if (!config.beamEnabled) return;

        double beamHeight = config.beam.beamHeight;
        double beamOffset = config.beam.beamOffset;
        if (beamHeight <= 0) return;

        // Early exit: no beam enabled for any rarity
        if (!config.common.beamEnabled
                && !config.uncommon.beamEnabled
                && !config.rare.beamEnabled
                && !config.epic.beamEnabled) {
            return;
        }

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cam = camera.position();

        PoseStack matrices = context.poseStack();
        matrices.pushPose();

        MultiBufferSource.BufferSource bufferSource = context.bufferSource();

        var pose = matrices.last().pose();

        // Single pass: hexagram + 3D beam pillar (using quads)
        VertexConsumer vc = bufferSource.getBuffer(RenderTypes.debugQuads());
        for (var entity : world.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity itemEntity)) continue;
            float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
            BeamCameraRel data = BeamCameraRel.forEntity(itemEntity, partialTicks, beamOffset, cam);
            if (data == null) continue;
            float dx = data.fx;
            float dy = data.fy;
            float dz = data.fz;
            double distSq = (double) dx * dx + (double) dy * dy + (double) dz * dz;
            double maxDist = config.beam.maxRenderDistance;
            if (distSq > maxDist * maxDist) continue;

            // Honeycomb floor pattern (toggleable)
            if (config.beam.patternEnabled) {
                // Honeycomb: center hexagon + 6 surrounding
                float cellRadius = 0.09f;
                float cellDist = cellRadius * (float) Math.sqrt(3);
                float cellZOff = cellRadius * 1.5f;
                float[][] cellCenters = {
                        {0, 0},
                        {cellDist, 0},
                        {cellDist * 0.5f, -cellZOff},
                        {-cellDist * 0.5f, -cellZOff},
                        {-cellDist, 0},
                        {-cellDist * 0.5f, cellZOff},
                        {cellDist * 0.5f, cellZOff},
                };
                float[] angles = {
                        (float) (Math.PI / 6), (float) (Math.PI / 2), (float) (5 * Math.PI / 6),
                        (float) (7 * Math.PI / 6), (float) (3 * Math.PI / 2), (float) (11 * Math.PI / 6)
                };
                float barWidth = 0.008f;
                float hexY = dy - (float) beamOffset + 0.005f;
                for (float[] cell : cellCenters) {
                    float cx = dx + cell[0];
                    float cz = dz + cell[1];

                    // Precompute 6 vertices
                    float[] vx = new float[6];
                    float[] vz = new float[6];
                    for (int i = 0; i < 6; i++) {
                        vx[i] = cx + cellRadius * Mth.cos(angles[i]);
                        vz[i] = cz + cellRadius * Mth.sin(angles[i]);
                    }

                    // Fill: 3 quads with 50% opacity
                    int[][] fillQuads = {{0, 1, 2}, {2, 3, 4}, {4, 5, 0}};
                    for (int[] tri : fillQuads) {
                        vc.addVertex(pose, cx, hexY, cz).setColor(data.r, data.g, data.b, 0.5f);
                        vc.addVertex(pose, vx[tri[0]], hexY, vz[tri[0]]).setColor(data.r, data.g, data.b, 0.5f);
                        vc.addVertex(pose, vx[tri[1]], hexY, vz[tri[1]]).setColor(data.r, data.g, data.b, 0.5f);
                        vc.addVertex(pose, vx[tri[2]], hexY, vz[tri[2]]).setColor(data.r, data.g, data.b, 0.5f);
                    }

                    // Outline: 6 opaque thin bars
                    for (int i = 0; i < 6; i++) {
                        int next = (i + 1) % 6;
                        float ddx = vx[next] - vx[i];
                        float ddz = vz[next] - vz[i];
                        float len = Mth.sqrt(ddx * ddx + ddz * ddz);
                        if (len < 0.001f) continue;
                        float nx = ddz / len * barWidth * 0.5f;
                        float nz = -ddx / len * barWidth * 0.5f;
                        float yBot = hexY - barWidth * 0.5f;
                        float yTop = hexY + barWidth * 0.5f;
                        vc.addVertex(pose, vx[i] - nx, yBot, vz[i] - nz).setColor(data.r, data.g, data.b, 1.0f);
                        vc.addVertex(pose, vx[next] - nx, yBot, vz[next] - nz).setColor(data.r, data.g, data.b, 1.0f);
                        vc.addVertex(pose, vx[next] + nx, yTop, vz[next] + nz).setColor(data.r, data.g, data.b, 1.0f);
                        vc.addVertex(pose, vx[i] + nx, yTop, vz[i] + nz).setColor(data.r, data.g, data.b, 1.0f);
                    }
                }
            } // end patternEnabled
            float beamHeightF = (float) beamHeight;
            float baseRadius = config.beam.beamWidth * 0.01f;
            int sides = 8;
            int vSegments = 12;

            for (int j = 0; j < vSegments; j++) {
                float t0 = (float) j / vSegments;
                float t1 = (float) (j + 1) / vSegments;

                float alpha0 = 1.0f - t0 * 0.95f;
                float alpha1 = 1.0f - t1 * 0.95f;
                float radius0 = baseRadius * (1.0f - t0 * 0.95f);
                float radius1 = baseRadius * (1.0f - t1 * 0.95f);
                float y0 = dy + t0 * beamHeightF;
                float y1 = dy + t1 * beamHeightF;

                for (int i = 0; i < sides; i++) {
                    float angle0 = (float) i / sides * (float) (Math.PI * 2);
                    float angle1 = (float) (i + 1) / sides * (float) (Math.PI * 2);

                    float x00 = dx + radius0 * Mth.cos(angle0);
                    float z00 = dz + radius0 * Mth.sin(angle0);
                    float x01 = dx + radius1 * Mth.cos(angle0);
                    float z01 = dz + radius1 * Mth.sin(angle0);
                    float x10 = dx + radius0 * Mth.cos(angle1);
                    float z10 = dz + radius0 * Mth.sin(angle1);
                    float x11 = dx + radius1 * Mth.cos(angle1);
                    float z11 = dz + radius1 * Mth.sin(angle1);

                    vc.addVertex(pose, x00, y0, z00).setColor(data.r, data.g, data.b, alpha0);
                    vc.addVertex(pose, x10, y0, z10).setColor(data.r, data.g, data.b, alpha0);
                    vc.addVertex(pose, x11, y1, z11).setColor(data.r, data.g, data.b, alpha1);
                    vc.addVertex(pose, x01, y1, z01).setColor(data.r, data.g, data.b, alpha1);
                }
            }
        }

        bufferSource.endBatch();
        matrices.popPose();
    }
}
