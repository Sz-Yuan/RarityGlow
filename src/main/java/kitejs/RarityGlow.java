package kitejs;

import kitejs.config.RarityGlowConfig;
import kitejs.utils.GlowColorCache;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.world.InteractionResult;

public class RarityGlow implements ClientModInitializer {
    public static final String MOD_ID = "rarityglow";
    public static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MOD_ID);
    public static volatile RarityGlowConfig CONFIG;
    public static volatile boolean ANY_GLOW_ENABLED = true;

    @Override
    public void onInitializeClient() {
        LOGGER.info("KiteJs");
        AutoConfig.register(RarityGlowConfig.class, Toml4jConfigSerializer::new);
        var holder = AutoConfig.getConfigHolder(RarityGlowConfig.class);
        CONFIG = holder.getConfig();
        GlowColorCache.updateFromConfig(CONFIG);
        updateGlobalState(CONFIG);

        holder.registerSaveListener((h, config) -> {
            CONFIG = config;
            updateGlobalState(config);
            GlowColorCache.updateFromConfig(config);
            return InteractionResult.SUCCESS;
        });
    }

    private static void updateGlobalState(RarityGlowConfig config) {
        ANY_GLOW_ENABLED = config.enable && (
                config.common.enabled ||
                        config.uncommon.enabled ||
                        config.rare.enabled ||
                        config.epic.enabled
        );
    }
}