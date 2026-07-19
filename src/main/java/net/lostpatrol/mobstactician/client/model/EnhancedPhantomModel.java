package net.lostpatrol.mobstactician.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.phantom.PhantomModel;

public class EnhancedPhantomModel extends PhantomModel {
    private final ModelPart body;
    private final ModelPart leftWingBase;
    private final ModelPart leftWingTip;
    private final ModelPart rightWingBase;
    private final ModelPart rightWingTip;

    public EnhancedPhantomModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.leftWingBase = this.body.getChild("left_wing_base");
        this.leftWingTip = this.leftWingBase.getChild("left_wing_tip");
        this.rightWingBase = this.body.getChild("right_wing_base");
        this.rightWingTip = this.rightWingBase.getChild("right_wing_tip");
    }

    public void translateToLeftWingTip(PoseStack poseStack) {
        this.translateToWingTip(poseStack, this.leftWingBase, this.leftWingTip);
    }

    public void translateToRightWingTip(PoseStack poseStack) {
        this.translateToWingTip(poseStack, this.rightWingBase, this.rightWingTip);
    }

    private void translateToWingTip(PoseStack poseStack, ModelPart wingBase, ModelPart wingTip) {
        this.root().translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        wingBase.translateAndRotate(poseStack);
        wingTip.translateAndRotate(poseStack);
    }
}
