package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.GpuOutOfMemoryException;
import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.logging.LogUtils;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class GlDevice implements GpuDevice {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected static boolean USE_GL_ARB_vertex_attrib_binding = true;
    protected static boolean USE_GL_KHR_debug = true;
    protected static boolean USE_GL_EXT_debug_label = true;
    protected static boolean USE_GL_ARB_debug_output = true;
    protected static boolean USE_GL_ARB_direct_state_access = true;
    private final CommandEncoder encoder;
    @Nullable
    private final GlDebug debugLog;
    private final GlDebugLabel debugLabels;
    private final int maxSupportedTextureSize;
    private final DirectStateAccess directStateAccess;
    private final BiFunction<ResourceLocation, ShaderType, String> defaultShaderSource;
    private final Map<RenderPipeline, GlRenderPipeline> pipelineCache = new IdentityHashMap<>();
    private final Map<GlDevice.ShaderCompilationKey, GlShaderModule> shaderCache = new HashMap<>();
    private final VertexArrayCache vertexArrayCache;
    private final Set<String> enabledExtensions = new HashSet<>();

    public GlDevice(long p_391790_, int p_397807_, boolean p_394690_, BiFunction<ResourceLocation, ShaderType, String> p_392078_, boolean p_396865_) {
        GLFW.glfwMakeContextCurrent(p_391790_);
        GLCapabilities glcapabilities = GL.createCapabilities();
        int i = getMaxSupportedTextureSize();
        GLFW.glfwSetWindowSizeLimits(p_391790_, -1, -1, i, i);
        this.debugLog = GlDebug.enableDebugCallback(p_397807_, p_394690_, this.enabledExtensions);
        this.debugLabels = GlDebugLabel.create(glcapabilities, p_396865_, this.enabledExtensions);
        this.vertexArrayCache = VertexArrayCache.create(glcapabilities, this.debugLabels, this.enabledExtensions);
        this.directStateAccess = DirectStateAccess.create(glcapabilities, this.enabledExtensions);
        this.maxSupportedTextureSize = i;
        this.defaultShaderSource = p_392078_;
        this.encoder = new GlCommandEncoder(this);
    }

    public GlDebugLabel debugLabels() {
        return this.debugLabels;
    }

    @Override
    public CommandEncoder createCommandEncoder() {
        return this.encoder;
    }

    @Override
    public GpuTexture createTexture(@Nullable Supplier<String> p_397830_, TextureFormat p_394839_, int p_394481_, int p_391831_, int p_395609_) {
        return this.createTexture(this.debugLabels.exists() && p_397830_ != null ? p_397830_.get() : null, p_394839_, p_394481_, p_391831_, p_395609_);
    }

    @Override
    public GpuTexture createTexture(@Nullable String p_394142_, TextureFormat p_394951_, int p_395535_, int p_393944_, int p_392329_) {
        if (p_392329_ < 1) {
            throw new IllegalArgumentException("mipLevels must be at least 1");
        } else {
            GlStateManager.clearGlErrors();
            int i = GlStateManager._genTexture();
            if (p_394142_ == null) {
                p_394142_ = String.valueOf(i);
            }

            GlStateManager._bindTexture(i);
            GlStateManager._texParameter(3553, 33085, p_392329_ - 1);
            GlStateManager._texParameter(3553, 33082, 0);
            GlStateManager._texParameter(3553, 33083, p_392329_ - 1);
            if (p_394951_.hasDepthAspect()) {
                GlStateManager._texParameter(3553, 34892, 0);
            }

            for (int j = 0; j < p_392329_; j++) {
                GlStateManager._texImage2D(
                    3553,
                    j,
                    GlConst.toGlInternalId(p_394951_),
                    p_395535_ >> j,
                    p_393944_ >> j,
                    0,
                    GlConst.toGlExternalId(p_394951_),
                    GlConst.toGlType(p_394951_),
                    null
                );
            }

            int k = GlStateManager._getError();
            if (k == 1285) {
                throw new GpuOutOfMemoryException("Could not allocate texture of " + p_395535_ + "x" + p_393944_ + " for " + p_394142_);
            } else if (k != 0) {
                throw new IllegalStateException("OpenGL error " + k);
            } else {
                GlTexture gltexture = new GlTexture(p_394142_, p_394951_, p_395535_, p_393944_, p_392329_, i);
                this.debugLabels.applyLabel(gltexture);
                return gltexture;
            }
        }
    }

    @Override
    public GpuBuffer createBuffer(@Nullable Supplier<String> p_398040_, BufferType p_394889_, BufferUsage p_396759_, int p_395846_) {
        if (p_395846_ <= 0) {
            throw new IllegalArgumentException("Buffer size must be greater than zero");
        } else {
            return new GlBuffer(this.debugLabels, p_398040_, p_394889_, p_396759_, p_395846_, GlStateManager._glGenBuffers());
        }
    }

    @Override
    public GpuBuffer createBuffer(@Nullable Supplier<String> p_396390_, BufferType p_391768_, BufferUsage p_396874_, ByteBuffer p_397021_) {
        if (!p_397021_.hasRemaining()) {
            throw new IllegalArgumentException("Buffer source must not be empty");
        } else {
            GlBuffer glbuffer = new GlBuffer(this.debugLabels, p_396390_, p_391768_, p_396874_, p_397021_.remaining(), GlStateManager._glGenBuffers());
            this.encoder.writeToBuffer(glbuffer, p_397021_, 0);
            return glbuffer;
        }
    }

    @Override
    public String getImplementationInformation() {
        return GLFW.glfwGetCurrentContext() == 0L
            ? "NO CONTEXT"
            : GlStateManager._getString(7937) + " GL version " + GlStateManager._getString(7938) + ", " + GlStateManager._getString(7936);
    }

    @Override
    public List<String> getLastDebugMessages() {
        return this.debugLog == null ? Collections.emptyList() : this.debugLog.getLastOpenGlDebugMessages();
    }

    @Override
    public boolean isDebuggingEnabled() {
        return this.debugLog != null;
    }

    @Override
    public String getRenderer() {
        return GlStateManager._getString(7937);
    }

    @Override
    public String getVendor() {
        return GlStateManager._getString(7936);
    }

    @Override
    public String getBackendName() {
        return "OpenGL";
    }

    @Override
    public String getVersion() {
        return GlStateManager._getString(7938);
    }

    private static int getMaxSupportedTextureSize() {
        int i = GlStateManager._getInteger(3379);

        for (int j = Math.max(32768, i); j >= 1024; j >>= 1) {
            GlStateManager._texImage2D(32868, 0, 6408, j, j, 0, 6408, 5121, null);
            int k = GlStateManager._getTexLevelParameter(32868, 0, 4096);
            if (k != 0) {
                return j;
            }
        }

        int l = Math.max(i, 1024);
        LOGGER.info("Failed to determine maximum texture size by probing, trying GL_MAX_TEXTURE_SIZE = {}", l);
        return l;
    }

    @Override
    public int getMaxTextureSize() {
        return this.maxSupportedTextureSize;
    }

    @Override
    public void clearPipelineCache() {
        for (GlRenderPipeline glrenderpipeline : this.pipelineCache.values()) {
            if (glrenderpipeline.program() != GlProgram.INVALID_PROGRAM) {
                glrenderpipeline.program().close();
            }
        }

        this.pipelineCache.clear();

        for (GlShaderModule glshadermodule : this.shaderCache.values()) {
            if (glshadermodule != GlShaderModule.INVALID_SHADER) {
                glshadermodule.close();
            }
        }

        this.shaderCache.clear();
    }

    @Override
    public List<String> getEnabledExtensions() {
        return new ArrayList<>(this.enabledExtensions);
    }

    @Override
    public void close() {
        this.clearPipelineCache();
    }

    public DirectStateAccess directStateAccess() {
        return this.directStateAccess;
    }

    protected GlRenderPipeline getOrCompilePipeline(RenderPipeline p_395704_) {
        return this.pipelineCache.computeIfAbsent(p_395704_, p_396980_ -> this.compilePipeline(p_395704_, this.defaultShaderSource));
    }

    protected GlShaderModule getOrCompileShader(
        ResourceLocation p_395044_, ShaderType p_393555_, ShaderDefines p_392809_, BiFunction<ResourceLocation, ShaderType, String> p_398003_
    ) {
        GlDevice.ShaderCompilationKey gldevice$shadercompilationkey = new GlDevice.ShaderCompilationKey(p_395044_, p_393555_, p_392809_);
        return this.shaderCache.computeIfAbsent(gldevice$shadercompilationkey, p_395152_ -> this.compileShader(gldevice$shadercompilationkey, p_398003_));
    }

    public GlRenderPipeline precompilePipeline(RenderPipeline p_395575_, @Nullable BiFunction<ResourceLocation, ShaderType, String> p_395925_) {
        BiFunction<ResourceLocation, ShaderType, String> bifunction = p_395925_ == null ? this.defaultShaderSource : p_395925_;
        return this.pipelineCache.computeIfAbsent(p_395575_, p_392371_ -> this.compilePipeline(p_395575_, bifunction));
    }

    private GlShaderModule compileShader(GlDevice.ShaderCompilationKey p_397148_, BiFunction<ResourceLocation, ShaderType, String> p_393230_) {
        String s = p_393230_.apply(p_397148_.id, p_397148_.type);
        if (s == null) {
            LOGGER.error("Couldn't find source for {} shader ({})", p_397148_.type, p_397148_.id);
            return GlShaderModule.INVALID_SHADER;
        } else {
            String s1 = GlslPreprocessor.injectDefines(s, p_397148_.defines);
            int i = GlStateManager.glCreateShader(GlConst.toGl(p_397148_.type));
            GlStateManager.glShaderSource(i, s1);
            GlStateManager.glCompileShader(i);
            if (GlStateManager.glGetShaderi(i, 35713) == 0) {
                String s2 = StringUtils.trim(GlStateManager.glGetShaderInfoLog(i, 32768));
                LOGGER.error("Couldn't compile {} shader ({}): {}", p_397148_.type.getName(), p_397148_.id, s2);
                return GlShaderModule.INVALID_SHADER;
            } else {
                GlShaderModule glshadermodule = new GlShaderModule(i, p_397148_.id, p_397148_.type);
                this.debugLabels.applyLabel(glshadermodule);
                return glshadermodule;
            }
        }
    }

    private GlRenderPipeline compilePipeline(RenderPipeline p_396022_, BiFunction<ResourceLocation, ShaderType, String> p_394041_) {
        GlShaderModule glshadermodule = this.getOrCompileShader(p_396022_.getVertexShader(), ShaderType.VERTEX, p_396022_.getShaderDefines(), p_394041_);
        GlShaderModule glshadermodule1 = this.getOrCompileShader(p_396022_.getFragmentShader(), ShaderType.FRAGMENT, p_396022_.getShaderDefines(), p_394041_);
        if (glshadermodule == GlShaderModule.INVALID_SHADER) {
            LOGGER.error("Couldn't compile pipeline {}: vertex shader {} was invalid", p_396022_.getLocation(), p_396022_.getVertexShader());
            return new GlRenderPipeline(p_396022_, GlProgram.INVALID_PROGRAM);
        } else if (glshadermodule1 == GlShaderModule.INVALID_SHADER) {
            LOGGER.error("Couldn't compile pipeline {}: fragment shader {} was invalid", p_396022_.getLocation(), p_396022_.getFragmentShader());
            return new GlRenderPipeline(p_396022_, GlProgram.INVALID_PROGRAM);
        } else {
            GlProgram glprogram;
            try {
                glprogram = GlProgram.link(glshadermodule, glshadermodule1, p_396022_.getVertexFormat(), p_396022_.getLocation().toString());
            } catch (ShaderManager.CompilationException shadermanager$compilationexception) {
                LOGGER.error("Couldn't compile program for pipeline {}: {}", p_396022_.getLocation(), shadermanager$compilationexception);
                return new GlRenderPipeline(p_396022_, GlProgram.INVALID_PROGRAM);
            }

            glprogram.setupUniforms(p_396022_.getUniforms(), p_396022_.getSamplers());
            this.debugLabels.applyLabel(glprogram);
            return new GlRenderPipeline(p_396022_, glprogram);
        }
    }

    public VertexArrayCache vertexArrayCache() {
        return this.vertexArrayCache;
    }

    @OnlyIn(Dist.CLIENT)
    record ShaderCompilationKey(ResourceLocation id, ShaderType type, ShaderDefines defines) {
        @Override
        public String toString() {
            String s = this.id + " (" + this.type + ")";
            return !this.defines.isEmpty() ? s + " with " + this.defines : s;
        }
    }
}