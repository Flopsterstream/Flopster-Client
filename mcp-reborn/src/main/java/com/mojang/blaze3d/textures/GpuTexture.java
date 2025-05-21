package com.mojang.blaze3d.textures;

import com.mojang.blaze3d.DontObfuscate;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@DontObfuscate
public abstract class GpuTexture implements AutoCloseable {
    private final TextureFormat format;
    private final int width;
    private final int height;
    private final int mipLevels;
    private final String label;
    protected AddressMode addressModeU = AddressMode.REPEAT;
    protected AddressMode addressModeV = AddressMode.REPEAT;
    protected FilterMode minFilter = FilterMode.NEAREST;
    protected FilterMode magFilter = FilterMode.LINEAR;
    protected boolean useMipmaps = true;

    public GpuTexture(String p_395679_, TextureFormat p_392008_, int p_393042_, int p_394574_, int p_397229_) {
        this.label = p_395679_;
        this.format = p_392008_;
        this.width = p_393042_;
        this.height = p_394574_;
        this.mipLevels = p_397229_;
    }

    public int getWidth(int p_397572_) {
        return this.width >> p_397572_;
    }

    public int getHeight(int p_394674_) {
        return this.height >> p_394674_;
    }

    public int getMipLevels() {
        return this.mipLevels;
    }

    public TextureFormat getFormat() {
        return this.format;
    }

    public void setAddressMode(AddressMode p_391531_) {
        this.setAddressMode(p_391531_, p_391531_);
    }

    public void setAddressMode(AddressMode p_396204_, AddressMode p_392726_) {
        this.addressModeU = p_396204_;
        this.addressModeV = p_392726_;
    }

    public void setTextureFilter(FilterMode p_393733_, boolean p_394281_) {
        this.setTextureFilter(p_393733_, p_393733_, p_394281_);
    }

    public void setTextureFilter(FilterMode p_396700_, FilterMode p_393522_, boolean p_396120_) {
        this.minFilter = p_396700_;
        this.magFilter = p_393522_;
        this.useMipmaps = p_396120_;
    }

    public String getLabel() {
        return this.label;
    }

    @Override
    public abstract void close();

    public abstract boolean isClosed();
}