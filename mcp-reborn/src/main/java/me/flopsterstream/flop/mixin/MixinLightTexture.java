package me.flopsterstream.flop.mixin;

import me.flopsterstream.flop.module.modules.render.Fullbright;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(LightTexture.class)
public class MixinLightTexture {

    private static Field lightmapField;

    static {
        try {
            lightmapField = LightTexture.class.getDeclaredField("lightTextureData"); // <-- change this if name differs
            lightmapField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "updateLightTexture", at = @At("HEAD"), cancellable = true)
    private void onUpdateLightTexture(CallbackInfo ci) {
        if (Fullbright.isEnabledStatic()) {
            try {
                int[] lightmap = (int[]) lightmapField.get(this);
                for (int i = 0; i < lightmap.length; i++) {
                    lightmap[i] = (255 << 24) | (255 << 16) | (255 << 8) | 255;
                }
                ci.cancel();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
