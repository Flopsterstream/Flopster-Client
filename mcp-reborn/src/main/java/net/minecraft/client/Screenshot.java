package net.minecraft.client;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Screenshot {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String SCREENSHOT_DIR = "screenshots";

    public static void grab(File p_92290_, RenderTarget p_92293_, Consumer<Component> p_92294_) {
        grab(p_92290_, null, p_92293_, p_92294_);
    }

    public static void grab(File p_92296_, @Nullable String p_92297_, RenderTarget p_92300_, Consumer<Component> p_92301_) {
        takeScreenshot(
            p_92300_,
            p_389141_ -> {
                File file1 = new File(p_92296_, "screenshots");
                file1.mkdir();
                File file2;
                if (p_92297_ == null) {
                    file2 = getFile(file1);
                } else {
                    file2 = new File(file1, p_92297_);
                }

                Util.ioPool()
                    .execute(
                        () -> {
                            try {
                                NativeImage $$4x = p_389141_;

                                try {
                                    p_389141_.writeToFile(file2);
                                    Component component = Component.literal(file2.getName())
                                        .withStyle(ChatFormatting.UNDERLINE)
                                        .withStyle(p_389149_ -> p_389149_.withClickEvent(new ClickEvent.OpenFile(file2.getAbsoluteFile())));
                                    p_92301_.accept(Component.translatable("screenshot.success", component));
                                } catch (Throwable throwable1) {
                                    if (p_389141_ != null) {
                                        try {
                                            $$4x.close();
                                        } catch (Throwable throwable) {
                                            throwable1.addSuppressed(throwable);
                                        }
                                    }

                                    throw throwable1;
                                }

                                if (p_389141_ != null) {
                                    p_389141_.close();
                                }
                            } catch (Exception exception) {
                                LOGGER.warn("Couldn't save screenshot", (Throwable)exception);
                                p_92301_.accept(Component.translatable("screenshot.failure", exception.getMessage()));
                            }
                        }
                    );
            }
        );
    }

    public static void takeScreenshot(RenderTarget p_92282_, Consumer<NativeImage> p_391783_) {
        int i = p_92282_.width;
        int j = p_92282_.height;
        GpuTexture gputexture = p_92282_.getColorTexture();
        if (gputexture == null) {
            throw new IllegalStateException("Tried to capture screenshot of an incomplete framebuffer");
        } else {
            GpuBuffer gpubuffer = RenderSystem.getDevice()
                .createBuffer(() -> "Screenshot buffer", BufferType.PIXEL_PACK, BufferUsage.STATIC_READ, i * j * gputexture.getFormat().pixelSize());
            CommandEncoder commandencoder = RenderSystem.getDevice().createCommandEncoder();
            RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(gputexture, gpubuffer, 0, () -> {
                try (GpuBuffer.ReadView gpubuffer$readview = commandencoder.readBuffer(gpubuffer)) {
                    NativeImage nativeimage = new NativeImage(i, j, false);

                    for (int k = 0; k < j; k++) {
                        for (int l = 0; l < i; l++) {
                            int i1 = gpubuffer$readview.data().getInt((l + k * i) * gputexture.getFormat().pixelSize());
                            nativeimage.setPixelABGR(l, j - k - 1, i1 | 0xFF000000);
                        }
                    }

                    p_391783_.accept(nativeimage);
                }

                gpubuffer.close();
            }, 0);
        }
    }

    private static File getFile(File p_92288_) {
        String s = Util.getFilenameFormattedDateTime();
        int i = 1;

        while (true) {
            File file1 = new File(p_92288_, s + (i == 1 ? "" : "_" + i) + ".png");
            if (!file1.exists()) {
                return file1;
            }

            i++;
        }
    }
}