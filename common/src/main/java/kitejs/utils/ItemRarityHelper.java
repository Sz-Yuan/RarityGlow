package kitejs.utils;

import kitejs.RarityGlow;
import kitejs.config.RarityGlowConfig.RaritySettings;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.function.Function;

public class ItemRarityHelper {

    private static RaritySettings settingsFor(Rarity rarity) {
        var config = RarityGlow.CONFIG;
        return switch (rarity) {
            case COMMON -> config.common;
            case UNCOMMON -> config.uncommon;
            case RARE -> config.rare;
            case EPIC -> config.epic;
        };
    }

    private static int getColorIfEnabled(ItemStack stack, boolean masterSwitch,
                                         Function<RaritySettings, Boolean> rarityToggle) {
        if (stack.isEmpty()) return 0;
        if (!masterSwitch) return 0;
        var rarity = stack.getRarity();
        if (!rarityToggle.apply(settingsFor(rarity))) return 0;
        return GlowColorCache.get(rarity);
    }

    public static int getGlowColorIfEnabled(ItemStack stack) {
        return getColorIfEnabled(stack, RarityGlow.CONFIG.glowEnabled, s -> s.enabled);
    }

    public static int getBeamColorIfEnabled(ItemStack stack) {
        return getColorIfEnabled(stack, RarityGlow.CONFIG.beamEnabled, s -> s.beamEnabled);
    }

    /**
     * Returns true if beam rendering is enabled for at least one rarity tier.
     * Used as a fast-path early exit before iterating entities.
     */
    public static boolean anyBeamEnabled() {
        var config = RarityGlow.CONFIG;
        return config.common.beamEnabled
                || config.uncommon.beamEnabled
                || config.rare.beamEnabled
                || config.epic.beamEnabled;
    }
}
