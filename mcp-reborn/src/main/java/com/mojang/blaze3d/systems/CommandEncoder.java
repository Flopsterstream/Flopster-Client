package com.mojang.blaze3d.systems;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.textures.GpuTexture;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@DontObfuscate
public interface CommandEncoder {
    RenderPass createRenderPass(GpuTexture p_397467_, OptionalInt p_391786_);

    RenderPass createRenderPass(GpuTexture p_392782_, OptionalInt p_396918_, @Nullable GpuTexture p_397075_, OptionalDouble p_393174_);

    void clearColorTexture(GpuTexture p_391899_, int p_397741_);

    void clearColorAndDepthTextures(GpuTexture p_395746_, int p_392725_, GpuTexture p_397792_, double p_397966_);

    void clearDepthTexture(GpuTexture p_391685_, double p_396385_);

    void writeToBuffer(GpuBuffer p_393085_, ByteBuffer p_393079_, int p_397124_);

    GpuBuffer.ReadView readBuffer(GpuBuffer p_393531_);

    GpuBuffer.ReadView readBuffer(GpuBuffer p_394774_, int p_398022_, int p_397858_);

    void writeToTexture(GpuTexture p_391309_, NativeImage p_391647_);

    void writeToTexture(
        GpuTexture p_396696_, NativeImage p_397032_, int p_397972_, int p_396110_, int p_393128_, int p_395682_, int p_393388_, int p_392125_, int p_396435_
    );

    void writeToTexture(
        GpuTexture p_391668_, IntBuffer p_391841_, NativeImage.Format p_393229_, int p_394216_, int p_392784_, int p_392071_, int p_396859_, int p_395354_
    );

    void copyTextureToBuffer(GpuTexture p_396423_, GpuBuffer p_392049_, int p_394917_, Runnable p_397413_, int p_391842_);

    void copyTextureToBuffer(
        GpuTexture p_395042_, GpuBuffer p_394803_, int p_396820_, Runnable p_394723_, int p_395841_, int p_395584_, int p_392863_, int p_391285_, int p_394268_
    );

    void copyTextureToTexture(
        GpuTexture p_391458_, GpuTexture p_397369_, int p_397601_, int p_394677_, int p_395197_, int p_393477_, int p_392763_, int p_394739_, int p_395091_
    );

    void presentTexture(GpuTexture p_396891_);
}