package com.mojang.blaze3d.systems;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@DontObfuscate
public interface GpuDevice {
    CommandEncoder createCommandEncoder();

    GpuTexture createTexture(@Nullable Supplier<String> p_394357_, TextureFormat p_395807_, int p_395623_, int p_395802_, int p_396157_);

    GpuTexture createTexture(@Nullable String p_391798_, TextureFormat p_393333_, int p_391800_, int p_395600_, int p_394065_);

    GpuBuffer createBuffer(@Nullable Supplier<String> p_395215_, BufferType p_392847_, BufferUsage p_391478_, int p_397054_);

    GpuBuffer createBuffer(@Nullable Supplier<String> p_395857_, BufferType p_392905_, BufferUsage p_393224_, ByteBuffer p_391613_);

    String getImplementationInformation();

    List<String> getLastDebugMessages();

    boolean isDebuggingEnabled();

    String getVendor();

    String getBackendName();

    String getVersion();

    String getRenderer();

    int getMaxTextureSize();

    default CompiledRenderPipeline precompilePipeline(RenderPipeline p_394764_) {
        return this.precompilePipeline(p_394764_, null);
    }

    CompiledRenderPipeline precompilePipeline(RenderPipeline p_391891_, @Nullable BiFunction<ResourceLocation, ShaderType, String> p_392310_);

    void clearPipelineCache();

    List<String> getEnabledExtensions();

    void close();
}