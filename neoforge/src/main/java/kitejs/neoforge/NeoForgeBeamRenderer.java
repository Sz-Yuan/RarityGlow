package kitejs.neoforge;

import kitejs.utils.BeamRenderer;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

// NeoForge render hook: the equivalent of Fabric's LevelRenderEvents for custom
// geometry submitted through SubmitNodeCollector.
public class NeoForgeBeamRenderer {
    public static void onSubmitCustomGeometry(SubmitCustomGeometryEvent event) {
        BeamRenderer.render(event.getPoseStack(), event.getSubmitNodeCollector());
    }
}
