package kitejs.fabric;

import kitejs.RarityGlow;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

public class RarityGlowFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RarityGlow.LOGGER.info("Hello RarityGlow (Fabric)!");
        RarityGlow.init();
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(new FabricBeamRenderer());
    }
}
