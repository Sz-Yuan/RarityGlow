package kitejs.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "rarityglow")
public class RarityGlowConfig implements ConfigData {
    public boolean enable = true;

    @ConfigEntry.Gui.CollapsibleObject
    public RaritySettings common = new RaritySettings(true, "255,255,255");

    @ConfigEntry.Gui.CollapsibleObject
    public RaritySettings uncommon = new RaritySettings(true, "255,255,85");

    @ConfigEntry.Gui.CollapsibleObject
    public RaritySettings rare = new RaritySettings(true, "85,255,255");

    @ConfigEntry.Gui.CollapsibleObject
    public RaritySettings epic = new RaritySettings(true, "255,85,255");

    public static class RaritySettings {
        public boolean enabled = true;
        @ConfigEntry.Gui.Tooltip
        public String rgb = "255,255,255";

        @SuppressWarnings("unused")
        public RaritySettings() {
        }

        public RaritySettings(boolean enabled, String rgb) {
            this.enabled = enabled;
            this.rgb = rgb;
        }
    }
}