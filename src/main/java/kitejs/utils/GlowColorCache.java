package kitejs.utils;

public class GlowColorCache {
    public static volatile int COMMON_COLOR;
    public static volatile int UNCOMMON_COLOR;
    public static volatile int RARE_COLOR;
    public static volatile int EPIC_COLOR;

    public static void updateFromConfig(kitejs.config.RarityGlowConfig config) {
        COMMON_COLOR = parseRgbSafe(config.common.rgb, 0xFFFFFFFF);
        UNCOMMON_COLOR = parseRgbSafe(config.uncommon.rgb, 0xFFFFFF55);
        RARE_COLOR = parseRgbSafe(config.rare.rgb, 0xFF55FFFF);
        EPIC_COLOR = parseRgbSafe(config.epic.rgb, 0xFFFF55FF);
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
        } catch (Exception e) {
            return fallback;
        }
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}