package dev.notheartyt.customoutline.config;

/** One set of outline settings (used for a category, a single entity type, or the block outline). */
public class OutlineProfile {
    /** If false, this profile is skipped and the next fallback is used. */
    public boolean enabled = false;
    /** RGB color, 0xRRGGBB. */
    public int color = 0x00FFFF;
    public OutlineMode mode = OutlineMode.STATIC;
    /** 0.0 - 2.0 (1.0 = unchanged). */
    public float brightness = 1.0f;
    /** Animation speed multiplier, 0.1 - 5.0. */
    public float speed = 1.0f;
    /** 0.05 - 1.0. Only used by the block outline (entity outlines ignore alpha). */
    public float opacity = 1.0f;

    public OutlineProfile() {
    }

    public OutlineProfile(int color) {
        this.color = color;
    }
}
