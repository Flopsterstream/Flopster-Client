package net.minecraft.world;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

public class ContainerHelper {
    public static final String TAG_ITEMS = "Items";

    public static ItemStack removeItem(List<ItemStack> p_18970_, int p_18971_, int p_18972_) {
        return p_18971_ >= 0 && p_18971_ < p_18970_.size() && !p_18970_.get(p_18971_).isEmpty() && p_18972_ > 0
            ? p_18970_.get(p_18971_).split(p_18972_)
            : ItemStack.EMPTY;
    }

    public static ItemStack takeItem(List<ItemStack> p_18967_, int p_18968_) {
        return p_18968_ >= 0 && p_18968_ < p_18967_.size() ? p_18967_.set(p_18968_, ItemStack.EMPTY) : ItemStack.EMPTY;
    }

    public static CompoundTag saveAllItems(CompoundTag p_18977_, NonNullList<ItemStack> p_18978_, HolderLookup.Provider p_333891_) {
        return saveAllItems(p_18977_, p_18978_, true, p_333891_);
    }

    public static CompoundTag saveAllItems(CompoundTag p_18974_, NonNullList<ItemStack> p_18975_, boolean p_336339_, HolderLookup.Provider p_329730_) {
        ListTag listtag = new ListTag();

        for (int i = 0; i < p_18975_.size(); i++) {
            ItemStack itemstack = p_18975_.get(i);
            if (!itemstack.isEmpty()) {
                CompoundTag compoundtag = new CompoundTag();
                compoundtag.putByte("Slot", (byte)i);
                listtag.add(itemstack.save(p_329730_, compoundtag));
            }
        }

        if (!listtag.isEmpty() || p_336339_) {
            p_18974_.put("Items", listtag);
        }

        return p_18974_;
    }

    public static void loadAllItems(CompoundTag p_18981_, NonNullList<ItemStack> p_18982_, HolderLookup.Provider p_334892_) {
        ListTag listtag = p_18981_.getListOrEmpty("Items");

        for (int i = 0; i < listtag.size(); i++) {
            CompoundTag compoundtag = listtag.getCompoundOrEmpty(i);
            int j = compoundtag.getByteOr("Slot", (byte)0) & 255;
            if (j >= 0 && j < p_18982_.size()) {
                p_18982_.set(j, ItemStack.parse(p_334892_, compoundtag).orElse(ItemStack.EMPTY));
            }
        }
    }

    public static int clearOrCountMatchingItems(Container p_18957_, Predicate<ItemStack> p_18958_, int p_18959_, boolean p_18960_) {
        int i = 0;

        for (int j = 0; j < p_18957_.getContainerSize(); j++) {
            ItemStack itemstack = p_18957_.getItem(j);
            int k = clearOrCountMatchingItems(itemstack, p_18958_, p_18959_ - i, p_18960_);
            if (k > 0 && !p_18960_ && itemstack.isEmpty()) {
                p_18957_.setItem(j, ItemStack.EMPTY);
            }

            i += k;
        }

        return i;
    }

    public static int clearOrCountMatchingItems(ItemStack p_18962_, Predicate<ItemStack> p_18963_, int p_18964_, boolean p_18965_) {
        if (p_18962_.isEmpty() || !p_18963_.test(p_18962_)) {
            return 0;
        } else if (p_18965_) {
            return p_18962_.getCount();
        } else {
            int i = p_18964_ < 0 ? p_18962_.getCount() : Math.min(p_18964_, p_18962_.getCount());
            p_18962_.shrink(i);
            return i;
        }
    }
}