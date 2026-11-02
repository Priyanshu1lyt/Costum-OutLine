package dev.notheartyt.customoutline.config;

public enum OutlineMode {
    STATIC("Static"),
    RAINBOW("Rainbow"),
    RGB_CYCLE("RGB Cycle"),
    BREATHING("Breathing");

    public final String label;

    OutlineMode(String label) {
        this.label = label;
    }

    public OutlineMode next() {
        OutlineMode[] v = values();
        return v[(ordinal() + 1) % v.length];
    }
}
