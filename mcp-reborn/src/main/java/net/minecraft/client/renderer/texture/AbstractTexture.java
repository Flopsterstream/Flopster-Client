package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import javax.annotation.Nullable;
import net.minecraft.util.TriState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractTexture implements AutoCloseable {
    @Nullable
    protected GpuTexture texture;
    protected boolean defaultBlur;

    public void setClamp(boolean p_377282_) {
        if (this.texture == null) {
            throw new IllegalStateException("Texture does not exist, can't change its clamp before something initializes it");
        } else {
            this.texture.setAddressMode(p_377282_ ? AddressMode.CLAMP_TO_EDGE : AddressMode.REPEAT);
        }
    }

    public void setFilter(TriState p_377375_, boolean p_378683_) {
        this.setFilter(p_377375_.toBoolean(this.defaultBlur), p_378683_);
    }

    public void setFilter(boolean p_117961_, boolean p_117962_) {
        if (this.texture == null) {
            throw new IllegalStateException("Texture does not exist, can't get change its filter before something initializes it");
        } else {
            this.texture.setTextureFilter(p_117961_ ? FilterMode.LINEAR : FilterMode.NEAREST, p_117962_);
        }
    }

    @Override
    public void close() {
        if (this.texture != null) {
            this.texture.close();
            this.texture = null;
        }
    }

    public GpuTexture getTexture() {
        if (this.texture == null) {
            throw new IllegalStateException("Texture does not exist, can't get it before something initializes it");
        } else {
            return this.texture;
        }
    }
}