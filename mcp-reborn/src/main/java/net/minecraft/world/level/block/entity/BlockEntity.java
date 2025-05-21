package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public abstract class BlockEntity {
    private static final Codec<BlockEntityType<?>> TYPE_CODEC = BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final BlockEntityType<?> type;
    @Nullable
    protected Level level;
    protected final BlockPos worldPosition;
    protected boolean remove;
    private BlockState blockState;
    private DataComponentMap components = DataComponentMap.EMPTY;

    public BlockEntity(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
        this.type = p_155228_;
        this.worldPosition = p_155229_.immutable();
        this.validateBlockState(p_155230_);
        this.blockState = p_155230_;
    }

    private void validateBlockState(BlockState p_345558_) {
        if (!this.isValidBlockState(p_345558_)) {
            throw new IllegalStateException("Invalid block entity " + this.getNameForReporting() + " state at " + this.worldPosition + ", got " + p_345558_);
        }
    }

    public boolean isValidBlockState(BlockState p_345570_) {
        return this.type.isValid(p_345570_);
    }

    public static BlockPos getPosFromTag(ChunkPos p_396083_, CompoundTag p_187473_) {
        int i = p_187473_.getIntOr("x", 0);
        int j = p_187473_.getIntOr("y", 0);
        int k = p_187473_.getIntOr("z", 0);
        int l = SectionPos.blockToSectionCoord(i);
        int i1 = SectionPos.blockToSectionCoord(k);
        if (l != p_396083_.x || i1 != p_396083_.z) {
            LOGGER.warn("Block entity {} found in a wrong chunk, expected position from chunk {}", p_187473_, p_396083_);
            i = p_396083_.getBlockX(SectionPos.sectionRelative(i));
            k = p_396083_.getBlockZ(SectionPos.sectionRelative(k));
        }

        return new BlockPos(i, j, k);
    }

    @Nullable
    public Level getLevel() {
        return this.level;
    }

    public void setLevel(Level p_155231_) {
        this.level = p_155231_;
    }

    public boolean hasLevel() {
        return this.level != null;
    }

    protected void loadAdditional(CompoundTag p_331149_, HolderLookup.Provider p_333170_) {
    }

    public final void loadWithComponents(CompoundTag p_331756_, HolderLookup.Provider p_335164_) {
        this.loadAdditional(p_331756_, p_335164_);
        this.components = p_331756_.read(BlockEntity.ComponentHelper.COMPONENTS_CODEC, p_335164_.createSerializationContext(NbtOps.INSTANCE)).orElse(DataComponentMap.EMPTY);
    }

    public final void loadCustomOnly(CompoundTag p_333694_, HolderLookup.Provider p_332017_) {
        this.loadAdditional(p_333694_, p_332017_);
    }

    protected void saveAdditional(CompoundTag p_187471_, HolderLookup.Provider p_327783_) {
    }

    public final CompoundTag saveWithFullMetadata(HolderLookup.Provider p_331193_) {
        CompoundTag compoundtag = this.saveWithoutMetadata(p_331193_);
        this.saveMetadata(compoundtag);
        return compoundtag;
    }

    public final CompoundTag saveWithId(HolderLookup.Provider p_332686_) {
        CompoundTag compoundtag = this.saveWithoutMetadata(p_332686_);
        this.saveId(compoundtag);
        return compoundtag;
    }

    public final CompoundTag saveWithoutMetadata(HolderLookup.Provider p_332372_) {
        CompoundTag compoundtag = new CompoundTag();
        this.saveAdditional(compoundtag, p_332372_);
        compoundtag.store(BlockEntity.ComponentHelper.COMPONENTS_CODEC, p_332372_.createSerializationContext(NbtOps.INSTANCE), this.components);
        return compoundtag;
    }

    public final CompoundTag saveCustomOnly(HolderLookup.Provider p_333091_) {
        CompoundTag compoundtag = new CompoundTag();
        this.saveAdditional(compoundtag, p_333091_);
        return compoundtag;
    }

    public final CompoundTag saveCustomAndMetadata(HolderLookup.Provider p_334487_) {
        CompoundTag compoundtag = this.saveCustomOnly(p_334487_);
        this.saveMetadata(compoundtag);
        return compoundtag;
    }

    private void saveId(CompoundTag p_187475_) {
        addEntityType(p_187475_, this.getType());
    }

    public static void addEntityType(CompoundTag p_187469_, BlockEntityType<?> p_187470_) {
        p_187469_.store("id", TYPE_CODEC, p_187470_);
    }

    private void saveMetadata(CompoundTag p_187479_) {
        this.saveId(p_187479_);
        p_187479_.putInt("x", this.worldPosition.getX());
        p_187479_.putInt("y", this.worldPosition.getY());
        p_187479_.putInt("z", this.worldPosition.getZ());
    }

    @Nullable
    public static BlockEntity loadStatic(BlockPos p_155242_, BlockState p_155243_, CompoundTag p_155244_, HolderLookup.Provider p_336084_) {
        BlockEntityType<?> blockentitytype = p_155244_.read("id", TYPE_CODEC).orElse(null);
        if (blockentitytype == null) {
            LOGGER.error("Skipping block entity with invalid type: {}", p_155244_.get("id"));
            return null;
        } else {
            BlockEntity blockentity;
            try {
                blockentity = blockentitytype.create(p_155242_, p_155243_);
            } catch (Throwable throwable1) {
                LOGGER.error("Failed to create block entity {} for block {} at position {} ", blockentitytype, p_155242_, p_155243_, throwable1);
                return null;
            }

            try {
                blockentity.loadWithComponents(p_155244_, p_336084_);
                return blockentity;
            } catch (Throwable throwable) {
                LOGGER.error("Failed to load data for block entity {} for block {} at position {}", blockentitytype, p_155242_, p_155243_, throwable);
                return null;
            }
        }
    }

    public void setChanged() {
        if (this.level != null) {
            setChanged(this.level, this.worldPosition, this.blockState);
        }
    }

    protected static void setChanged(Level p_155233_, BlockPos p_155234_, BlockState p_155235_) {
        p_155233_.blockEntityChanged(p_155234_);
        if (!p_155235_.isAir()) {
            p_155233_.updateNeighbourForOutputSignal(p_155234_, p_155235_.getBlock());
        }
    }

    public BlockPos getBlockPos() {
        return this.worldPosition;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return null;
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider p_329179_) {
        return new CompoundTag();
    }

    public boolean isRemoved() {
        return this.remove;
    }

    public void setRemoved() {
        this.remove = true;
    }

    public void clearRemoved() {
        this.remove = false;
    }

    public void preRemoveSideEffects(BlockPos p_397404_, BlockState p_395805_) {
        if (this instanceof Container container && this.level != null) {
            Containers.dropContents(this.level, p_397404_, container);
        }
    }

    public boolean triggerEvent(int p_58889_, int p_58890_) {
        return false;
    }

    public void fillCrashReportCategory(CrashReportCategory p_58887_) {
        p_58887_.setDetail("Name", this::getNameForReporting);
        p_58887_.setDetail("Cached block", this.getBlockState()::toString);
        if (this.level == null) {
            p_58887_.setDetail("Block location", () -> this.worldPosition + " (world missing)");
        } else {
            p_58887_.setDetail("Actual block", this.level.getBlockState(this.worldPosition)::toString);
            CrashReportCategory.populateBlockLocationDetails(p_58887_, this.level, this.worldPosition);
        }
    }

    private String getNameForReporting() {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(this.getType()) + " // " + this.getClass().getCanonicalName();
    }

    public BlockEntityType<?> getType() {
        return this.type;
    }

    @Deprecated
    public void setBlockState(BlockState p_155251_) {
        this.validateBlockState(p_155251_);
        this.blockState = p_155251_;
    }

    protected void applyImplicitComponents(DataComponentGetter p_391290_) {
    }

    public final void applyComponentsFromItemStack(ItemStack p_328941_) {
        this.applyComponents(p_328941_.getPrototype(), p_328941_.getComponentsPatch());
    }

    public final void applyComponents(DataComponentMap p_335232_, DataComponentPatch p_331646_) {
        final Set<DataComponentType<?>> set = new HashSet<>();
        set.add(DataComponents.BLOCK_ENTITY_DATA);
        set.add(DataComponents.BLOCK_STATE);
        final DataComponentMap datacomponentmap = PatchedDataComponentMap.fromPatch(p_335232_, p_331646_);
        this.applyImplicitComponents(new DataComponentGetter() {
            @Nullable
            @Override
            public <T> T get(DataComponentType<? extends T> p_335233_) {
                set.add(p_335233_);
                return datacomponentmap.get(p_335233_);
            }

            @Override
            public <T> T getOrDefault(DataComponentType<? extends T> p_334887_, T p_333244_) {
                set.add(p_334887_);
                return datacomponentmap.getOrDefault(p_334887_, p_333244_);
            }
        });
        DataComponentPatch datacomponentpatch = p_331646_.forget(set::contains);
        this.components = datacomponentpatch.split().added();
    }

    protected void collectImplicitComponents(DataComponentMap.Builder p_328216_) {
    }

    @Deprecated
    public void removeComponentsFromTag(CompoundTag p_334718_) {
    }

    public final DataComponentMap collectComponents() {
        DataComponentMap.Builder datacomponentmap$builder = DataComponentMap.builder();
        datacomponentmap$builder.addAll(this.components);
        this.collectImplicitComponents(datacomponentmap$builder);
        return datacomponentmap$builder.build();
    }

    public DataComponentMap components() {
        return this.components;
    }

    public void setComponents(DataComponentMap p_335672_) {
        this.components = p_335672_;
    }

    @Nullable
    public static Component parseCustomNameSafe(@Nullable Tag p_393442_, HolderLookup.Provider p_336417_) {
        return p_393442_ == null
            ? null
            : ComponentSerialization.CODEC
                .parse(p_336417_.createSerializationContext(NbtOps.INSTANCE), p_393442_)
                .resultOrPartial(p_327293_ -> LOGGER.warn("Failed to parse custom name, discarding: {}", p_327293_))
                .orElse(null);
    }

    static class ComponentHelper {
        public static final MapCodec<DataComponentMap> COMPONENTS_CODEC = DataComponentMap.CODEC.optionalFieldOf("components", DataComponentMap.EMPTY);

        private ComponentHelper() {
        }
    }
}