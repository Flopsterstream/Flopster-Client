package com.mojang.blaze3d.buffers;

import com.mojang.blaze3d.DontObfuscate;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@DontObfuscate
public enum BufferUsage {
    DYNAMIC_WRITE(false, true),
    STATIC_WRITE(false, true),
    STREAM_WRITE(false, true),
    STATIC_READ(true, false),
    DYNAMIC_READ(true, false),
    STREAM_READ(true, false),
    DYNAMIC_COPY(false, false),
    STATIC_COPY(false, false),
    STREAM_COPY(false, false);

    final boolean readable;
    final boolean writable;

    private BufferUsage(final boolean p_364371_, final boolean p_369858_) {
        this.readable = p_364371_;
        this.writable = p_369858_;
    }

    public boolean isReadable() {
        return this.readable;
    }

    public boolean isWritable() {
        return this.writable;
    }
}