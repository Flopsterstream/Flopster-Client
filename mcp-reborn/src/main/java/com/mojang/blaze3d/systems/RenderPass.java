package com.mojang.blaze3d.systems;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Collection;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
@DontObfuscate
public interface RenderPass extends AutoCloseable {
    void setPipeline(RenderPipeline p_394712_);

    void bindSampler(String p_395678_, GpuTexture p_396153_);

    void setUniform(String p_397813_, int... p_397417_);

    void setUniform(String p_395198_, float... p_395025_);

    void setUniform(String p_394004_, Matrix4f p_396609_);

    void enableScissor(ScissorState p_392503_);

    void enableScissor(int p_392594_, int p_394512_, int p_391828_, int p_391712_);

    void disableScissor();

    void setVertexBuffer(int p_393394_, GpuBuffer p_395764_);

    void setIndexBuffer(GpuBuffer p_393127_, VertexFormat.IndexType p_397465_);

    void drawIndexed(int p_393708_, int p_396477_);

    void drawMultipleIndexed(Collection<RenderPass.Draw> p_392442_, @Nullable GpuBuffer p_396172_, @Nullable VertexFormat.IndexType p_394399_);

    void draw(int p_397730_, int p_394941_);

    @Override
    void close();

    @OnlyIn(Dist.CLIENT)
    public record Draw(
        int slot,
        GpuBuffer vertexBuffer,
        @Nullable GpuBuffer indexBuffer,
        @Nullable VertexFormat.IndexType indexType,
        int firstIndex,
        int indexCount,
        @Nullable Consumer<RenderPass.UniformUploader> uniformUploaderConsumer
    ) {
        public Draw(int p_394209_, GpuBuffer p_394761_, GpuBuffer p_393439_, VertexFormat.IndexType p_393418_, int p_392985_, int p_394886_) {
            this(p_394209_, p_394761_, p_393439_, p_393418_, p_392985_, p_394886_, null);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface UniformUploader {
        void upload(String p_391168_, float... p_391952_);
    }
}