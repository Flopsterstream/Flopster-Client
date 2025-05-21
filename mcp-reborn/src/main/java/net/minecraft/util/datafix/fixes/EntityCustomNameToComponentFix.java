package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.Util;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EntityCustomNameToComponentFix extends DataFix {
    public EntityCustomNameToComponentFix(Schema p_15398_) {
        super(p_15398_, true);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.ENTITY);
        Type<?> type1 = this.getOutputSchema().getType(References.ENTITY);
        OpticFinder<String> opticfinder = DSL.fieldFinder("id", NamespacedSchema.namespacedString());
        OpticFinder<String> opticfinder1 = (OpticFinder<String>)type.findField("CustomName");
        Type<?> type2 = type1.findFieldType("CustomName");
        return this.fixTypeEverywhereTyped("EntityCustomNameToComponentFix", type, type1, p_390241_ -> fixEntity(p_390241_, opticfinder, opticfinder1, type2));
    }

    private static <T> Typed<?> fixEntity(Typed<?> p_395890_, OpticFinder<String> p_391688_, OpticFinder<String> p_393616_, Type<T> p_396872_) {
        return p_395890_.update(p_393616_, p_396872_, p_390237_ -> {
            String s = p_395890_.getOptional(p_391688_).orElse("");
            Dynamic<?> dynamic = fixCustomName(p_395890_.getOps(), p_390237_, s);
            return Util.readTypedOrThrow(p_396872_, dynamic).getValue();
        });
    }

    private static <T> Dynamic<T> fixCustomName(DynamicOps<T> p_395370_, String p_393746_, String p_397031_) {
        return "minecraft:commandblock_minecart".equals(p_397031_)
            ? new Dynamic<>(p_395370_, p_395370_.createString(p_393746_))
            : LegacyComponentDataFixUtils.createPlainTextComponent(p_395370_, p_393746_);
    }
}