package net.minecraft.world.phys.shapes;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public interface CollisionContext {
    static CollisionContext empty() {
        return EntityCollisionContext.EMPTY;
    }

    static CollisionContext of(Entity p_82751_) {
        return (CollisionContext)(switch (p_82751_) {
            case AbstractMinecart abstractminecart -> AbstractMinecart.useExperimentalMovement(abstractminecart.level())
                ? new MinecartCollisionContext(abstractminecart, false)
                : new EntityCollisionContext(p_82751_, false, false);
            default -> new EntityCollisionContext(p_82751_, false, false);
        });
    }

    static CollisionContext of(Entity p_366094_, boolean p_366904_) {
        return new EntityCollisionContext(p_366094_, p_366904_, false);
    }

    static CollisionContext placementContext(@Nullable Entity p_397199_) {
        return new EntityCollisionContext(
            p_397199_ != null ? p_397199_.isDescending() : false,
            true,
            p_397199_ != null ? p_397199_.getY() : -Double.MAX_VALUE,
            p_397199_ instanceof LivingEntity livingentity ? livingentity.getMainHandItem() : ItemStack.EMPTY,
            p_397199_ instanceof LivingEntity livingentity1 ? p_394132_ -> ((LivingEntity)p_397199_).canStandOnFluid(p_394132_) : p_397477_ -> false,
            p_397199_
        );
    }

    boolean isDescending();

    boolean isAbove(VoxelShape p_82755_, BlockPos p_82756_, boolean p_82757_);

    boolean isHoldingItem(Item p_82752_);

    boolean canStandOnFluid(FluidState p_205110_, FluidState p_205111_);

    VoxelShape getCollisionShape(BlockState p_363466_, CollisionGetter p_365376_, BlockPos p_362678_);

    default boolean isPlacement() {
        return false;
    }
}
