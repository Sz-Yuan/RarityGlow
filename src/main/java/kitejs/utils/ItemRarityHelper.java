package kitejs.utils;

import kitejs.RarityGlow;
import net.minecraft.world.item.ItemStack;

public class ItemRarityHelper {

    public static int getGlowColorIfEnabled(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        var config = RarityGlow.CONFIG;
        if (!config.enable) return -1;

        return switch (stack.getRarity()) {
            case COMMON -> config.common.enabled ? GlowColorCache.COMMON_COLOR : -1;
            case UNCOMMON -> config.uncommon.enabled ? GlowColorCache.UNCOMMON_COLOR : -1;
            case RARE -> config.rare.enabled ? GlowColorCache.RARE_COLOR : -1;
            case EPIC -> config.epic.enabled ? GlowColorCache.EPIC_COLOR : -1;
        };
    }
}