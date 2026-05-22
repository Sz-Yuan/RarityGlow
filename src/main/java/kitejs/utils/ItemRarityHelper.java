package kitejs.utils;

import kitejs.RarityGlow;
import net.minecraft.world.item.ItemStack;

public class ItemRarityHelper {

    public static int getGlowColorIfEnabled(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        var config = RarityGlow.CONFIG;
        if (!config.enable) return 0;

        return switch (stack.getRarity()) {
            case COMMON -> config.common.enabled ? GlowColorCache.COMMON_COLOR : 0;
            case UNCOMMON -> config.uncommon.enabled ? GlowColorCache.UNCOMMON_COLOR : 0;
            case RARE -> config.rare.enabled ? GlowColorCache.RARE_COLOR : 0;
            case EPIC -> config.epic.enabled ? GlowColorCache.EPIC_COLOR : 0;
        };
    }

    public static boolean isBeamEnabled(ItemStack stack) {
        if (stack.isEmpty()) return false;
        var config = RarityGlow.CONFIG;
        if (!config.enable) return false;

        return switch (stack.getRarity()) {
            case COMMON -> config.common.beamEnabled;
            case UNCOMMON -> config.uncommon.beamEnabled;
            case RARE -> config.rare.beamEnabled;
            case EPIC -> config.epic.beamEnabled;
        };
    }

    public static int getBeamColor(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        var config = RarityGlow.CONFIG;
        if (!config.enable) return 0;

        return switch (stack.getRarity()) {
            case COMMON -> GlowColorCache.COMMON_COLOR;
            case UNCOMMON -> GlowColorCache.UNCOMMON_COLOR;
            case RARE -> GlowColorCache.RARE_COLOR;
            case EPIC -> GlowColorCache.EPIC_COLOR;
        };
    }
}