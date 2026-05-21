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
}