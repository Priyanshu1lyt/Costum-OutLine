package dev.notheartyt.customoutline.gui;

import dev.notheartyt.customoutline.OutlineColors;
import dev.notheartyt.customoutline.config.OutlineConfig;
import dev.notheartyt.customoutline.config.OutlineProfile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

public class OutlineConfigScreen extends Screen {
    private static final int NEON = 0xFF00F0FF;
    private static final int NEON2 = 0xFFFF2EE6;
    private static final int TEXT = 0xFFE6EAF2;
    private static final int DIM = 0xFF8A93A6;
    private static final String[] TABS = {"Default", "Players", "Villagers", "Animals", "Mobs", "Custom"};
    private static final int[] PRESETS = {
            0xFF3B3B, 0xFF9F1C, 0xFFEE32, 0x39FF14, 0x00FFFF, 0x3A86FF, 0xB14AFF, 0xFF2E97, 0xFFFFFF
    };

    private static int tab = 0;
    private static String customId = "minecraft:creeper";

    private final Screen parent;
    private final List<int[]> swatches = new ArrayList<>();
    private int px, py, pw, ph, rx, rw;
    private TextFieldWidget hexField;
    private ValueSlider sr, sg, sb;
    private boolean syncing;
    private String status = "";

    public OutlineConfigScreen(Screen parent) {
        super(Text.literal("CustomOutline"));
        this.parent = parent;
    }

    private OutlineProfile editing() {
        OutlineConfig c = OutlineConfig.get();
        switch (tab) {
            case 0: return c.defaults;
            case 1: return c.players;
            case 2: return c.villagers;
            case 3: return c.animals;
            case 4: return c.mobs;
            default:
                Identifier id = Identifier.tryParse(customId);
                return id == null ? null : c.custom.get(id.toString());
        }
    }

    @Override
    protected void init() {
        swatches.clear();
        OutlineConfig cfg = OutlineConfig.get();

        pw = Math.min(width - 16, 440);
        ph = Math.min(height - 16, 262);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        rx = px + 118;
        rw = pw - 128;

        // ---- left column: tabs + master toggles ----
        int lx = px + 10;
        int ty = py + 30;
        for (int i = 0; i < TABS.length; i++) {
            final int idx = i;
            ButtonWidget b = ButtonWidget.builder(Text.literal(TABS[i]), btn -> {
                tab = idx;
                status = "";
                clearAndInit();
            }).dimensions(lx, ty + i * 22, 96, 20).build();
            b.active = (i != tab);
            addDrawableChild(b);
        }
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Outlines: " + (cfg.outlinesEnabled ? "ON" : "OFF")), btn -> {
                    cfg.outlinesEnabled = !cfg.outlinesEnabled;
                    clearAndInit();
                }).dimensions(lx, py + 168, 96, 20).build());
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Teams: " + (cfg.respectTeamColors ? "Keep" : "Override")), btn -> {
                    cfg.respectTeamColors = !cfg.respectTeamColors;
                    clearAndInit();
                }).dimensions(lx, py + 190, 96, 20).build());

        // ---- bottom row ----
        int bw = (pw - 20 - 8) / 3;
        int by = py + ph - 26;
        addDrawableChild(ButtonWidget.builder(Text.literal("Save"), btn -> {
            OutlineConfig.save();
            status = "Saved!";
        }).dimensions(px + 10, by, bw, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset All"), btn -> {
            OutlineConfig.reset();
            status = "Reset to defaults (press Save to keep)";
            clearAndInit();
        }).dimensions(px + 14 + bw, by, bw, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Close"), btn -> {
            OutlineConfig.save();
            client.setScreen(parent);
        }).dimensions(px + 18 + bw * 2, by, bw, 20).build());

        // ---- right column: profile editor ----
        int y = py + 50;

        if (tab == 5) {
            TextFieldWidget idField = new TextFieldWidget(textRenderer, rx, y, rw - 84, 18, Text.literal("Entity ID"));
            idField.setMaxLength(64);
            idField.setText(customId);
            idField.setChangedListener(s -> customId = s.trim().toLowerCase(Locale.ROOT));
            addDrawableChild(idField);
            addDrawableChild(ButtonWidget.builder(Text.literal("Add"), btn -> {
                Identifier id = Identifier.tryParse(customId);
                if (id == null || !Registries.ENTITY_TYPE.containsId(id)) {
                    status = "Unknown entity id";
                    return;
                }
                OutlineProfile np = new OutlineProfile(0x00FFFF);
                np.enabled = true;
                cfg.custom.putIfAbsent(id.toString(), np);
                customId = id.toString();
                status = "Added " + customId;
                clearAndInit();
            }).dimensions(rx + rw - 80, y - 1, 38, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Del"), btn -> {
                Identifier id = Identifier.tryParse(customId);
                if (id != null && cfg.custom.remove(id.toString()) != null) {
                    status = "Removed " + id;
                }
                clearAndInit();
            }).dimensions(rx + rw - 40, y - 1, 38, 20).build());
            y += 24;
        }

        final OutlineProfile p = editing();
        if (p == null) {
            return;
        }

        int half = (rw - 4) / 2;
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Override: " + (p.enabled ? "ON" : "OFF")), btn -> {
                    p.enabled = !p.enabled;
                    clearAndInit();
                }).dimensions(rx, y, half, 20).build());
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Mode: " + p.mode.label), btn -> {
                    p.mode = p.mode.next();
                    clearAndInit();
                }).dimensions(rx + half + 4, y, half, 20).build());
        y += 21;

        sr = new ValueSlider(rx, y, rw, 18, "Red", 0, 255, (p.color >> 16) & 255,
                v -> setChannel(p, 16, v.intValue()), v -> String.valueOf(Math.round(v)));
        addDrawableChild(sr);
        y += 21;
        sg = new ValueSlider(rx, y, rw, 18, "Green", 0, 255, (p.color >> 8) & 255,
                v -> setChannel(p, 8, v.intValue()), v -> String.valueOf(Math.round(v)));
        addDrawableChild(sg);
        y += 21;
        sb = new ValueSlider(rx, y, rw, 18, "Blue", 0, 255, p.color & 255,
                v -> setChannel(p, 0, v.intValue()), v -> String.valueOf(Math.round(v)));
        addDrawableChild(sb);
        y += 21;

        hexField = new TextFieldWidget(textRenderer, rx, y, 62, 18, Text.literal("Hex"));
        hexField.setMaxLength(7);
        hexField.setText(hex(p.color));
        hexField.setChangedListener(s -> {
            if (syncing) return;
            Integer v = parseHex(s);
            if (v != null) {
                p.color = v;
                sr.setCurrent((v >> 16) & 255);
                sg.setCurrent((v >> 8) & 255);
                sb.setCurrent(v & 255);
            }
        });
        addDrawableChild(hexField);

        int sx = rx + 68;
        for (int i = 0; i < PRESETS.length; i++) {
            final int col = PRESETS[i];
            int x = sx + i * 18;
            addDrawableChild(ButtonWidget.builder(Text.empty(), btn -> {
                p.color = col;
                sr.setCurrent((col >> 16) & 255);
                sg.setCurrent((col >> 8) & 255);
                sb.setCurrent(col & 255);
                syncHex(p);
            }).dimensions(x, y - 1, 16, 20).build());
            swatches.add(new int[]{x, y - 1, 16, 20, col});
        }
        y += 21;

        int hw = (rw - 4) / 2;
        addDrawableChild(new ValueSlider(rx, y, hw, 18, "Bright", 0, 2, p.brightness,
                v -> p.brightness = (float) (double) v, v -> Math.round(v * 100) + "%"));
        addDrawableChild(new ValueSlider(rx + hw + 4, y, hw, 18, "Speed", 0.1, 5, p.speed,
                v -> p.speed = (float) (double) v, v -> String.format(Locale.ROOT, "%.1fx", v)));
    }

    private void setChannel(OutlineProfile p, int shift, int value) {
        p.color = (p.color & ~(255 << shift)) | (MathHelper.clamp(value, 0, 255) << shift);
        syncHex(p);
    }

    private void syncHex(OutlineProfile p) {
        if (hexField == null) return;
        syncing = true;
        hexField.setText(hex(p.color));
        syncing = false;
    }

    private static String hex(int rgb) {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }

    private static Integer parseHex(String s) {
        s = s.trim();
        if (s.startsWith("#")) s = s.substring(1);
        if (!s.matches("[0-9a-fA-F]{6}")) return null;
        return Integer.parseInt(s, 16);
    }

    private static void border(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1, y1, x2, y1 + 1, color);
        ctx.fill(x1, y2 - 1, x2, y2, color);
        ctx.fill(x1, y1, x1 + 1, y2, color);
        ctx.fill(x2 - 1, y1, x2, y2, color);
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);
        ctx.fill(px, py, px + pw, py + ph, 0xF00B0E15);
        ctx.fill(px + 1, py + 1, px + pw - 1, py + 22, 0xFF121726);
        border(ctx, px, py, px + pw, py + ph, NEON);
        ctx.drawText(textRenderer, "CustomOutline", px + 10, py + 7, NEON, true);
        String by = "by NotHeartYT";
        ctx.drawText(textRenderer, by, px + pw - 10 - textRenderer.getWidth(by), py + 7, NEON2, true);
        ctx.fill(px + 112, py + 28, px + 113, py + ph - 34, 0xFF23293A);
        // selected tab marker
        ctx.fill(px + 5, py + 30 + tab * 22, px + 8, py + 50 + tab * 22, NEON2);

        // live preview bar
        OutlineProfile p = editing();
        if (p != null) {
            int col = 0xFF000000 | OutlineColors.compute(p);
            ctx.fill(rx, py + 30, rx + rw, py + 44, 0xFF0B0E15);
            border(ctx, rx, py + 30, rx + rw, py + 44, col);
            border(ctx, rx - 1, py + 29, rx + rw + 1, py + 45, (col & 0x00FFFFFF) | 0x66000000);
            String label = "PREVIEW" + (p.enabled ? "" : " (override off)");
            ctx.drawText(textRenderer, label, rx + (rw - textRenderer.getWidth(label)) / 2, py + 34, col, false);
        } else {
            ctx.drawText(textRenderer, "Type an entity id and press Add", rx, py + 34, DIM, false);
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        for (int[] s : swatches) {
            ctx.fill(s[0] + 3, s[1] + 3, s[0] + s[2] - 3, s[1] + s[3] - 3, 0xFF000000 | s[4]);
        }
        if (!status.isEmpty()) {
            ctx.drawText(textRenderer, status, px + 10, py + ph - 38, NEON2, false);
        }
    }

    @Override
    public void close() {
        // Escape = cancel unsaved changes
        OutlineConfig.load();
        client.setScreen(parent);
    }

    /** Simple labelled slider with a custom value range. */
    private static class ValueSlider extends SliderWidget {
        private final String label;
        private final double min, max;
        private final Consumer<Double> setter;
        private final Function<Double, String> fmt;

        ValueSlider(int x, int y, int w, int h, String label, double min, double max, double cur,
                    Consumer<Double> setter, Function<Double, String> fmt) {
            super(x, y, w, h, Text.empty(), (cur - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.setter = setter;
            this.fmt = fmt;
            updateMessage();
        }

        void setCurrent(double v) {
            this.value = MathHelper.clamp((v - min) / (max - min), 0.0, 1.0);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            if (fmt == null) return;
            setMessage(Text.literal(label + ": " + fmt.apply(min + (max - min) * this.value)));
        }

        @Override
        protected void applyValue() {
            setter.accept(min + (max - min) * this.value);
        }
    }
}
