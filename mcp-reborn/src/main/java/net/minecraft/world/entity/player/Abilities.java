package net.minecraft.world.entity.player;

import net.minecraft.nbt.CompoundTag;

public class Abilities {
    private static final boolean DEFAULT_INVULNERABLE = false;
    private static final boolean DEFAULY_FLYING = false;
    private static final boolean DEFAULT_MAY_FLY = false;
    private static final boolean DEFAULT_INSTABUILD = false;
    private static final boolean DEFAULT_MAY_BUILD = true;
    private static final float DEFAULT_FLYING_SPEED = 0.05F;
    private static final float DEFAULT_WALKING_SPEED = 0.1F;
    public boolean invulnerable;
    public boolean flying;
    public boolean mayfly;
    public boolean instabuild;
    public boolean mayBuild = true;
    private float flyingSpeed = 0.05F;
    private float walkingSpeed = 0.1F;

    public void addSaveData(CompoundTag p_35946_) {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putBoolean("invulnerable", this.invulnerable);
        compoundtag.putBoolean("flying", this.flying);
        compoundtag.putBoolean("mayfly", this.mayfly);
        compoundtag.putBoolean("instabuild", this.instabuild);
        compoundtag.putBoolean("mayBuild", this.mayBuild);
        compoundtag.putFloat("flySpeed", this.flyingSpeed);
        compoundtag.putFloat("walkSpeed", this.walkingSpeed);
        p_35946_.put("abilities", compoundtag);
    }

    public void loadSaveData(CompoundTag p_35951_) {
        CompoundTag compoundtag = p_35951_.getCompoundOrEmpty("abilities");
        this.invulnerable = compoundtag.getBooleanOr("invulnerable", false);
        this.flying = compoundtag.getBooleanOr("flying", false);
        this.mayfly = compoundtag.getBooleanOr("mayfly", false);
        this.instabuild = compoundtag.getBooleanOr("instabuild", false);
        this.flyingSpeed = compoundtag.getFloatOr("flySpeed", 0.05F);
        this.walkingSpeed = compoundtag.getFloatOr("walkSpeed", 0.1F);
        this.mayBuild = compoundtag.getBooleanOr("mayBuild", true);
    }

    public float getFlyingSpeed() {
        return this.flyingSpeed;
    }

    public void setFlyingSpeed(float p_35944_) {
        this.flyingSpeed = p_35944_;
    }

    public float getWalkingSpeed() {
        return this.walkingSpeed;
    }

    public void setWalkingSpeed(float p_35949_) {
        this.walkingSpeed = p_35949_;
    }
}