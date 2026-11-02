package dev.notheartyt.customoutline.mixin;

import dev.notheartyt.customoutline.config.OutlineConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Master on/off switch: hides outlines when disabled. */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true, require = 0)
    private void customoutline$toggle(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!OutlineConfig.get().outlinesEnabled) {
            cir.setReturnValue(false);
        }
    }
}
