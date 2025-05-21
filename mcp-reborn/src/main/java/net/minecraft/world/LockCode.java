package net.minecraft.world;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;

public record LockCode(ItemPredicate predicate) {
    public static final LockCode NO_LOCK = new LockCode(ItemPredicate.Builder.item().build());
    public static final Codec<LockCode> CODEC = ItemPredicate.CODEC.xmap(LockCode::new, LockCode::predicate);
    public static final String TAG_LOCK = "lock";

    public boolean unlocksWith(ItemStack p_19108_) {
        return this.predicate.test(p_19108_);
    }

    public void addToTag(CompoundTag p_19110_, HolderLookup.Provider p_367767_) {
        if (this != NO_LOCK) {
            p_19110_.store("lock", CODEC, p_367767_.createSerializationContext(NbtOps.INSTANCE), this);
        }
    }

    public static LockCode fromTag(CompoundTag p_19112_, HolderLookup.Provider p_361968_) {
        return p_19112_.read("lock", CODEC, p_361968_.createSerializationContext(NbtOps.INSTANCE)).orElse(NO_LOCK);
    }
}