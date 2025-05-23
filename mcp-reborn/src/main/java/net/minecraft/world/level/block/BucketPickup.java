package net.minecraft.world.level.block;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public interface BucketPickup {
    ItemStack pickupBlock(@Nullable LivingEntity p_391402_, LevelAccessor p_152719_, BlockPos p_152720_, BlockState p_152721_);

    Optional<SoundEvent> getPickupSound();
}