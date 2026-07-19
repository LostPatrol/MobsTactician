package net.lostpatrol.mobstactician.client.render.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.lostpatrol.mobstactician.client.model.EnhancedPhantomModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.lostpatrol.mobstactician.client.render.entity.state.PhantomHoldingRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class EnhancedPhantomEyesLayer extends RenderLayer<PhantomHoldingRenderState, EnhancedPhantomModel> {
    private static final RenderType PHANTOM_EYES = RenderTypes.eyes(Identifier.withDefaultNamespace("textures/entity/phantom_eyes.png"));

    public EnhancedPhantomEyesLayer(RenderLayerParent<PhantomHoldingRenderState, EnhancedPhantomModel> renderLayerParent) {
        super(renderLayerParent);
    }

    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            int packedLight,
            PhantomHoldingRenderState renderState,
            float yRot,
            float xRot
    ) {
        nodeCollector.order(1).submitModel(
                this.getParentModel(),
                renderState,
                poseStack,
                PHANTOM_EYES,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                -1,
                null,
                renderState.outlineColor,
                null
        );
    }
}
