package kitejs.fabric;

import kitejs.utils.BeamRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

// Fabric render hook: hands Fabric's level render context to the shared beam renderer.
public class FabricBeamRenderer implements LevelRenderEvents.AfterTranslucentFeatures {
    @Override
    public void afterTranslucentFeatures(LevelRenderContext context) {
        BeamRenderer.render(context.poseStack(), context.submitNodeCollector());
    }
}
