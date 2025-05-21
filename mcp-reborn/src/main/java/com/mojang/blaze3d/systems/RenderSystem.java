package com.mojang.blaze3d.systems;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.TracyFrameCapture;
import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.IntConsumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ArrayListDeque;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
@DontObfuscate
public class RenderSystem {
    public static final ScissorState SCISSOR_STATE = new ScissorState();
    static final Logger LOGGER = LogUtils.getLogger();
    public static final int MINIMUM_ATLAS_TEXTURE_SIZE = 1024;
    @Nullable
    private static Thread renderThread;
    @Nullable
    private static GpuDevice DEVICE;
    private static double lastDrawTime = Double.MIN_VALUE;
    private static final RenderSystem.AutoStorageIndexBuffer sharedSequential = new RenderSystem.AutoStorageIndexBuffer(1, 1, IntConsumer::accept);
    private static final RenderSystem.AutoStorageIndexBuffer sharedSequentialQuad = new RenderSystem.AutoStorageIndexBuffer(4, 6, (p_389087_, p_389088_) -> {
        p_389087_.accept(p_389088_);
        p_389087_.accept(p_389088_ + 1);
        p_389087_.accept(p_389088_ + 2);
        p_389087_.accept(p_389088_ + 2);
        p_389087_.accept(p_389088_ + 3);
        p_389087_.accept(p_389088_);
    });
    private static final RenderSystem.AutoStorageIndexBuffer sharedSequentialLines = new RenderSystem.AutoStorageIndexBuffer(4, 6, (p_389089_, p_389090_) -> {
        p_389089_.accept(p_389090_);
        p_389089_.accept(p_389090_ + 1);
        p_389089_.accept(p_389090_ + 2);
        p_389089_.accept(p_389090_ + 3);
        p_389089_.accept(p_389090_ + 2);
        p_389089_.accept(p_389090_ + 1);
    });
    private static Matrix4f projectionMatrix = new Matrix4f();
    private static Matrix4f savedProjectionMatrix = new Matrix4f();
    private static ProjectionType projectionType = ProjectionType.PERSPECTIVE;
    private static ProjectionType savedProjectionType = ProjectionType.PERSPECTIVE;
    private static final Matrix4fStack modelViewStack = new Matrix4fStack(16);
    private static Matrix4f textureMatrix = new Matrix4f();
    public static final int TEXTURE_COUNT = 12;
    private static final GpuTexture[] shaderTextures = new GpuTexture[12];
    private static final float[] shaderColor = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
    private static float shaderGlintAlpha = 1.0F;
    private static FogParameters shaderFog = FogParameters.NO_FOG;
    private static final Vector3f[] shaderLightDirections = new Vector3f[2];
    private static float shaderGameTime;
    private static final Vector3f modelOffset = new Vector3f();
    private static float shaderLineWidth = 1.0F;
    private static String apiDescription = "Unknown";
    private static final AtomicLong pollEventsWaitStart = new AtomicLong();
    private static final AtomicBoolean pollingEvents = new AtomicBoolean(false);
    @Nullable
    private static GpuBuffer QUAD_VERTEX_BUFFER;
    private static final ArrayListDeque<RenderSystem.GpuAsyncTask> PENDING_FENCES = new ArrayListDeque<>();

    public static void initRenderThread() {
        if (renderThread != null) {
            throw new IllegalStateException("Could not initialize render thread");
        } else {
            renderThread = Thread.currentThread();
        }
    }

    public static boolean isOnRenderThread() {
        return Thread.currentThread() == renderThread;
    }

    public static void assertOnRenderThread() {
        if (!isOnRenderThread()) {
            throw constructThreadException();
        }
    }

    private static IllegalStateException constructThreadException() {
        return new IllegalStateException("Rendersystem called from wrong thread");
    }

    private static void pollEvents() {
        pollEventsWaitStart.set(Util.getMillis());
        pollingEvents.set(true);
        GLFW.glfwPollEvents();
        pollingEvents.set(false);
    }

    public static boolean isFrozenAtPollEvents() {
        return pollingEvents.get() && Util.getMillis() - pollEventsWaitStart.get() > 200L;
    }

    public static void flipFrame(long p_69496_, @Nullable TracyFrameCapture p_365037_) {
        pollEvents();
        Tesselator.getInstance().clear();
        GLFW.glfwSwapBuffers(p_69496_);
        if (p_365037_ != null) {
            p_365037_.endFrame();
        }

        pollEvents();
    }

    public static void limitDisplayFPS(int p_69831_) {
        double d0 = lastDrawTime + 1.0 / p_69831_;

        double d1;
        for (d1 = GLFW.glfwGetTime(); d1 < d0; d1 = GLFW.glfwGetTime()) {
            GLFW.glfwWaitEventsTimeout(d0 - d1);
        }

        lastDrawTime = d1;
    }

    public static void enableScissor(int p_69489_, int p_69490_, int p_69491_, int p_69492_) {
        SCISSOR_STATE.enable(p_69489_, p_69490_, p_69491_, p_69492_);
    }

    public static void disableScissor() {
        SCISSOR_STATE.disable();
    }

    public static void setShaderFog(FogParameters p_366203_) {
        assertOnRenderThread();
        shaderFog = p_366203_;
    }

    public static FogParameters getShaderFog() {
        assertOnRenderThread();
        return shaderFog;
    }

    public static void setShaderGlintAlpha(double p_268332_) {
        setShaderGlintAlpha((float)p_268332_);
    }

    public static void setShaderGlintAlpha(float p_268329_) {
        assertOnRenderThread();
        shaderGlintAlpha = p_268329_;
    }

    public static float getShaderGlintAlpha() {
        assertOnRenderThread();
        return shaderGlintAlpha;
    }

    public static void setShaderLights(Vector3f p_254155_, Vector3f p_254006_) {
        assertOnRenderThread();
        shaderLightDirections[0] = p_254155_;
        shaderLightDirections[1] = p_254006_;
    }

    public static Vector3f[] getShaderLights() {
        return shaderLightDirections;
    }

    public static void setShaderColor(float p_157430_, float p_157431_, float p_157432_, float p_157433_) {
        assertOnRenderThread();
        shaderColor[0] = p_157430_;
        shaderColor[1] = p_157431_;
        shaderColor[2] = p_157432_;
        shaderColor[3] = p_157433_;
    }

    public static float[] getShaderColor() {
        assertOnRenderThread();
        return shaderColor;
    }

    public static void lineWidth(float p_69833_) {
        assertOnRenderThread();
        shaderLineWidth = p_69833_;
    }

    public static float getShaderLineWidth() {
        assertOnRenderThread();
        return shaderLineWidth;
    }

    public static String getBackendDescription() {
        return String.format(Locale.ROOT, "LWJGL version %s", GLX._getLWJGLVersion());
    }

    public static String getApiDescription() {
        return apiDescription;
    }

    public static TimeSource.NanoTimeSource initBackendSystem() {
        return GLX._initGlfw()::getAsLong;
    }

    public static void initRenderer(
        long p_392173_, int p_69581_, boolean p_69582_, BiFunction<ResourceLocation, ShaderType, String> p_393765_, boolean p_394006_
    ) {
        DEVICE = new GlDevice(p_392173_, p_69581_, p_69582_, p_393765_, p_394006_);
        apiDescription = getDevice().getImplementationInformation();

        try (ByteBufferBuilder bytebufferbuilder = new ByteBufferBuilder(DefaultVertexFormat.POSITION.getVertexSize() * 4)) {
            BufferBuilder bufferbuilder = new BufferBuilder(bytebufferbuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            bufferbuilder.addVertex(0.0F, 0.0F, 0.0F);
            bufferbuilder.addVertex(1.0F, 0.0F, 0.0F);
            bufferbuilder.addVertex(1.0F, 1.0F, 0.0F);
            bufferbuilder.addVertex(0.0F, 1.0F, 0.0F);

            try (MeshData meshdata = bufferbuilder.buildOrThrow()) {
                QUAD_VERTEX_BUFFER = getDevice().createBuffer(() -> "Quad", BufferType.VERTICES, BufferUsage.STATIC_WRITE, meshdata.vertexBuffer());
            }
        }
    }

    public static void setErrorCallback(GLFWErrorCallbackI p_69901_) {
        GLX._setGlfwErrorCallback(p_69901_);
    }

    public static void setupDefaultState() {
        projectionMatrix.identity();
        savedProjectionMatrix.identity();
        modelViewStack.clear();
        textureMatrix.identity();
    }

    public static void setupOverlayColor(@Nullable GpuTexture p_394661_) {
        assertOnRenderThread();
        setShaderTexture(1, p_394661_);
    }

    public static void teardownOverlayColor() {
        assertOnRenderThread();
        setShaderTexture(1, null);
    }

    public static void setupLevelDiffuseLighting(Vector3f p_254489_, Vector3f p_254541_) {
        assertOnRenderThread();
        setShaderLights(p_254489_, p_254541_);
    }

    public static void setupGuiFlatDiffuseLighting(Vector3f p_254419_, Vector3f p_254483_) {
        assertOnRenderThread();
        Matrix4f matrix4f = new Matrix4f().rotationY((float) (-Math.PI / 8)).rotateX((float) (Math.PI * 3.0 / 4.0));
        setShaderLights(matrix4f.transformDirection(p_254419_, new Vector3f()), matrix4f.transformDirection(p_254483_, new Vector3f()));
    }

    public static void setupGui3DDiffuseLighting(Vector3f p_253859_, Vector3f p_253890_) {
        assertOnRenderThread();
        Matrix4f matrix4f = new Matrix4f()
            .scaling(1.0F, -1.0F, 1.0F)
            .rotateYXZ(1.0821041F, 3.2375858F, 0.0F)
            .rotateYXZ((float) (-Math.PI / 8), (float) (Math.PI * 3.0 / 4.0), 0.0F);
        setShaderLights(matrix4f.transformDirection(p_253859_, new Vector3f()), matrix4f.transformDirection(p_253890_, new Vector3f()));
    }

    public static void setShaderTexture(int p_157457_, @Nullable GpuTexture p_391574_) {
        assertOnRenderThread();
        if (p_157457_ >= 0 && p_157457_ < shaderTextures.length) {
            shaderTextures[p_157457_] = p_391574_;
        }
    }

    @Nullable
    public static GpuTexture getShaderTexture(int p_157204_) {
        assertOnRenderThread();
        return p_157204_ >= 0 && p_157204_ < shaderTextures.length ? shaderTextures[p_157204_] : null;
    }

    public static void setProjectionMatrix(Matrix4f p_277884_, ProjectionType p_362578_) {
        assertOnRenderThread();
        projectionMatrix = new Matrix4f(p_277884_);
        projectionType = p_362578_;
    }

    public static void setTextureMatrix(Matrix4f p_254081_) {
        assertOnRenderThread();
        textureMatrix = new Matrix4f(p_254081_);
    }

    public static void resetTextureMatrix() {
        assertOnRenderThread();
        textureMatrix.identity();
    }

    public static void backupProjectionMatrix() {
        assertOnRenderThread();
        savedProjectionMatrix = projectionMatrix;
        savedProjectionType = projectionType;
    }

    public static void restoreProjectionMatrix() {
        assertOnRenderThread();
        projectionMatrix = savedProjectionMatrix;
        projectionType = savedProjectionType;
    }

    public static Matrix4f getProjectionMatrix() {
        assertOnRenderThread();
        return projectionMatrix;
    }

    public static Matrix4f getModelViewMatrix() {
        assertOnRenderThread();
        return modelViewStack;
    }

    public static Matrix4fStack getModelViewStack() {
        assertOnRenderThread();
        return modelViewStack;
    }

    public static Matrix4f getTextureMatrix() {
        assertOnRenderThread();
        return textureMatrix;
    }

    public static RenderSystem.AutoStorageIndexBuffer getSequentialBuffer(VertexFormat.Mode p_221942_) {
        assertOnRenderThread();

        return switch (p_221942_) {
            case QUADS -> sharedSequentialQuad;
            case LINES -> sharedSequentialLines;
            default -> sharedSequential;
        };
    }

    public static void setShaderGameTime(long p_157448_, float p_157449_) {
        assertOnRenderThread();
        shaderGameTime = ((float)(p_157448_ % 24000L) + p_157449_) / 24000.0F;
    }

    public static float getShaderGameTime() {
        assertOnRenderThread();
        return shaderGameTime;
    }

    public static ProjectionType getProjectionType() {
        assertOnRenderThread();
        return projectionType;
    }

    public static GpuBuffer getQuadVertexBuffer() {
        if (QUAD_VERTEX_BUFFER == null) {
            throw new IllegalStateException("Can't getQuadVertexBuffer() before renderer was initialized");
        } else {
            return QUAD_VERTEX_BUFFER;
        }
    }

    public static void setModelOffset(float p_395251_, float p_397531_, float p_396894_) {
        assertOnRenderThread();
        modelOffset.set(p_395251_, p_397531_, p_396894_);
    }

    public static void resetModelOffset() {
        assertOnRenderThread();
        modelOffset.set(0.0F, 0.0F, 0.0F);
    }

    public static Vector3f getModelOffset() {
        assertOnRenderThread();
        return modelOffset;
    }

    public static void queueFencedTask(Runnable p_396027_) {
        PENDING_FENCES.addLast(new RenderSystem.GpuAsyncTask(p_396027_, new GpuFence()));
    }

    public static void executePendingTasks() {
        for (RenderSystem.GpuAsyncTask rendersystem$gpuasynctask = PENDING_FENCES.peekFirst();
            rendersystem$gpuasynctask != null;
            rendersystem$gpuasynctask = PENDING_FENCES.peekFirst()
        ) {
            if (!rendersystem$gpuasynctask.fence.awaitCompletion(0L)) {
                return;
            }

            try {
                rendersystem$gpuasynctask.callback.run();
            } finally {
                rendersystem$gpuasynctask.fence.close();
            }

            PENDING_FENCES.removeFirst();
        }
    }

    public static GpuDevice getDevice() {
        if (DEVICE == null) {
            throw new IllegalStateException("Can't getDevice() before it was initialized");
        } else {
            return DEVICE;
        }
    }

    @Nullable
    public static GpuDevice tryGetDevice() {
        return DEVICE;
    }

    @OnlyIn(Dist.CLIENT)
    public static final class AutoStorageIndexBuffer {
        private final int vertexStride;
        private final int indexStride;
        private final RenderSystem.AutoStorageIndexBuffer.IndexGenerator generator;
        @Nullable
        private GpuBuffer buffer;
        private VertexFormat.IndexType type = VertexFormat.IndexType.SHORT;
        private int indexCount;

        AutoStorageIndexBuffer(int p_157472_, int p_157473_, RenderSystem.AutoStorageIndexBuffer.IndexGenerator p_157474_) {
            this.vertexStride = p_157472_;
            this.indexStride = p_157473_;
            this.generator = p_157474_;
        }

        public boolean hasStorage(int p_221945_) {
            return p_221945_ <= this.indexCount;
        }

        public GpuBuffer getBuffer(int p_395093_) {
            this.ensureStorage(p_395093_);
            return this.buffer;
        }

        private void ensureStorage(int p_157477_) {
            if (!this.hasStorage(p_157477_)) {
                p_157477_ = Mth.roundToward(p_157477_ * 2, this.indexStride);
                RenderSystem.LOGGER.debug("Growing IndexBuffer: Old limit {}, new limit {}.", this.indexCount, p_157477_);
                int i = p_157477_ / this.indexStride;
                int j = i * this.vertexStride;
                VertexFormat.IndexType vertexformat$indextype = VertexFormat.IndexType.least(j);
                int k = Mth.roundToward(p_157477_ * vertexformat$indextype.bytes, 4);
                ByteBuffer bytebuffer = MemoryUtil.memAlloc(k);

                try {
                    this.type = vertexformat$indextype;
                    it.unimi.dsi.fastutil.ints.IntConsumer intconsumer = this.intConsumer(bytebuffer);

                    for (int l = 0; l < p_157477_; l += this.indexStride) {
                        this.generator.accept(intconsumer, l * this.vertexStride / this.indexStride);
                    }

                    bytebuffer.flip();
                    if (this.buffer != null) {
                        this.buffer.close();
                    }

                    this.buffer = RenderSystem.getDevice()
                        .createBuffer(() -> "Auto Storage index buffer", BufferType.INDICES, BufferUsage.DYNAMIC_WRITE, bytebuffer);
                } finally {
                    MemoryUtil.memFree(bytebuffer);
                }

                this.indexCount = p_157477_;
            }
        }

        private it.unimi.dsi.fastutil.ints.IntConsumer intConsumer(ByteBuffer p_157479_) {
            switch (this.type) {
                case SHORT:
                    return p_157482_ -> p_157479_.putShort((short)p_157482_);
                case INT:
                default:
                    return p_157479_::putInt;
            }
        }

        public VertexFormat.IndexType type() {
            return this.type;
        }

        @OnlyIn(Dist.CLIENT)
        interface IndexGenerator {
            void accept(it.unimi.dsi.fastutil.ints.IntConsumer p_157488_, int p_157489_);
        }
    }

    @OnlyIn(Dist.CLIENT)
    record GpuAsyncTask(Runnable callback, GpuFence fence) {
    }
}