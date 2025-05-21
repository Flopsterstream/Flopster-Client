package com.mojang.blaze3d.buffers;

import com.mojang.blaze3d.DontObfuscate;
import java.nio.ByteBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@DontObfuscate
public abstract class GpuBuffer implements AutoCloseable {
    private final BufferType type;
    private final BufferUsage usage;
    public int size;

    public GpuBuffer(BufferType p_367350_, BufferUsage p_363902_, int p_361832_) {
        this.type = p_367350_;
        this.size = p_361832_;
        this.usage = p_363902_;
    }

    public int size() {
        return this.size;
    }

    public BufferType type() {
        return this.type;
    }

    public BufferUsage usage() {
        return this.usage;
    }

    public abstract boolean isClosed();

    @Override
    public abstract void close();

    @OnlyIn(Dist.CLIENT)
    @DontObfuscate
    public interface ReadView extends AutoCloseable {
        ByteBuffer data();

        @Override
        void close();
    }
}