package net.minecraft.world.level;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public abstract class BaseCommandBlock implements CommandSource {
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final Component DEFAULT_NAME = Component.literal("@");
    private static final int NO_LAST_EXECUTION = -1;
    private long lastExecution = -1L;
    private boolean updateLastExecution = true;
    private int successCount;
    private boolean trackOutput = true;
    @Nullable
    private Component lastOutput;
    private String command = "";
    @Nullable
    private Component customName;

    public int getSuccessCount() {
        return this.successCount;
    }

    public void setSuccessCount(int p_45411_) {
        this.successCount = p_45411_;
    }

    public Component getLastOutput() {
        return this.lastOutput == null ? CommonComponents.EMPTY : this.lastOutput;
    }

    public CompoundTag save(CompoundTag p_45422_, HolderLookup.Provider p_329299_) {
        p_45422_.putString("Command", this.command);
        p_45422_.putInt("SuccessCount", this.successCount);
        RegistryOps<Tag> registryops = p_329299_.createSerializationContext(NbtOps.INSTANCE);
        p_45422_.storeNullable("CustomName", ComponentSerialization.CODEC, registryops, this.customName);
        p_45422_.putBoolean("TrackOutput", this.trackOutput);
        if (this.trackOutput) {
            p_45422_.storeNullable("LastOutput", ComponentSerialization.CODEC, registryops, this.lastOutput);
        }

        p_45422_.putBoolean("UpdateLastExecution", this.updateLastExecution);
        if (this.updateLastExecution && this.lastExecution != -1L) {
            p_45422_.putLong("LastExecution", this.lastExecution);
        }

        return p_45422_;
    }

    public void load(CompoundTag p_45432_, HolderLookup.Provider p_329410_) {
        this.command = p_45432_.getStringOr("Command", "");
        this.successCount = p_45432_.getIntOr("SuccessCount", 0);
        this.setCustomName(BlockEntity.parseCustomNameSafe(p_45432_.get("CustomName"), p_329410_));
        this.trackOutput = p_45432_.getBooleanOr("TrackOutput", true);
        if (this.trackOutput) {
            this.lastOutput = BlockEntity.parseCustomNameSafe(p_45432_.get("LastOutput"), p_329410_);
        } else {
            this.lastOutput = null;
        }

        this.updateLastExecution = p_45432_.getBooleanOr("UpdateLastExecution", true);
        if (this.updateLastExecution) {
            this.lastExecution = p_45432_.getLongOr("LastExecution", -1L);
        } else {
            this.lastExecution = -1L;
        }
    }

    public void setCommand(String p_45420_) {
        this.command = p_45420_;
        this.successCount = 0;
    }

    public String getCommand() {
        return this.command;
    }

    public boolean performCommand(Level p_45415_) {
        if (p_45415_.isClientSide || p_45415_.getGameTime() == this.lastExecution) {
            return false;
        } else if ("Searge".equalsIgnoreCase(this.command)) {
            this.lastOutput = Component.literal("#itzlipofutzli");
            this.successCount = 1;
            return true;
        } else {
            this.successCount = 0;
            MinecraftServer minecraftserver = this.getLevel().getServer();
            if (minecraftserver.isCommandBlockEnabled() && !StringUtil.isNullOrEmpty(this.command)) {
                try {
                    this.lastOutput = null;
                    CommandSourceStack commandsourcestack = this.createCommandSourceStack().withCallback((p_45418_, p_45419_) -> {
                        if (p_45418_) {
                            this.successCount++;
                        }
                    });
                    minecraftserver.getCommands().performPrefixedCommand(commandsourcestack, this.command);
                } catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.forThrowable(throwable, "Executing command block");
                    CrashReportCategory crashreportcategory = crashreport.addCategory("Command to be executed");
                    crashreportcategory.setDetail("Command", this::getCommand);
                    crashreportcategory.setDetail("Name", () -> this.getName().getString());
                    throw new ReportedException(crashreport);
                }
            }

            if (this.updateLastExecution) {
                this.lastExecution = p_45415_.getGameTime();
            } else {
                this.lastExecution = -1L;
            }

            return true;
        }
    }

    public Component getName() {
        return this.customName != null ? this.customName : DEFAULT_NAME;
    }

    @Nullable
    public Component getCustomName() {
        return this.customName;
    }

    public void setCustomName(@Nullable Component p_327944_) {
        this.customName = p_327944_;
    }

    @Override
    public void sendSystemMessage(Component p_220330_) {
        if (this.trackOutput) {
            this.lastOutput = Component.literal("[" + TIME_FORMAT.format(new Date()) + "] ").append(p_220330_);
            this.onUpdated();
        }
    }

    public abstract ServerLevel getLevel();

    public abstract void onUpdated();

    public void setLastOutput(@Nullable Component p_45434_) {
        this.lastOutput = p_45434_;
    }

    public void setTrackOutput(boolean p_45429_) {
        this.trackOutput = p_45429_;
    }

    public boolean isTrackOutput() {
        return this.trackOutput;
    }

    public InteractionResult usedBy(Player p_45413_) {
        if (!p_45413_.canUseGameMasterBlocks()) {
            return InteractionResult.PASS;
        } else {
            if (p_45413_.getCommandSenderWorld().isClientSide) {
                p_45413_.openMinecartCommandBlock(this);
            }

            return InteractionResult.SUCCESS;
        }
    }

    public abstract Vec3 getPosition();

    public abstract CommandSourceStack createCommandSourceStack();

    @Override
    public boolean acceptsSuccess() {
        return this.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK) && this.trackOutput;
    }

    @Override
    public boolean acceptsFailure() {
        return this.trackOutput;
    }

    @Override
    public boolean shouldInformAdmins() {
        return this.getLevel().getGameRules().getBoolean(GameRules.RULE_COMMANDBLOCKOUTPUT);
    }

    public abstract boolean isValid();
}