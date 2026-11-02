package dev.notheartyt.customoutline.mixin;

import dev.notheartyt.customoutline.OutlineColors;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Vanilla outline color comes from getTeamColorValue(); we only swap the returned color. */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "getTeamColorValue", at = @At("RETURN"), cancellable = true)
    private void customoutline$color(CallbackInfoReturnable<Integer> cir) {
        int c = OutlineColors.resolve((Entity) (Object) this, cir.getReturnValue());
        if (c != OutlineColors.NONE) {
            cir.setReturnValue(c);
        }
    }
}
