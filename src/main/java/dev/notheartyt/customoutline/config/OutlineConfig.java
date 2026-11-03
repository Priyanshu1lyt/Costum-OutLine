package dev.notheartyt.customoutline.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class OutlineConfig {
    private static final Logger LOG = LoggerFactory.getLogger("CustomOutline");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("customoutline.json");
    private static OutlineConfig instance = new OutlineConfig();

    /** Master switch. False hides all glowing entity outlines. */
    public boolean outlinesEnabled = true;
    /** True keeps vanilla team colors (scoreboard teams) untouched. */
    public boolean respectTeamColors = true;

    public OutlineProfile defaults = new OutlineProfile(0x00FFFF);
    public OutlineProfile players = new OutlineProfile(0xFF2E97);
    public OutlineProfile villagers = new OutlineProfile(0x39FF14);
    public OutlineProfile animals = new OutlineProfile(0xFFEE32);
    public OutlineProfile mobs = new OutlineProfile(0xFF3B3B);
    /** Per entity type id, e.g. "minecraft:creeper". */
    public Map<String, OutlineProfile> custom = new LinkedHashMap<>();

    /** The outline drawn around the block you are looking at (vanilla: thin dark line). */
    public OutlineProfile blocks = blockDefaults();
    /** Block outline line width. 0 = keep vanilla width. */
    public float blockThickness = 0f;

    private static OutlineProfile blockDefaults() {
        OutlineProfile p = new OutlineProfile(0xFFD800);
        p.enabled = true;
        return p;
    }

    public static OutlineConfig get() {
        return instance;
    }

    public static void reset() {
        instance = new OutlineConfig();
    }

    public static void load() {
        try {
            if (Files.exists(PATH)) {
                try (Reader r = Files.newBufferedReader(PATH)) {
                    OutlineConfig c = GSON.fromJson(r, OutlineConfig.class);
                    if (c != null) {
                        c.sanitize();
                        instance = c;
                        return;
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Could not read config, using defaults", e);
        }
        instance = new OutlineConfig();
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer w = Files.newBufferedWriter(PATH)) {
                GSON.toJson(instance, w);
            }
        } catch (Exception e) {
            LOG.warn("Could not save config", e);
        }
    }

    private void sanitize() {
        if (defaults == null) defaults = new OutlineProfile(0x00FFFF);
        if (players == null) players = new OutlineProfile(0xFF2E97);
        if (villagers == null) villagers = new OutlineProfile(0x39FF14);
        if (animals == null) animals = new OutlineProfile(0xFFEE32);
        if (mobs == null) mobs = new OutlineProfile(0xFF3B3B);
        if (blocks == null) blocks = blockDefaults();
        if (custom == null) custom = new LinkedHashMap<>();
        fix(defaults);
        fix(players);
        fix(villagers);
        fix(animals);
        fix(mobs);
        fix(blocks);
        blockThickness = Math.max(0f, Math.min(8f, blockThickness));
        custom.values().removeIf(p -> p == null);
        custom.values().forEach(OutlineConfig::fix);
    }

    private static void fix(OutlineProfile p) {
        if (p.mode == null) p.mode = OutlineMode.STATIC;
        p.color &= 0xFFFFFF;
        p.brightness = Math.max(0f, Math.min(2f, p.brightness));
        p.speed = Math.max(0.1f, Math.min(5f, p.speed));
        p.opacity = Math.max(0.05f, Math.min(1f, p.opacity));
    }
}
