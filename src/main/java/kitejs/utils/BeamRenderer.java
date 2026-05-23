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
        matrices.translate(-cam.x, -cam.y, -cam.z);

        MultiBufferSource.BufferSource bufferSource = context.bufferSource();
        VertexConsumer vc = bufferSource.getBuffer(RenderTypes.debugQuads());

        var pose = matrices.last().pose();

        for (var entity : world.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity itemEntity)) continue;

            int packedColor = ItemRarityHelper.getBeamColorIfEnabled(itemEntity.getItem());
            if (packedColor == 0) continue;

            // Extract RGB from 0xAARRGGBB
            float r = ((packedColor >> 16) & 0xFF) / 255f;
            float g = ((packedColor >> 8) & 0xFF) / 255f;
            float b = (packedColor & 0xFF) / 255f;

            float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);

            // Interpolated visual position (matches EntityRenderer.extractRenderState)
            double vx = Mth.lerp(partialTicks, itemEntity.xOld, itemEntity.getX());
            double vy = Mth.lerp(partialTicks, itemEntity.yOld, itemEntity.getY());
            double vz = Mth.lerp(partialTicks, itemEntity.zOld, itemEntity.getZ());

            // Vertical bob animation (matches ItemEntityRenderer.submit)
            float ageInTicks = itemEntity.tickCount + partialTicks;
            float bob = Mth.sin(ageInTicks / 10.0F + itemEntity.bobOffs) * 0.1F + 0.1F;

            float fx = (float) vx;
            float fy = (float) (vy + beamOffset + bob);
            float fz = (float) vz;

            // Distance-adjusted line width (screen-space lines appear thicker at distance)
            double distToCam = Math.sqrt(
                    (fx - cam.x) * (fx - cam.x) + (fy - cam.y) * (fy - cam.y) + (fz - cam.z) * (fz - cam.z)
            );
            if (distToCam > config.beam.maxRenderDistance) continue;

            float beamHeightF = (float) beamHeight;
            float baseRadius = config.beam.beamWidth * 0.01f;
            int sides = 8;
            int vSegments = 12;

            for (int j = 0; j < vSegments; j++) {
                float t0 = (float) j / vSegments;
                float t1 = (float) (j + 1) / vSegments;

                // Opacity gradient: 100% at bottom → 5% at top
                float alpha0 = 1.0f - t0 * 0.95f;
                float alpha1 = 1.0f - t1 * 0.95f;

                // Radius taper: 100% at bottom → 5% at top
                float radius0 = baseRadius * (1.0f - t0 * 0.95f);
                float radius1 = baseRadius * (1.0f - t1 * 0.95f);

                float y0 = fy + t0 * beamHeightF;
                float y1 = fy + t1 * beamHeightF;

                for (int i = 0; i < sides; i++) {
                    float angle0 = (float) i / sides * (float) (Math.PI * 2);
                    float angle1 = (float) (i + 1) / sides * (float) (Math.PI * 2);

                    float x00 = fx + radius0 * Mth.cos(angle0);
                    float z00 = fz + radius0 * Mth.sin(angle0);
                    float x01 = fx + radius1 * Mth.cos(angle0);
                    float z01 = fz + radius1 * Mth.sin(angle0);
                    float x10 = fx + radius0 * Mth.cos(angle1);
                    float z10 = fz + radius0 * Mth.sin(angle1);
                    float x11 = fx + radius1 * Mth.cos(angle1);
                    float z11 = fz + radius1 * Mth.sin(angle1);

                    // Quad: bottom-left, bottom-right, top-right, top-left
                    vc.addVertex(pose, x00, y0, z00).setColor(r, g, b, alpha0);
                    vc.addVertex(pose, x10, y0, z10).setColor(r, g, b, alpha0);
                    vc.addVertex(pose, x11, y1, z11).setColor(r, g, b, alpha1);
                    vc.addVertex(pose, x01, y1, z01).setColor(r, g, b, alpha1);
                }
            }
        }

        bufferSource.endBatch();
        matrices.popPose();
    }
}
