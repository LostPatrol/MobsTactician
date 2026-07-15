package net.lostpatrol.mobstactician.client.render.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.lostpatrol.mobstactician.client.render.entity.state.IronGolemRepairRenderState;
import net.minecraft.client.model.animal.golem.IronGolemModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;

public class IronGolemRepairItemLayer extends RenderLayer<IronGolemRenderState, IronGolemModel> {
    public IronGolemRepairItemLayer(RenderLayerParent<IronGolemRenderState, IronGolemModel> parent) {
        super(parent);
    }

    @Override
    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            int packedLight,
            IronGolemRenderState renderState,
            float yRot,
            float xRot
    ) {
        if (!(renderState instanceof IronGolemRepairRenderState repairState)) {
            return;
        }

        if (repairState.repairItem.isEmpty()) {
            submitFlower(poseStack, nodeCollector, packedLight, repairState);
            return;
        }

        poseStack.pushPose();
        ModelPart rightArm = this.getParentModel().getFlowerHoldingArm();
        rightArm.translateAndRotate(poseStack);
        poseStack.translate(-11.0F / 16.0F, 25.0F / 16.0F, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(1.0F / 16.0F, 0.125F, 0.0F);
        poseStack.scale(1.25F, 1.25F, 1.25F);
        repairState.repairItem.submit(
                poseStack,
                nodeCollector,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                repairState.outlineColor
        );
        poseStack.popPose();
    }

    private void submitFlower(
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            int packedLight,
            IronGolemRepairRenderState renderState
    ) {
        if (renderState.actualOfferFlowerTick == 0) {
            return;
        }

        poseStack.pushPose();
        this.getParentModel().getFlowerHoldingArm().translateAndRotate(poseStack);
        poseStack.translate(-1.1875F, 1.0625F, -0.9375F);
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        nodeCollector.submitBlock(
                poseStack,
                Blocks.POPPY.defaultBlockState(),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                renderState.outlineColor
        );
        poseStack.popPose();
    }
}
