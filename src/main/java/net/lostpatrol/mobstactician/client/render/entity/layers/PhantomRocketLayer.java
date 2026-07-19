package net.lostpatrol.mobstactician.client.render.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.lostpatrol.mobstactician.client.model.EnhancedPhantomModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.lostpatrol.mobstactician.client.render.entity.state.PhantomHoldingRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class PhantomRocketLayer extends RenderLayer<PhantomHoldingRenderState, EnhancedPhantomModel> {
    private static final float MODEL_UNIT = 1.0F / 16.0F;

    public PhantomRocketLayer(RenderLayerParent<PhantomHoldingRenderState, EnhancedPhantomModel> renderLayerParent) {
        super(renderLayerParent);
    }

    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, PhantomHoldingRenderState phantomRenderState, float yRot, float xRot) {
        ItemStackRenderState itemstackrenderstate = phantomRenderState.offhandItem;
        if (!itemstackrenderstate.isEmpty()) {
            poseStack.pushPose();
            this.getParentModel().translateToRightWingTip(poseStack);
            poseStack.translate(-5.7F * MODEL_UNIT, 1.5F * MODEL_UNIT, -MODEL_UNIT);
            poseStack.mulPose(Axis.ZP.rotationDegrees(15.5F));
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

            itemstackrenderstate.submit(poseStack, submitNodeCollector, packedLight, OverlayTexture.NO_OVERLAY, phantomRenderState.outlineColor);
            poseStack.popPose();
        }
    }
}
