package kitejs;

import kitejs.config.RarityGlowConfig;
import kitejs.utils.GlowColorCache;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RarityGlow {
    public static final String MOD_ID = "rarityglow";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static volatile RarityGlowConfig CONFIG;

    // Loader-independent setup: registers the Cloth Config holder and fills the color cache.
    // Called from each platform entry point.
    public static void init() {
        LOGGER.info("RarityGlow initializing...");
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
