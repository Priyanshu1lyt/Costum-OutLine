package dev.notheartyt.customoutline;

import dev.notheartyt.customoutline.config.OutlineConfig;
import dev.notheartyt.customoutline.gui.OutlineConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;

public class CustomOutlineClient implements ClientModInitializer {
    public static final String MOD_ID = "customoutline";

    @Override
    public void onInitializeClient() {
        OutlineConfig.load();

        // /customoutline opens the settings screen (works without Mod Menu)
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("customoutline").executes(ctx -> {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    mc.send(() -> mc.setScreen(new OutlineConfigScreen(null)));
                    return 1;
                })));
    }
}
