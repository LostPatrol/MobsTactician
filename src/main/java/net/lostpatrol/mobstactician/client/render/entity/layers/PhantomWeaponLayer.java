package net.lostpatrol.mobstactician.client.render.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.lostpatrol.mobstactician.MobsTactician;
import net.lostpatrol.mobstactician.client.model.EnhancedPhantomModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.lostpatrol.mobstactician.client.render.entity.state.PhantomHoldingRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.slf4j.Logger;

public class PhantomWeaponLayer extends RenderLayer<PhantomHoldingRenderState, EnhancedPhantomModel> {
    public static final Logger logger = MobsTactician.LOGGER;

    public PhantomWeaponLayer(RenderLayerParent<PhantomHoldingRenderState, EnhancedPhantomModel> renderLayerParent) {
        super(renderLayerParent);
    }

    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, PhantomHoldingRenderState phantomRenderState, float yRot, float xRot) {
        ItemStackRenderState itemstackrenderstate = phantomRenderState.mainHandItem;
        if (!itemstackrenderstate.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(this.getParentModel().leftWingTip.x / 16.0F, this.getParentModel().leftWingTip.y / 16.0F, this.getParentModel().leftWingTip.z / 16.0F);

            poseStack.mulPose(Axis.YP.rotationDegrees(43.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(10.0F));
            poseStack.mulPose(Axis.ZP.rotation(this.getParentModel().leftWingBase.zRot));
            poseStack.mulPose(Axis.ZP.rotation(this.getParentModel().leftWingTip.zRot));

            poseStack.mulPose(Axis.ZP.rotation(-0.3F));
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
//            poseStack.mulPose(Axis.XP.rotationDegrees(xRot));

//            poseStack.translate(0.5F, -0.24F, -0.5F);
            poseStack.translate(0.6F, 0F, -0.45F);

            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));

            poseStack.scale(1.5F, 1.5F, 1.5F);

            itemstackrenderstate.submit(poseStack, submitNodeCollector, packedLight, OverlayTexture.NO_OVERLAY, phantomRenderState.outlineColor);
            poseStack.popPose();
        }
    }
}