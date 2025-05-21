package net.minecraft.client.renderer;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class CloudRenderer extends SimplePreparableReloadListener<Optional<CloudRenderer.TextureData>> implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/clouds.png");
    private static final float CELL_SIZE_IN_BLOCKS = 12.0F;
    private static final float HEIGHT_IN_BLOCKS = 4.0F;
    private static final float BLOCKS_PER_SECOND = 0.6F;
    private static final long EMPTY_CELL = 0L;
    private static final int COLOR_OFFSET = 4;
    private static final int NORTH_OFFSET = 3;
    private static final int EAST_OFFSET = 2;
    private static final int SOUTH_OFFSET = 1;
    private static final int WEST_OFFSET = 0;
    private boolean needsRebuild = true;
    private int prevCellX = Integer.MIN_VALUE;
    private int prevCellZ = Integer.MIN_VALUE;
    private CloudRenderer.RelativeCameraPos prevRelativeCameraPos = CloudRenderer.RelativeCameraPos.INSIDE_CLOUDS;
    @Nullable
    private CloudStatus prevType;
    @Nullable
    private CloudRenderer.TextureData texture;
    @Nullable
    private GpuBuffer vertexBuffer = null;
    private int indexCount = 0;
    private final RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);

    protected Optional<CloudRenderer.TextureData> prepare(ResourceManager p_361257_, ProfilerFiller p_362196_) {
        try {
            Optional optional;
            try (
                InputStream inputstream = p_361257_.open(TEXTURE_LOCATION);
                NativeImage nativeimage = NativeImage.read(inputstream);
            ) {
                int i = nativeimage.getWidth();
                int j = nativeimage.getHeight();
                long[] along = new long[i * j];

                for (int k = 0; k < j; k++) {
                    for (int l = 0; l < i; l++) {
                        int i1 = nativeimage.getPixel(l, k);
                        if (isCellEmpty(i1)) {
                            along[l + k * i] = 0L;
                        } else {
                            boolean flag = isCellEmpty(nativeimage.getPixel(l, Math.floorMod(k - 1, j)));
                            boolean flag1 = isCellEmpty(nativeimage.getPixel(Math.floorMod(l + 1, j), k));
                            boolean flag2 = isCellEmpty(nativeimage.getPixel(l, Math.floorMod(k + 1, j)));
                            boolean flag3 = isCellEmpty(nativeimage.getPixel(Math.floorMod(l - 1, j), k));
                            along[l + k * i] = packCellData(i1, flag, flag1, flag2, flag3);
                        }
                    }
                }

                optional = Optional.of(new CloudRenderer.TextureData(along, i, j));
            }

            return optional;
        } catch (IOException ioexception) {
            LOGGER.error("Failed to load cloud texture", (Throwable)ioexception);
            return Optional.empty();
        }
    }

    protected void apply(Optional<CloudRenderer.TextureData> p_370042_, ResourceManager p_368869_, ProfilerFiller p_367795_) {
        this.texture = p_370042_.orElse(null);
        this.needsRebuild = true;
    }

    private static boolean isCellEmpty(int p_366824_) {
        return ARGB.alpha(p_366824_) < 10;
    }

    private static long packCellData(int p_364599_, boolean p_362267_, boolean p_364671_, boolean p_363926_, boolean p_361986_) {
        return (long)p_364599_ << 4 | (p_362267_ ? 1 : 0) << 3 | (p_364671_ ? 1 : 0) << 2 | (p_363926_ ? 1 : 0) << 1 | (p_361986_ ? 1 : 0) << 0;
    }

    private static int getColor(long p_362131_) {
        return (int)(p_362131_ >> 4 & 4294967295L);
    }

    private static boolean isNorthEmpty(long p_369910_) {
        return (p_369910_ >> 3 & 1L) != 0L;
    }

    private static boolean isEastEmpty(long p_365859_) {
        return (p_365859_ >> 2 & 1L) != 0L;
    }

    private static boolean isSouthEmpty(long p_362752_) {
        return (p_362752_ >> 1 & 1L) != 0L;
    }

    private static boolean isWestEmpty(long p_366272_) {
        return (p_366272_ >> 0 & 1L) != 0L;
    }

    public void render(int p_369834_, CloudStatus p_363277_, float p_367079_, Vec3 p_367264_, float p_364211_) {
        if (this.texture != null) {
            float f = (float)(p_367079_ - p_367264_.y);
            float f1 = f + 4.0F;
            CloudRenderer.RelativeCameraPos cloudrenderer$relativecamerapos;
            if (f1 < 0.0F) {
                cloudrenderer$relativecamerapos = CloudRenderer.RelativeCameraPos.ABOVE_CLOUDS;
            } else if (f > 0.0F) {
                cloudrenderer$relativecamerapos = CloudRenderer.RelativeCameraPos.BELOW_CLOUDS;
            } else {
                cloudrenderer$relativecamerapos = CloudRenderer.RelativeCameraPos.INSIDE_CLOUDS;
            }

            double d0 = p_367264_.x + p_364211_ * 0.030000001F;
            double d1 = p_367264_.z + 3.96F;
            double d2 = this.texture.width * 12.0;
            double d3 = this.texture.height * 12.0;
            d0 -= Mth.floor(d0 / d2) * d2;
            d1 -= Mth.floor(d1 / d3) * d3;
            int i = Mth.floor(d0 / 12.0);
            int j = Mth.floor(d1 / 12.0);
            float f2 = (float)(d0 - i * 12.0F);
            float f3 = (float)(d1 - j * 12.0F);
            boolean flag = p_363277_ == CloudStatus.FANCY;
            RenderPipeline renderpipeline = flag ? RenderPipelines.CLOUDS : RenderPipelines.FLAT_CLOUDS;
            if (this.needsRebuild
                || i != this.prevCellX
                || j != this.prevCellZ
                || cloudrenderer$relativecamerapos != this.prevRelativeCameraPos
                || p_363277_ != this.prevType) {
                this.needsRebuild = false;
                this.prevCellX = i;
                this.prevCellZ = j;
                this.prevRelativeCameraPos = cloudrenderer$relativecamerapos;
                this.prevType = p_363277_;

                try (MeshData meshdata = this.buildMesh(Tesselator.getInstance(), i, j, p_363277_, cloudrenderer$relativecamerapos, renderpipeline)) {
                    if (meshdata == null) {
                        this.indexCount = 0;
                    } else {
                        if (this.vertexBuffer != null && this.vertexBuffer.size >= meshdata.vertexBuffer().remaining()) {
                            CommandEncoder commandencoder = RenderSystem.getDevice().createCommandEncoder();
                            commandencoder.writeToBuffer(this.vertexBuffer, meshdata.vertexBuffer(), 0);
                        } else {
                            if (this.vertexBuffer != null) {
                                this.vertexBuffer.close();
                            }

                            this.vertexBuffer = RenderSystem.getDevice()
                                .createBuffer(() -> "Cloud vertex buffer", BufferType.VERTICES, BufferUsage.DYNAMIC_WRITE, meshdata.vertexBuffer());
                        }

                        this.indexCount = meshdata.drawState().indexCount();
                    }
                }
            }

            if (this.indexCount != 0) {
                RenderSystem.setShaderColor(ARGB.redFloat(p_369834_), ARGB.greenFloat(p_369834_), ARGB.blueFloat(p_369834_), 1.0F);
                if (flag) {
                    this.draw(RenderPipelines.CLOUDS_DEPTH_ONLY, f2, f, f3);
                }

                this.draw(renderpipeline, f2, f, f3);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    private void draw(RenderPipeline p_397720_, float p_391846_, float p_394150_, float p_396931_) {
        RenderSystem.setModelOffset(-p_391846_, p_394150_, -p_396931_);
        RenderTarget rendertarget = Minecraft.getInstance().getMainRenderTarget();
        RenderTarget rendertarget1 = Minecraft.getInstance().levelRenderer.getCloudsTarget();
        GpuTexture gputexture;
        GpuTexture gputexture1;
        if (rendertarget1 != null) {
            gputexture = rendertarget1.getColorTexture();
            gputexture1 = rendertarget1.getDepthTexture();
        } else {
            gputexture = rendertarget.getColorTexture();
            gputexture1 = rendertarget.getDepthTexture();
        }

        GpuBuffer gpubuffer = this.indices.getBuffer(this.indexCount);

        try (RenderPass renderpass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(gputexture, OptionalInt.empty(), gputexture1, OptionalDouble.empty())) {
            renderpass.setPipeline(p_397720_);
            renderpass.setIndexBuffer(gpubuffer, this.indices.type());
            renderpass.setVertexBuffer(0, this.vertexBuffer);
            renderpass.drawIndexed(0, this.indexCount);
        }

        RenderSystem.resetModelOffset();
    }

    @Nullable
    private MeshData buildMesh(
        Tesselator p_369688_, int p_363487_, int p_363111_, CloudStatus p_369576_, CloudRenderer.RelativeCameraPos p_366327_, RenderPipeline p_392323_
    ) {
        float f = 0.8F;
        int i = ARGB.colorFromFloat(0.8F, 1.0F, 1.0F, 1.0F);
        int j = ARGB.colorFromFloat(0.8F, 0.9F, 0.9F, 0.9F);
        int k = ARGB.colorFromFloat(0.8F, 0.7F, 0.7F, 0.7F);
        int l = ARGB.colorFromFloat(0.8F, 0.8F, 0.8F, 0.8F);
        BufferBuilder bufferbuilder = p_369688_.begin(p_392323_.getVertexFormatMode(), p_392323_.getVertexFormat());
        this.buildMesh(p_366327_, bufferbuilder, p_363487_, p_363111_, k, i, j, l, p_369576_ == CloudStatus.FANCY);
        return bufferbuilder.build();
    }

    private void buildMesh(
        CloudRenderer.RelativeCameraPos p_369002_,
        BufferBuilder p_368338_,
        int p_362583_,
        int p_363426_,
        int p_366474_,
        int p_363821_,
        int p_368216_,
        int p_370211_,
        boolean p_369773_
    ) {
        if (this.texture != null) {
            int i = 32;
            long[] along = this.texture.cells;
            int j = this.texture.width;
            int k = this.texture.height;

            for (int l = -32; l <= 32; l++) {
                for (int i1 = -32; i1 <= 32; i1++) {
                    int j1 = Math.floorMod(p_362583_ + i1, j);
                    int k1 = Math.floorMod(p_363426_ + l, k);
                    long l1 = along[j1 + k1 * j];
                    if (l1 != 0L) {
                        int i2 = getColor(l1);
                        if (p_369773_) {
                            this.buildExtrudedCell(
                                p_369002_,
                                p_368338_,
                                ARGB.multiply(p_366474_, i2),
                                ARGB.multiply(p_363821_, i2),
                                ARGB.multiply(p_368216_, i2),
                                ARGB.multiply(p_370211_, i2),
                                i1,
                                l,
                                l1
                            );
                        } else {
                            this.buildFlatCell(p_368338_, ARGB.multiply(p_363821_, i2), i1, l);
                        }
                    }
                }
            }
        }
    }

    private void buildFlatCell(BufferBuilder p_362581_, int p_362314_, int p_368834_, int p_364116_) {
        float f = p_368834_ * 12.0F;
        float f1 = f + 12.0F;
        float f2 = p_364116_ * 12.0F;
        float f3 = f2 + 12.0F;
        p_362581_.addVertex(f, 0.0F, f2).setColor(p_362314_);
        p_362581_.addVertex(f, 0.0F, f3).setColor(p_362314_);
        p_362581_.addVertex(f1, 0.0F, f3).setColor(p_362314_);
        p_362581_.addVertex(f1, 0.0F, f2).setColor(p_362314_);
    }

    private void buildExtrudedCell(
        CloudRenderer.RelativeCameraPos p_361197_,
        BufferBuilder p_364242_,
        int p_363655_,
        int p_363819_,
        int p_369270_,
        int p_370048_,
        int p_360917_,
        int p_364085_,
        long p_369137_
    ) {
        float f = p_360917_ * 12.0F;
        float f1 = f + 12.0F;
        float f2 = 0.0F;
        float f3 = 4.0F;
        float f4 = p_364085_ * 12.0F;
        float f5 = f4 + 12.0F;
        if (p_361197_ != CloudRenderer.RelativeCameraPos.BELOW_CLOUDS) {
            p_364242_.addVertex(f, 4.0F, f4).setColor(p_363819_);
            p_364242_.addVertex(f, 4.0F, f5).setColor(p_363819_);
            p_364242_.addVertex(f1, 4.0F, f5).setColor(p_363819_);
            p_364242_.addVertex(f1, 4.0F, f4).setColor(p_363819_);
        }

        if (p_361197_ != CloudRenderer.RelativeCameraPos.ABOVE_CLOUDS) {
            p_364242_.addVertex(f1, 0.0F, f4).setColor(p_363655_);
            p_364242_.addVertex(f1, 0.0F, f5).setColor(p_363655_);
            p_364242_.addVertex(f, 0.0F, f5).setColor(p_363655_);
            p_364242_.addVertex(f, 0.0F, f4).setColor(p_363655_);
        }

        if (isNorthEmpty(p_369137_) && p_364085_ > 0) {
            p_364242_.addVertex(f, 0.0F, f4).setColor(p_370048_);
            p_364242_.addVertex(f, 4.0F, f4).setColor(p_370048_);
            p_364242_.addVertex(f1, 4.0F, f4).setColor(p_370048_);
            p_364242_.addVertex(f1, 0.0F, f4).setColor(p_370048_);
        }

        if (isSouthEmpty(p_369137_) && p_364085_ < 0) {
            p_364242_.addVertex(f1, 0.0F, f5).setColor(p_370048_);
            p_364242_.addVertex(f1, 4.0F, f5).setColor(p_370048_);
            p_364242_.addVertex(f, 4.0F, f5).setColor(p_370048_);
            p_364242_.addVertex(f, 0.0F, f5).setColor(p_370048_);
        }

        if (isWestEmpty(p_369137_) && p_360917_ > 0) {
            p_364242_.addVertex(f, 0.0F, f5).setColor(p_369270_);
            p_364242_.addVertex(f, 4.0F, f5).setColor(p_369270_);
            p_364242_.addVertex(f, 4.0F, f4).setColor(p_369270_);
            p_364242_.addVertex(f, 0.0F, f4).setColor(p_369270_);
        }

        if (isEastEmpty(p_369137_) && p_360917_ < 0) {
            p_364242_.addVertex(f1, 0.0F, f4).setColor(p_369270_);
            p_364242_.addVertex(f1, 4.0F, f4).setColor(p_369270_);
            p_364242_.addVertex(f1, 4.0F, f5).setColor(p_369270_);
            p_364242_.addVertex(f1, 0.0F, f5).setColor(p_369270_);
        }

        boolean flag = Math.abs(p_360917_) <= 1 && Math.abs(p_364085_) <= 1;
        if (flag) {
            p_364242_.addVertex(f1, 4.0F, f4).setColor(p_363819_);
            p_364242_.addVertex(f1, 4.0F, f5).setColor(p_363819_);
            p_364242_.addVertex(f, 4.0F, f5).setColor(p_363819_);
            p_364242_.addVertex(f, 4.0F, f4).setColor(p_363819_);
            p_364242_.addVertex(f, 0.0F, f4).setColor(p_363655_);
            p_364242_.addVertex(f, 0.0F, f5).setColor(p_363655_);
            p_364242_.addVertex(f1, 0.0F, f5).setColor(p_363655_);
            p_364242_.addVertex(f1, 0.0F, f4).setColor(p_363655_);
            p_364242_.addVertex(f1, 0.0F, f4).setColor(p_370048_);
            p_364242_.addVertex(f1, 4.0F, f4).setColor(p_370048_);
            p_364242_.addVertex(f, 4.0F, f4).setColor(p_370048_);
            p_364242_.addVertex(f, 0.0F, f4).setColor(p_370048_);
            p_364242_.addVertex(f, 0.0F, f5).setColor(p_370048_);
            p_364242_.addVertex(f, 4.0F, f5).setColor(p_370048_);
            p_364242_.addVertex(f1, 4.0F, f5).setColor(p_370048_);
            p_364242_.addVertex(f1, 0.0F, f5).setColor(p_370048_);
            p_364242_.addVertex(f, 0.0F, f4).setColor(p_369270_);
            p_364242_.addVertex(f, 4.0F, f4).setColor(p_369270_);
            p_364242_.addVertex(f, 4.0F, f5).setColor(p_369270_);
            p_364242_.addVertex(f, 0.0F, f5).setColor(p_369270_);
            p_364242_.addVertex(f1, 0.0F, f5).setColor(p_369270_);
            p_364242_.addVertex(f1, 4.0F, f5).setColor(p_369270_);
            p_364242_.addVertex(f1, 4.0F, f4).setColor(p_369270_);
            p_364242_.addVertex(f1, 0.0F, f4).setColor(p_369270_);
        }
    }

    public void markForRebuild() {
        this.needsRebuild = true;
    }

    @Override
    public void close() {
        if (this.vertexBuffer != null) {
            this.vertexBuffer.close();
        }
    }

    @OnlyIn(Dist.CLIENT)
    static enum RelativeCameraPos {
        ABOVE_CLOUDS,
        INSIDE_CLOUDS,
        BELOW_CLOUDS;
    }

    @OnlyIn(Dist.CLIENT)
    public record TextureData(long[] cells, int width, int height) {
    }
}