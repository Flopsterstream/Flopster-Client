package net.minecraft.world.entity.variant;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class VariantUtils {
    public static final String TAG_VARIANT = "variant";

    public static <T> Holder<T> getDefaultOrAny(RegistryAccess p_392079_, ResourceKey<T> p_391441_) {
        Registry<T> registry = p_392079_.lookupOrThrow(p_391441_.registryKey());
        return registry.get(p_391441_).or(registry::getAny).orElseThrow();
    }

    public static <T> Holder<T> getAny(RegistryAccess p_392091_, ResourceKey<? extends Registry<T>> p_396932_) {
        return p_392091_.lookupOrThrow(p_396932_).getAny().orElseThrow();
    }

    public static <T> void writeVariant(CompoundTag p_392284_, Holder<T> p_397545_) {
        p_397545_.unwrapKey().ifPresent(p_392288_ -> p_392284_.store("variant", ResourceLocation.CODEC, p_392288_.location()));
    }

    public static <T> Optional<Holder<T>> readVariant(CompoundTag p_397797_, RegistryAccess p_394287_, ResourceKey<? extends Registry<T>> p_394094_) {
        return p_397797_.read("variant", ResourceLocation.CODEC)
            .map(p_397274_ -> ResourceKey.create(p_394094_, p_397274_))
            .flatMap(p_394287_::get);
    }
}