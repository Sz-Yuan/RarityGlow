package kitejs.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import kitejs.RarityGlow;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class BeamRenderer implements LevelRenderEvents.AfterTranslucentFeatures {

    // --- Honeycomb constants (cellRadius = 0.09f) ---
    private static final float CELL_RADIUS = 0.09f;
    private static final float[][] HEX_CENTERS;
    private static final float[] HEX_COS = new float[6];
    private static final float[] HEX_SIN = new float[6];
    private static final int[][] FILL_QUADS = {{0, 1, 2}, {2, 3, 4}, {4, 5, 0}};

    // --- Beam pillar constants (8 sides) ---
    private static final int BEAM_SIDES = 8;
    private static final float[] BEAM_COS = new float[BEAM_SIDES];
    private static final float[] BEAM_SIN = new float[BEAM_SIDES];

    static {
        float cellDist = CELL_RADIUS * (float) Math.sqrt(3);
        float cellZOff = CELL_RADIUS * 1.5f;
        HEX_CENTERS = new float[][] {
                {0, 0},
                {cellDist, 0},
                {cellDist * 0.5f, -cellZOff},
                {-cellDist * 0.5f, -cellZOff},
                {-cellDist, 0},
                {-cellDist * 0.5f, cellZOff},
                {cellDist * 0.5f, cellZOff},
        };

        float[] hexAngles = {
                (float) (Math.PI / 6), (float) (Math.PI / 2), (float) (5 * Math.PI / 6),
                (float) (7 * Math.PI / 6), (float) (3 * Math.PI / 2), (float) (11 * Math.PI / 6)
        };
        for (int i = 0; i < 6; i++) {
            HEX_COS[i] = Mth.cos(hexAngles[i]);
            HEX_SIN[i] = Mth.sin(hexAngles[i]);
        }

        for (int i = 0; i < BEAM_SIDES; i++) {
            float angle = (float) i / BEAM_SIDES * (float) (Math.PI * 2);
            BEAM_COS[i] = Mth.cos(angle);
            BEAM_SIN[i] = Mth.sin(angle);
        }
    }

    private record BeamCameraRel(float fx, float fy, float fz, float r, float g, float b) {
        static @Nullable BeamCameraRel forEntity(ItemEntity entity, float partialTicks, double beamOffset, Vec3 cameraPos) {
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
    public void afterTranslucentFeatures(LevelRenderContext context) {
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) return;

        var config = RarityGlow.CONFIG;
        if (!config.beamEnabled) return;

        double beamHeight = config.beam.beamHeight;
        double beamOffset = config.beam.beamOffset;
        if (beamHeight <= 0) return;

        if (!ItemRarityHelper.anyBeamEnabled()) return;

        Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
        Vec3 cam = camera.position();

        float beamHeightF = (float) beamHeight;
        float baseRadius = config.beam.beamWidth * 0.01f;
        int vSegments = 12;
        double maxDist = config.maxRenderDistance;
        double maxDistSq = maxDist * maxDist;
        boolean patternEnabled = config.beam.patternEnabled;
        float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);

        PoseStack matrices = context.poseStack();
        matrices.pushPose();

        SubmitNodeCollector submitNodeCollector = context.submitNodeCollector();

        submitNodeCollector.submitCustomGeometry(matrices, RenderTypes.debugQuads(), (pose, vc) -> {
            for (var entity : world.entitiesForRendering()) {
                if (!(entity instanceof ItemEntity itemEntity)) continue;
                BeamCameraRel data = BeamCameraRel.forEntity(itemEntity, partialTicks, beamOffset, cam);
                if (data == null) continue;

                double distSq = (double) data.fx * data.fx + (double) data.fy * data.fy + (double) data.fz * data.fz;
                if (distSq > maxDistSq) continue;

                if (patternEnabled) {
                    drawHoneycomb(pose, vc, data, beamOffset);
                }
                drawBeamPillar(pose, vc, data, beamHeightF, baseRadius, vSegments);
            }
        });

        matrices.popPose();
    }

    private static void drawHoneycomb(PoseStack.Pose pose, VertexConsumer vc, BeamCameraRel data, double beamOffset) {
        float barWidth = 0.008f;
        float hexY = data.fy - (float) beamOffset + 0.005f;

        // Reusable vertex arrays
        float[] vx = new float[6];
        float[] vz = new float[6];

        for (float[] cell : HEX_CENTERS) {
            float cx = data.fx + cell[0];
            float cz = data.fz + cell[1];

            for (int i = 0; i < 6; i++) {
                vx[i] = cx + CELL_RADIUS * HEX_COS[i];
                vz[i] = cz + CELL_RADIUS * HEX_SIN[i];
            }

            // Fill: 3 quads with 50% opacity
            for (int[] tri : FILL_QUADS) {
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
    }

    private static void drawBeamPillar(PoseStack.Pose pose, VertexConsumer vc, BeamCameraRel data,
                                       float beamHeightF, float baseRadius, int vSegments) {
        for (int j = 0; j < vSegments; j++) {
            float t0 = (float) j / vSegments;
            float t1 = (float) (j + 1) / vSegments;

            float fade = 1.0f - t0 * 0.95f;
            float fadeNext = 1.0f - t1 * 0.95f;
            float radius0 = baseRadius * fade;
            float radius1 = baseRadius * fadeNext;
            float y0 = data.fy + t0 * beamHeightF;
            float y1 = data.fy + t1 * beamHeightF;

            for (int i = 0; i < BEAM_SIDES; i++) {
                float cos0 = BEAM_COS[i];
                float sin0 = BEAM_SIN[i];
                float cos1 = BEAM_COS[(i + 1) % BEAM_SIDES];
                float sin1 = BEAM_SIN[(i + 1) % BEAM_SIDES];

                vc.addVertex(pose, data.fx + radius0 * cos0, y0, data.fz + radius0 * sin0).setColor(data.r, data.g, data.b, fade);
                vc.addVertex(pose, data.fx + radius0 * cos1, y0, data.fz + radius0 * sin1).setColor(data.r, data.g, data.b, fade);
                vc.addVertex(pose, data.fx + radius1 * cos1, y1, data.fz + radius1 * sin1).setColor(data.r, data.g, data.b, fadeNext);
                vc.addVertex(pose, data.fx + radius1 * cos0, y1, data.fz + radius1 * sin0).setColor(data.r, data.g, data.b, fadeNext);
            }
        }
    }
}
