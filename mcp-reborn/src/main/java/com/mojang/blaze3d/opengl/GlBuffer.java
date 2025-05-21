package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.jtracy.MemoryPool;
import com.mojang.jtracy.TracyClient;
import java.nio.ByteBuffer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GlBuffer extends GpuBuffer {
    protected static final MemoryPool MEMORY_POOl = TracyClient.createMemoryPool("GPU Buffers");
    protected boolean closed;
    protected boolean initialized = false;
    @Nullable
    protected final Supplier<String> label;
    protected final int handle;

    protected GlBuffer(GlDebugLabel p_394789_, @Nullable Supplier<String> p_394612_, BufferType p_394608_, BufferUsage p_393237_, int p_395014_, int p_395070_) {
        super(p_394608_, p_393237_, p_395014_);
        this.label = p_394612_;
        this.handle = p_395070_;
        if (p_393237_.isReadable()) {
            GlStateManager._glBindBuffer(GlConst.toGl(p_394608_), p_395070_);
            GlStateManager._glBufferData(GlConst.toGl(p_394608_), p_395014_, GlConst.toGl(p_393237_));
            MEMORY_POOl.malloc(p_395070_, p_395014_);
            this.initialized = true;
            p_394789_.applyLabel(this);
        }
    }

    protected void ensureBufferExists() {
        if (!this.initialized) {
            GlStateManager._glBindBuffer(GlConst.toGl(this.type()), this.handle);
            GlStateManager._glBindBuffer(GlConst.toGl(this.type()), 0);
        }
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            GlStateManager._glDeleteBuffers(this.handle);
            if (this.initialized) {
                MEMORY_POOl.free(this.handle);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ReadView implements GpuBuffer.ReadView {
        private final int target;
        private final ByteBuffer data;

        protected ReadView(int p_391338_, ByteBuffer p_394614_) {
            this.target = p_391338_;
            this.data = p_394614_;
        }

        @Override
        public ByteBuffer data() {
            return this.data;
        }

        @Override
        public void close() {
            GlStateManager._glUnmapBuffer(this.target);
        }
    }
}