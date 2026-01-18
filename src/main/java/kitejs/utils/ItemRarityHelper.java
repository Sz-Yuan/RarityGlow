package kitejs.utils;

import kitejs.RarityGlow;
import net.minecraft.world.item.ItemStack;

public class ItemRarityHelper {

    public static boolean shouldGlow(ItemStack stack) {
        if (stack.isEmpty()) return false;
        var config = RarityGlow.CONFIG;
        if (!config.enable) return false;

        return switch (stack.getRarity()) {
            case COMMON -> config.common.enabled;
            case UNCOMMON -> config.uncommon.enabled;
            case RARE -> config.rare.enabled;
            case EPIC -> config.epic.enabled;
        };
    }

    public static int getGlowColor(ItemStack stack) {
        if (stack.isEmpty()) return 0xFFFFFFFF;
        return switch (stack.getRarity()) {
            case COMMON -> GlowColorCache.COMMON_COLOR;
            case UNCOMMON -> GlowColorCache.UNCOMMON_COLOR;
            case RARE -> GlowColorCache.RARE_COLOR;
            case EPIC -> GlowColorCache.EPIC_COLOR;
        };
    }
}