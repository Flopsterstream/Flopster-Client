package com.mojang.blaze3d.systems;

import com.mojang.blaze3d.DontObfuscate;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@DontObfuscate
public class ScissorState {
    private boolean enabled;
    private int x;
    private int y;
    private int width;
    private int height;

    public void enable(int p_392490_, int p_395265_, int p_398025_, int p_397710_) {
        this.enabled = true;
        this.x = p_392490_;
        this.y = p_395265_;
        this.width = p_398025_;
        this.height = p_397710_;
    }

    public void disable() {
        this.enabled = false;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void copyFrom(ScissorState p_396090_) {
        this.enabled = p_396090_.enabled;
        this.x = p_396090_.x;
        this.y = p_396090_.y;
        this.width = p_396090_.width;
        this.height = p_396090_.height;
    }
}