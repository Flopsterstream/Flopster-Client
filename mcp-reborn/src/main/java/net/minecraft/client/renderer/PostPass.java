package net.minecraft.client.renderer;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class PostPass {
    private final String name;
    private final RenderPipeline pipeline;
    private final ResourceLocation outputTargetId;
    private final List<PostChainConfig.Uniform> uniforms;
    private final List<PostPass.Input> inputs = new ArrayList<>();

    public PostPass(RenderPipeline p_395322_, ResourceLocation p_369053_, List<PostChainConfig.Uniform> p_361905_) {
        this.pipeline = p_395322_;
        this.name = p_395322_.getLocation().toString();
        this.outputTargetId = p_369053_;
        this.uniforms = p_361905_;
    }

    public void addInput(PostPass.Input p_364040_) {
        this.inputs.add(p_364040_);
    }

    public void addToFrame(
        FrameGraphBuilder p_369714_,
        Map<ResourceLocation, ResourceHandle<RenderTarget>> p_365909_,
        Matrix4f p_363094_,
        @Nullable Consumer<RenderPass> p_392946_
    ) {
        FramePass framepass = p_369714_.addPass(this.name);

        for (PostPass.Input postpass$input : this.inputs) {
            postpass$input.addToPass(framepass, p_365909_);
        }

        ResourceHandle<RenderTarget> resourcehandle = p_365909_.computeIfPresent(
            this.outputTargetId, (p_366255_, p_363433_) -> framepass.readsAndWrites((ResourceHandle<RenderTarget>)p_363433_)
        );
        if (resourcehandle == null) {
            throw new IllegalStateException("Missing handle for target " + this.outputTargetId);
        } else {
            framepass.executes(
                () -> {
                    RenderTarget rendertarget = resourcehandle.get();
                    RenderSystem.backupProjectionMatrix();
                    RenderSystem.setProjectionMatrix(p_363094_, ProjectionType.ORTHOGRAPHIC);
                    GpuBuffer gpubuffer = RenderSystem.getQuadVertexBuffer();
                    RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
                    GpuBuffer gpubuffer1 = rendersystem$autostorageindexbuffer.getBuffer(6);

                    try (RenderPass renderpass = RenderSystem.getDevice()
                            .createCommandEncoder()
                            .createRenderPass(
                                rendertarget.getColorTexture(), OptionalInt.empty(), rendertarget.useDepth ? rendertarget.getDepthTexture() : null, OptionalDouble.empty()
                            )) {
                        renderpass.setPipeline(this.pipeline);
                        renderpass.setUniform("OutSize", (float)rendertarget.width, (float)rendertarget.height);
                        renderpass.setVertexBuffer(0, gpubuffer);
                        renderpass.setIndexBuffer(gpubuffer1, rendersystem$autostorageindexbuffer.type());

                        for (PostPass.Input postpass$input1 : this.inputs) {
                            postpass$input1.bindTo(renderpass, p_365909_);
                        }

                        if (p_392946_ != null) {
                            p_392946_.accept(renderpass);
                        }

                        for (PostChainConfig.Uniform postchainconfig$uniform : this.uniforms) {
                            postchainconfig$uniform.setOnRenderPass(renderpass);
                        }

                        renderpass.drawIndexed(0, 6);
                    }

                    RenderSystem.restoreProjectionMatrix();

                    for (PostPass.Input postpass$input2 : this.inputs) {
                        postpass$input2.cleanup(p_365909_);
                    }
                }
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface Input {
        void addToPass(FramePass p_362856_, Map<ResourceLocation, ResourceHandle<RenderTarget>> p_367378_);

        void bindTo(RenderPass p_392140_, Map<ResourceLocation, ResourceHandle<RenderTarget>> p_366076_);

        default void cleanup(Map<ResourceLocation, ResourceHandle<RenderTarget>> p_366914_) {
        }
    }

    @OnlyIn(Dist.CLIENT)
    public record TargetInput(String samplerName, ResourceLocation targetId, boolean depthBuffer, boolean bilinear) implements PostPass.Input {
        private ResourceHandle<RenderTarget> getHandle(Map<ResourceLocation, ResourceHandle<RenderTarget>> p_369908_) {
            ResourceHandle<RenderTarget> resourcehandle = p_369908_.get(this.targetId);
            if (resourcehandle == null) {
                throw new IllegalStateException("Missing handle for target " + this.targetId);
            } else {
                return resourcehandle;
            }
        }

        @Override
        public void addToPass(FramePass p_369983_, Map<ResourceLocation, ResourceHandle<RenderTarget>> p_369342_) {
            p_369983_.reads(this.getHandle(p_369342_));
        }

        @Override
        public void bindTo(RenderPass p_396479_, Map<ResourceLocation, ResourceHandle<RenderTarget>> p_363476_) {
            ResourceHandle<RenderTarget> resourcehandle = this.getHandle(p_363476_);
            RenderTarget rendertarget = resourcehandle.get();
            rendertarget.setFilterMode(this.bilinear ? FilterMode.LINEAR : FilterMode.NEAREST);
            GpuTexture gputexture = this.depthBuffer ? rendertarget.getDepthTexture() : rendertarget.getColorTexture();
            if (gputexture == null) {
                throw new IllegalStateException("Missing " + (this.depthBuffer ? "depth" : "color") + "texture for target " + this.targetId);
            } else {
                p_396479_.bindSampler(this.samplerName + "Sampler", gputexture);
                p_396479_.setUniform(this.samplerName + "Size", (float)rendertarget.width, (float)rendertarget.height);
            }
        }

        @Override
        public void cleanup(Map<ResourceLocation, ResourceHandle<RenderTarget>> p_363639_) {
            if (this.bilinear) {
                this.getHandle(p_363639_).get().setFilterMode(FilterMode.NEAREST);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public record TextureInput(String samplerName, AbstractTexture texture, int width, int height) implements PostPass.Input {
        @Override
        public void addToPass(FramePass p_364568_, Map<ResourceLocation, ResourceHandle<RenderTarget>> p_370060_) {
        }

        @Override
        public void bindTo(RenderPass p_393743_, Map<ResourceLocation, ResourceHandle<RenderTarget>> p_364335_) {
            p_393743_.bindSampler(this.samplerName + "Sampler", this.texture.getTexture());
            p_393743_.setUniform(this.samplerName + "Size", (float)this.width, (float)this.height);
        }
    }
}