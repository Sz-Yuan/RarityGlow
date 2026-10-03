package kitejs.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "rarityglow")
public class RarityGlowConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public boolean glowEnabled = true;
    @ConfigEntry.Gui.Tooltip
    public boolean beamEnabled = true;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 256)
    public int maxRenderDistance = 24;

    @ConfigEntry.Gui.CollapsibleObject
    public RaritySettings common = new RaritySettings(false, false, "255,255,255");

    @ConfigEntry.Gui.CollapsibleObject
    public RaritySettings uncommon = new RaritySettings(true, true, "255,255,85");

    @ConfigEntry.Gui.CollapsibleObject
    public RaritySettings rare = new RaritySettings(true, true, "85,255,255");

    @ConfigEntry.Gui.CollapsibleObject
    public RaritySettings epic = new RaritySettings(true, true, "255,85,255");

    @ConfigEntry.Gui.CollapsibleObject
    public BeamSettings beam = new BeamSettings();

    public static class BeamSettings {
        @ConfigEntry.Gui.Tooltip
        public boolean patternEnabled = true;
        @ConfigEntry.Gui.Tooltip
        public double beamHeight = 1.5;
        @ConfigEntry.Gui.Tooltip
        public double beamOffset = 0.3;
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int beamWidth = 5;
    }

    public static class RaritySettings {
        public boolean enabled = true;
        public boolean beamEnabled = false;
        @ConfigEntry.Gui.Tooltip
        public String rgb = "255,255,255";

        @SuppressWarnings("unused")
        public RaritySettings() {
        }

        public RaritySettings(boolean enabled, boolean beamEnabled, String rgb) {
            this.enabled = enabled;
            this.beamEnabled = beamEnabled;
            this.rgb = rgb;
        }
    }
}