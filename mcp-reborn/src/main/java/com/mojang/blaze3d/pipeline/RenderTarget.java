package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RenderTarget {
    private static int UNNAMED_RENDER_TARGETS = 0;
    public int width;
    public int height;
    public int viewWidth;
    public int viewHeight;
    protected final String label;
    public final boolean useDepth;
    @Nullable
    protected GpuTexture colorTexture;
    @Nullable
    protected GpuTexture depthTexture;
    public FilterMode filterMode;

    public RenderTarget(@Nullable String p_392164_, boolean p_166199_) {
        this.label = p_392164_ == null ? "FBO " + UNNAMED_RENDER_TARGETS++ : p_392164_;
        this.useDepth = p_166199_;
    }

    public void resize(int p_83942_, int p_83943_) {
        RenderSystem.assertOnRenderThread();
        this.destroyBuffers();
        this.createBuffers(p_83942_, p_83943_);
    }

    public void destroyBuffers() {
        RenderSystem.assertOnRenderThread();
        if (this.depthTexture != null) {
            this.depthTexture.close();
            this.depthTexture = null;
        }

        if (this.colorTexture != null) {
            this.colorTexture.close();
            this.colorTexture = null;
        }
    }

    public void copyDepthFrom(RenderTarget p_83946_) {
        RenderSystem.assertOnRenderThread();
        if (this.depthTexture == null) {
            throw new IllegalStateException("Trying to copy depth texture to a RenderTarget without a depth texture");
        } else if (p_83946_.depthTexture == null) {
            throw new IllegalStateException("Trying to copy depth texture from a RenderTarget without a depth texture");
        } else {
            RenderSystem.getDevice()
                .createCommandEncoder()
                .copyTextureToTexture(p_83946_.depthTexture, this.depthTexture, 0, 0, 0, 0, 0, this.width, this.height);
        }
    }

    public void createBuffers(int p_83951_, int p_83952_) {
        RenderSystem.assertOnRenderThread();
        int i = RenderSystem.getDevice().getMaxTextureSize();
        if (p_83951_ > 0 && p_83951_ <= i && p_83952_ > 0 && p_83952_ <= i) {
            this.viewWidth = p_83951_;
            this.viewHeight = p_83952_;
            this.width = p_83951_;
            this.height = p_83952_;
            if (this.useDepth) {
                this.depthTexture = RenderSystem.getDevice().createTexture(() -> this.label + " / Depth", TextureFormat.DEPTH32, p_83951_, p_83952_, 1);
                this.depthTexture.setTextureFilter(FilterMode.NEAREST, false);
                this.depthTexture.setAddressMode(AddressMode.CLAMP_TO_EDGE);
            }

            this.colorTexture = RenderSystem.getDevice().createTexture(() -> this.label + " / Color", TextureFormat.RGBA8, p_83951_, p_83952_, 1);
            this.colorTexture.setAddressMode(AddressMode.CLAMP_TO_EDGE);
            this.setFilterMode(FilterMode.NEAREST, true);
        } else {
            throw new IllegalArgumentException("Window " + p_83951_ + "x" + p_83952_ + " size out of bounds (max. size: " + i + ")");
        }
    }

    public void setFilterMode(FilterMode p_397955_) {
        this.setFilterMode(p_397955_, false);
    }

    private void setFilterMode(FilterMode p_397959_, boolean p_333030_) {
        if (this.colorTexture == null) {
            throw new IllegalStateException("Can't change filter mode, color texture doesn't exist yet");
        } else {
            if (p_333030_ || p_397959_ != this.filterMode) {
                this.filterMode = p_397959_;
                this.colorTexture.setTextureFilter(p_397959_, false);
            }
        }
    }

    public void blitToScreen() {
        if (this.colorTexture == null) {
            throw new IllegalStateException("Can't blit to screen, color texture doesn't exist yet");
        } else {
            RenderSystem.getDevice().createCommandEncoder().presentTexture(this.colorTexture);
        }
    }

    public void blitAndBlendToTexture(GpuTexture p_395067_) {
        RenderSystem.assertOnRenderThread();
        RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer gpubuffer = rendersystem$autostorageindexbuffer.getBuffer(6);
        GpuBuffer gpubuffer1 = RenderSystem.getQuadVertexBuffer();

        try (RenderPass renderpass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(p_395067_, OptionalInt.empty())) {
            renderpass.setPipeline(RenderPipelines.ENTITY_OUTLINE_BLIT);
            renderpass.setVertexBuffer(0, gpubuffer1);
            renderpass.setIndexBuffer(gpubuffer, rendersystem$autostorageindexbuffer.type());
            renderpass.bindSampler("InSampler", this.colorTexture);
            renderpass.drawIndexed(0, 6);
        }
    }

    @Nullable
    public GpuTexture getColorTexture() {
        return this.colorTexture;
    }

    @Nullable
    public GpuTexture getDepthTexture() {
        return this.depthTexture;
    }
}