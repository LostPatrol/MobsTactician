package net.lostpatrol.mobspvpmaster.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.lostpatrol.mobspvpmaster.MobsPVPMaster;
import net.lostpatrol.mobspvpmaster.client.render.entity.layers.PhantomWeaponLayer;
import net.lostpatrol.mobspvpmaster.client.model.EnhancedPhantomModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.lostpatrol.mobspvpmaster.client.render.entity.layers.EnhancedPhantomEyesLayer;
import net.lostpatrol.mobspvpmaster.client.render.entity.state.PhantomHoldingRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Phantom;
import org.slf4j.Logger;

public class EnhancedPhantomRenderer extends MobRenderer<Phantom, PhantomHoldingRenderState, EnhancedPhantomModel> {
    public Logger logger = MobsPVPMaster.LOGGER;

    private static final Identifier PHANTOM_LOCATION = Identifier.ofDefault("textures/entity/phantom.png");

    public EnhancedPhantomRenderer(EntityRendererProvider.Context context) {
        super(context, new EnhancedPhantomModel(context.bakeLayer(ModelLayers.PHANTOM)), 0.75F);
        this.addLayer(new EnhancedPhantomEyesLayer(this));
        this.addLayer(new PhantomWeaponLayer(this));
        logger.info("Added enhanced phantom renderer");
    }

    public Identifier getTextureLocation(PhantomHoldingRenderState phantomHoldingRenderState) {
        return PHANTOM_LOCATION;
    }

    public PhantomHoldingRenderState createRenderState() {
        return new PhantomHoldingRenderState();
    }

    public void extractRenderState(Phantom phantom, PhantomHoldingRenderState renderState, float partialTick) {
        super.extractRenderState(phantom, renderState, partialTick);
        HoldingEntityRenderState.extractHoldingEntityRenderState(phantom, renderState, this.itemModelResolver);
        renderState.flapTime = phantom.getUniqueFlapTickOffset() + renderState.ageInTicks;
        renderState.size = phantom.getPhantomSize();
    }

    protected void scale(PhantomHoldingRenderState p_364542_, PoseStack p_115682_) {
        float f = 1.0F + 0.15F * p_364542_.size;
        p_115682_.scale(f, f, f);
        p_115682_.translate(0.0F, 1.3125F, 0.1875F);
    }

    protected void setupRotations(PhantomHoldingRenderState renderState, PoseStack poseStack, float bodyRot, float scale) {
        super.setupRotations(renderState, poseStack, bodyRot, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(renderState.xRot));
    }
}
