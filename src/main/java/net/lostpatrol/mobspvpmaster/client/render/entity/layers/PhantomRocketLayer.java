package net.lostpatrol.mobspvpmaster.client.render.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.lostpatrol.mobspvpmaster.MobsPVPMaster;
import net.lostpatrol.mobspvpmaster.client.model.EnhancedPhantomModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.lostpatrol.mobspvpmaster.client.render.entity.state.PhantomHoldingRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.slf4j.Logger;

public class PhantomRocketLayer extends RenderLayer<PhantomHoldingRenderState, EnhancedPhantomModel> {
    public static final Logger logger = MobsPVPMaster.LOGGER;

    public PhantomRocketLayer(RenderLayerParent<PhantomHoldingRenderState, EnhancedPhantomModel> renderLayerParent) {
        super(renderLayerParent);
    }

    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, PhantomHoldingRenderState phantomRenderState, float yRot, float xRot) {
        ItemStackRenderState itemstackrenderstate = phantomRenderState.offhandItem;
        if (!itemstackrenderstate.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(this.getParentModel().rightWingTip.x / 16.0F, this.getParentModel().rightWingTip.y / 16.0F, this.getParentModel().rightWingTip.z / 16.0F);

            poseStack.mulPose(Axis.ZP.rotation(this.getParentModel().rightWingBase.zRot));
            poseStack.mulPose(Axis.ZP.rotation(this.getParentModel().rightWingTip.zRot));

            poseStack.mulPose(Axis.ZP.rotation(0.27F));
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
//            poseStack.mulPose(Axis.XP.rotationDegrees(xRot));

//            poseStack.translate(0.06F, 0.27F, -0.5F);
            poseStack.translate(-0.5F, 0F, -0.5F);

            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));

            poseStack.scale(1.5F, 1.5F, 1.5F);

            itemstackrenderstate.submit(poseStack, submitNodeCollector, packedLight, OverlayTexture.NO_OVERLAY, phantomRenderState.outlineColor);
            poseStack.popPose();
        }
    }
}