package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

public class DataPackCommand {
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_PACK = new DynamicCommandExceptionType(
        p_308647_ -> Component.translatableEscape("commands.datapack.unknown", p_308647_)
    );
    private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_ENABLED = new DynamicCommandExceptionType(
        p_308646_ -> Component.translatableEscape("commands.datapack.enable.failed", p_308646_)
    );
    private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_DISABLED = new DynamicCommandExceptionType(
        p_308645_ -> Component.translatableEscape("commands.datapack.disable.failed", p_308645_)
    );
    private static final DynamicCommandExceptionType ERROR_CANNOT_DISABLE_FEATURE = new DynamicCommandExceptionType(
        p_326235_ -> Component.translatableEscape("commands.datapack.disable.failed.feature", p_326235_)
    );
    private static final Dynamic2CommandExceptionType ERROR_PACK_FEATURES_NOT_ENABLED = new Dynamic2CommandExceptionType(
        (p_308643_, p_308644_) -> Component.translatableEscape("commands.datapack.enable.failed.no_flags", p_308643_, p_308644_)
    );
    private static final SuggestionProvider<CommandSourceStack> SELECTED_PACKS = (p_136848_, p_136849_) -> SharedSuggestionProvider.suggest(
        p_136848_.getSource().getServer().getPackRepository().getSelectedIds().stream().map(StringArgumentType::escapeIfRequired), p_136849_
    );
    private static final SuggestionProvider<CommandSourceStack> UNSELECTED_PACKS = (p_248113_, p_248114_) -> {
        PackRepository packrepository = p_248113_.getSource().getServer().getPackRepository();
        Collection<String> collection = packrepository.getSelectedIds();
        FeatureFlagSet featureflagset = p_248113_.getSource().enabledFeatures();
        return SharedSuggestionProvider.suggest(
            packrepository.getAvailablePacks()
                .stream()
                .filter(p_248116_ -> p_248116_.getRequestedFeatures().isSubsetOf(featureflagset))
                .map(Pack::getId)
                .filter(p_250072_ -> !collection.contains(p_250072_))
                .map(StringArgumentType::escapeIfRequired),
            p_248114_
        );
    };

    public static void register(CommandDispatcher<CommandSourceStack> p_136809_) {
        p_136809_.register(
            Commands.literal("datapack")
                .requires(p_136872_ -> p_136872_.hasPermission(2))
                .then(
                    Commands.literal("enable")
                        .then(
                            Commands.argument("name", StringArgumentType.string())
                                .suggests(UNSELECTED_PACKS)
                                .executes(
                                    p_136876_ -> enablePack(
                                        p_136876_.getSource(),
                                        getPack(p_136876_, "name", true),
                                        (p_180059_, p_180060_) -> p_180060_.getDefaultPosition().insert(p_180059_, p_180060_, Pack::selectionConfig, false)
                                    )
                                )
                                .then(
                                    Commands.literal("after")
                                        .then(
                                            Commands.argument("existing", StringArgumentType.string())
                                                .suggests(SELECTED_PACKS)
                                                .executes(
                                                    p_136880_ -> enablePack(
                                                        p_136880_.getSource(),
                                                        getPack(p_136880_, "name", true),
                                                        (p_180056_, p_180057_) -> p_180056_.add(
                                                            p_180056_.indexOf(getPack(p_136880_, "existing", false)) + 1, p_180057_
                                                        )
                                                    )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("before")
                                        .then(
                                            Commands.argument("existing", StringArgumentType.string())
                                                .suggests(SELECTED_PACKS)
                                                .executes(
                                                    p_136878_ -> enablePack(
                                                        p_136878_.getSource(),
                                                        getPack(p_136878_, "name", true),
                                                        (p_180046_, p_180047_) -> p_180046_.add(
                                                            p_180046_.indexOf(getPack(p_136878_, "existing", false)), p_180047_
                                                        )
                                                    )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("last")
                                        .executes(p_136874_ -> enablePack(p_136874_.getSource(), getPack(p_136874_, "name", true), List::add))
                                )
                                .then(
                                    Commands.literal("first")
                                        .executes(
                                            p_136882_ -> enablePack(
                                                p_136882_.getSource(),
                                                getPack(p_136882_, "name", true),
                                                (p_180052_, p_180053_) -> p_180052_.add(0, p_180053_)
                                            )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("disable")
                        .then(
                            Commands.argument("name", StringArgumentType.string())
                                .suggests(SELECTED_PACKS)
                                .executes(p_136870_ -> disablePack(p_136870_.getSource(), getPack(p_136870_, "name", false)))
                        )
                )
                .then(
                    Commands.literal("list")
                        .executes(p_136864_ -> listPacks(p_136864_.getSource()))
                        .then(Commands.literal("available").executes(p_136846_ -> listAvailablePacks(p_136846_.getSource())))
                        .then(Commands.literal("enabled").executes(p_136811_ -> listEnabledPacks(p_136811_.getSource())))
                )
        );
    }

    private static int enablePack(CommandSourceStack p_136829_, Pack p_136830_, DataPackCommand.Inserter p_136831_) throws CommandSyntaxException {
        PackRepository packrepository = p_136829_.getServer().getPackRepository();
        List<Pack> list = Lists.newArrayList(packrepository.getSelectedPacks());
        p_136831_.apply(list, p_136830_);
        p_136829_.sendSuccess(() -> Component.translatable("commands.datapack.modify.enable", p_136830_.getChatLink(true)), true);
        ReloadCommand.reloadPacks(list.stream().map(Pack::getId).collect(Collectors.toList()), p_136829_);
        return list.size();
    }

    private static int disablePack(CommandSourceStack p_136826_, Pack p_136827_) {
        PackRepository packrepository = p_136826_.getServer().getPackRepository();
        List<Pack> list = Lists.newArrayList(packrepository.getSelectedPacks());
        list.remove(p_136827_);
        p_136826_.sendSuccess(() -> Component.translatable("commands.datapack.modify.disable", p_136827_.getChatLink(true)), true);
        ReloadCommand.reloadPacks(list.stream().map(Pack::getId).collect(Collectors.toList()), p_136826_);
        return list.size();
    }

    private static int listPacks(CommandSourceStack p_136824_) {
        return listEnabledPacks(p_136824_) + listAvailablePacks(p_136824_);
    }

    private static int listAvailablePacks(CommandSourceStack p_136855_) {
        PackRepository packrepository = p_136855_.getServer().getPackRepository();
        packrepository.reload();
        Collection<Pack> collection = packrepository.getSelectedPacks();
        Collection<Pack> collection1 = packrepository.getAvailablePacks();
        FeatureFlagSet featureflagset = p_136855_.enabledFeatures();
        List<Pack> list = collection1.stream().filter(p_248121_ -> !collection.contains(p_248121_) && p_248121_.getRequestedFeatures().isSubsetOf(featureflagset)).toList();
        if (list.isEmpty()) {
            p_136855_.sendSuccess(() -> Component.translatable("commands.datapack.list.available.none"), false);
        } else {
            p_136855_.sendSuccess(
                () -> Component.translatable(
                    "commands.datapack.list.available.success", list.size(), ComponentUtils.formatList(list, p_136844_ -> p_136844_.getChatLink(false))
                ),
                false
            );
        }

        return list.size();
    }

    private static int listEnabledPacks(CommandSourceStack p_136866_) {
        PackRepository packrepository = p_136866_.getServer().getPackRepository();
        packrepository.reload();
        Collection<? extends Pack> collection = packrepository.getSelectedPacks();
        if (collection.isEmpty()) {
            p_136866_.sendSuccess(() -> Component.translatable("commands.datapack.list.enabled.none"), false);
        } else {
            p_136866_.sendSuccess(
                () -> Component.translatable(
                    "commands.datapack.list.enabled.success", collection.size(), ComponentUtils.formatList(collection, p_136807_ -> p_136807_.getChatLink(true))
                ),
                false
            );
        }

        return collection.size();
    }

    private static Pack getPack(CommandContext<CommandSourceStack> p_136816_, String p_136817_, boolean p_136818_) throws CommandSyntaxException {
        String s = StringArgumentType.getString(p_136816_, p_136817_);
        PackRepository packrepository = p_136816_.getSource().getServer().getPackRepository();
        Pack pack = packrepository.getPack(s);
        if (pack == null) {
            throw ERROR_UNKNOWN_PACK.create(s);
        } else {
            boolean flag = packrepository.getSelectedPacks().contains(pack);
            if (p_136818_ && flag) {
                throw ERROR_PACK_ALREADY_ENABLED.create(s);
            } else if (!p_136818_ && !flag) {
                throw ERROR_PACK_ALREADY_DISABLED.create(s);
            } else {
                FeatureFlagSet featureflagset = p_136816_.getSource().enabledFeatures();
                FeatureFlagSet featureflagset1 = pack.getRequestedFeatures();
                if (!p_136818_ && !featureflagset1.isEmpty() && pack.getPackSource() == PackSource.FEATURE) {
                    throw ERROR_CANNOT_DISABLE_FEATURE.create(s);
                } else if (!featureflagset1.isSubsetOf(featureflagset)) {
                    throw ERROR_PACK_FEATURES_NOT_ENABLED.create(s, FeatureFlags.printMissingFlags(featureflagset, featureflagset1));
                } else {
                    return pack;
                }
            }
        }
    }

    interface Inserter {
        void apply(List<Pack> p_136884_, Pack p_136885_) throws CommandSyntaxException;
    }
}