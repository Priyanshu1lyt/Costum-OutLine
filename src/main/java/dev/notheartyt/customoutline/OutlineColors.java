package dev.notheartyt.customoutline;

import dev.notheartyt.customoutline.config.OutlineConfig;
import dev.notheartyt.customoutline.config.OutlineProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;

public final class OutlineColors {
    /** Returned when the vanilla color should be kept. */
    public static final int NONE = Integer.MIN_VALUE;

    private OutlineColors() {
    }

    /** Resolve the outline color (0xRRGGBB) for an entity, or NONE to keep vanilla. */
    public static int resolve(Entity entity, int vanillaColor) {
        OutlineConfig cfg = OutlineConfig.get();
        OutlineProfile p = pick(entity, cfg);
        if (p == null) return NONE;
        // Keep real scoreboard team colors if requested (no team = plain white)
        if (cfg.respectTeamColors && (vanillaColor & 0xFFFFFF) != 0xFFFFFF) return NONE;
        return compute(p);
    }

    private static OutlineProfile pick(Entity e, OutlineConfig c) {
        if (!c.custom.isEmpty()) {
            OutlineProfile cp = c.custom.get(Registries.ENTITY_TYPE.getId(e.getType()).toString());
            if (cp != null && cp.enabled) return cp;
        }
        OutlineProfile cat;
        if (e instanceof PlayerEntity) cat = c.players;
        else if (e instanceof MerchantEntity) cat = c.villagers;
        else if (e instanceof Monster) cat = c.mobs;
        else if (e instanceof AnimalEntity) cat = c.animals;
        else cat = null;
        if (cat != null && cat.enabled) return cat;
        return c.defaults.enabled ? c.defaults : null;
    }

    /** Final color of a profile right now, including animation and brightness. */
    public static int compute(OutlineProfile p) {
        double sec = System.currentTimeMillis() / 1000.0;
        double sp = p.speed;
        int rgb;
        switch (p.mode) {
            case RAINBOW -> {
                float hue = (float) ((sec * sp * 0.2) % 1.0);
                rgb = hsv(hue);
            }
            case RGB_CYCLE -> {
                double pos = (sec * sp * 0.5) % 3.0;
                int seg = (int) pos;
                float f = (float) (pos - seg);
                int[] k = {0xFF0000, 0x00FF00, 0x0000FF};
                rgb = lerp(k[seg], k[(seg + 1) % 3], f);
            }
            case BREATHING -> {
                double wave = 0.5 + 0.5 * Math.sin(2 * Math.PI * sec * sp * 0.4);
                rgb = scale(p.color, (float) (0.25 + 0.75 * wave));
            }
            default -> rgb = p.color;
        }
        return scale(rgb, p.brightness) & 0xFFFFFF;
    }

    private static int scale(int rgb, float f) {
        int r = clamp(Math.round(((rgb >> 16) & 255) * f));
        int g = clamp(Math.round(((rgb >> 8) & 255) * f));
        int b = clamp(Math.round((rgb & 255) * f));
        return (r << 16) | (g << 8) | b;
    }

    private static int lerp(int a, int b, float t) {
        int r = Math.round(((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = Math.round(((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = Math.round((a & 255) * (1 - t) + (b & 255) * t);
        return (clamp(r) << 16) | (clamp(g) << 8) | clamp(bl);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    /** HSV (s=1, v=1) to 0xRRGGBB. */
    private static int hsv(float h) {
        float x = h * 6f;
        int i = (int) Math.floor(x) % 6;
        float f = x - (float) Math.floor(x);
        float q = 1f - f;
        float r, g, b;
        switch (i) {
            case 0 -> { r = 1; g = f; b = 0; }
            case 1 -> { r = q; g = 1; b = 0; }
            case 2 -> { r = 0; g = 1; b = f; }
            case 3 -> { r = 0; g = q; b = 1; }
            case 4 -> { r = f; g = 0; b = 1; }
            default -> { r = 1; g = 0; b = q; }
        }
        return (clamp(Math.round(r * 255)) << 16) | (clamp(Math.round(g * 255)) << 8) | clamp(Math.round(b * 255));
    }
}
