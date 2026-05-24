package kitejs.utils;

import net.minecraft.world.item.Rarity;

import java.util.EnumMap;

public class GlowColorCache {
    private static volatile EnumMap<Rarity, Integer> COLORS = new EnumMap<>(Rarity.class);

    public static int get(Rarity rarity) {
        return COLORS.getOrDefault(rarity, 0);
    }

    public static void updateFromConfig(kitejs.config.RarityGlowConfig config) {
        var map = new EnumMap<Rarity, Integer>(Rarity.class);
        map.put(Rarity.COMMON, parseRgbSafe(config.common.rgb, 0xFFFFFFFF));
        map.put(Rarity.UNCOMMON, parseRgbSafe(config.uncommon.rgb, 0xFFFFFF55));
        map.put(Rarity.RARE, parseRgbSafe(config.rare.rgb, 0xFF55FFFF));
        map.put(Rarity.EPIC, parseRgbSafe(config.epic.rgb, 0xFFFF55FF));
        COLORS = map;
    }

    private static int parseRgbSafe(String rgb, int fallback) {
        try {
            if (rgb == null || rgb.isBlank()) return fallback;
            String[] parts = rgb.split(",");
            if (parts.length != 3) return fallback;
            int r = clamp(Integer.parseInt(parts[0].trim()));
            int g = clamp(Integer.parseInt(parts[1].trim()));
            int b = clamp(Integer.parseInt(parts[2].trim()));
            return (255 << 24) | (r << 16) | (g << 8) | b;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return fallback;
        }
    }

    private static int clamp(int value) {
        return Math.clamp(value, 0, 255);
    }
}