package kitejs.utils;

import kitejs.RarityGlow;
import kitejs.config.RarityGlowConfig.RaritySettings;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

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

    public static int getGlowColorIfEnabled(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        var config = RarityGlow.CONFIG;
        if (!config.glowEnabled) return 0;

        var settings = settingsFor(stack.getRarity());
        return settings.enabled ? GlowColorCache.get(stack.getRarity()) : 0;
    }

    /**
     * Returns the beam color for this stack's rarity, or 0 if the beam is disabled.
     */
    public static int getBeamColorIfEnabled(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        var config = RarityGlow.CONFIG;
        if (!config.beamEnabled) return 0;

        var rarity = stack.getRarity();
        if (!settingsFor(rarity).beamEnabled) return 0;
        return GlowColorCache.get(rarity);
    }
}
