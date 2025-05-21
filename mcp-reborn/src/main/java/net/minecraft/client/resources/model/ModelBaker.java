package net.minecraft.client.resources.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ModelBaker {
    ResolvedModel getModel(ResourceLocation p_397309_);

    SpriteGetter sprites();

    <T> T compute(ModelBaker.SharedOperationKey<T> p_395456_);

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface SharedOperationKey<T> {
        T compute(ModelBaker p_393089_);
    }
}