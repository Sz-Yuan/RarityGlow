package kitejs;

import kitejs.config.RarityGlowConfig;
import kitejs.utils.BeamRenderer;
import kitejs.utils.GlowColorCache;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.world.InteractionResult;

public class RarityGlow implements ClientModInitializer {
    public static final String MOD_ID = "rarityglow";
    public static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MOD_ID);
    public static volatile RarityGlowConfig CONFIG;

    @Override
    public void onInitializeClient() {
        LOGGER.info("RarityGlow initializing...");
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(new BeamRenderer());
        AutoConfig.register(RarityGlowConfig.class, Toml4jConfigSerializer::new);
        var holder = AutoConfig.getConfigHolder(RarityGlowConfig.class);
        CONFIG = holder.getConfig();
        GlowColorCache.updateFromConfig(CONFIG);

        holder.registerSaveListener((_, config) -> {
            CONFIG = config;
            GlowColorCache.updateFromConfig(config);
            return InteractionResult.SUCCESS;
        });
    }
}