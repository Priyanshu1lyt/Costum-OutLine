package dev.notheartyt.customoutline.mixin;

import dev.notheartyt.customoutline.OutlineColors;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Block selection outline (the line around the block you look at).
 * drawBlockOutline(..., OutlineRenderState state, int color, float lineWidth): we swap color and width.
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    private static final String DRAW = "drawBlockOutline(Lnet/minecraft/client/util/math/MatrixStack;"
            + "Lnet/minecraft/client/render/VertexConsumer;DDDLnet/minecraft/client/render/state/OutlineRenderState;IF)V";

    @ModifyVariable(method = DRAW, at = @At("HEAD"), argsOnly = true, require = 0)
    private int customoutline$blockColor(int color) {
        return OutlineColors.resolveBlock(color);
    }

    @ModifyVariable(method = DRAW, at = @At("HEAD"), argsOnly = true, require = 0)
    private float customoutline$blockWidth(float width) {
        return OutlineColors.resolveBlockWidth(width);
    }
}
