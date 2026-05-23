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
        VertexConsumer vc = bufferSource.getBuffer(RenderTypes.lines());

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

            // Bottom of beam
            vc.addVertex(pose, fx, fy, fz)
                    .setColor(r, g, b, 1f)
                    .setNormal(0f, 1f, 0f)
                    .setLineWidth(config.beam.beamWidth);

            // Top of beam
            vc.addVertex(pose, fx, fy + (float) beamHeight, fz)
                    .setColor(r, g, b, 1f)
                    .setNormal(0f, 1f, 0f)
                    .setLineWidth(config.beam.beamWidth);
        }

        bufferSource.endBatch();
        matrices.popPose();
    }
}
