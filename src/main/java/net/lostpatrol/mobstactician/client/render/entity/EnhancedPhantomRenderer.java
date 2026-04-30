package net.lostpatrol.mobstactician.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.lostpatrol.mobstactician.MobsTactician;
import net.lostpatrol.mobstactician.client.render.entity.layers.PhantomRocketLayer;
import net.lostpatrol.mobstactician.client.render.entity.layers.PhantomWeaponLayer;
import net.lostpatrol.mobstactician.client.model.EnhancedPhantomModel;
import net.lostpatrol.mobstactician.util.Constants;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.lostpatrol.mobstactician.client.render.entity.layers.EnhancedPhantomEyesLayer;
import net.lostpatrol.mobstactician.client.render.entity.state.PhantomHoldingRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class EnhancedPhantomRenderer extends MobRenderer<Phantom, PhantomHoldingRenderState, EnhancedPhantomModel> {
    public static final Logger logger = MobsTactician.LOGGER;

    private static final Identifier PHANTOM_LOCATION = Identifier.withDefaultNamespace("textures/entity/phantom.png");

    public EnhancedPhantomRenderer(EntityRendererProvider.Context context) {
        super(context, new EnhancedPhantomModel(context.bakeLayer(ModelLayers.PHANTOM)), 0.75F);
        this.addLayer(new EnhancedPhantomEyesLayer(this));
        this.addLayer(new PhantomWeaponLayer(this));
        this.addLayer(new PhantomRocketLayer(this));
        logger.info("Added enhanced phantom renderer");
    }

    @NotNull
    public Identifier getTextureLocation(@NotNull PhantomHoldingRenderState phantomHoldingRenderState) {
        return PHANTOM_LOCATION;
    }

    @NotNull
    public PhantomHoldingRenderState createRenderState() {
        return new PhantomHoldingRenderState();
    }

    public void extractRenderState(@NotNull Phantom phantom, @NotNull PhantomHoldingRenderState renderState, float partialTick) {
        super.extractRenderState(phantom, renderState, partialTick);

        this.itemModelResolver.updateForLiving(renderState.mainHandItem, phantom.getMainHandItem(), ItemDisplayContext.GROUND, phantom);
        this.itemModelResolver.updateForLiving(renderState.offhandItem, phantom.getOffhandItem(), ItemDisplayContext.GROUND, phantom);

        renderState.flapTime = phantom.getUniqueFlapTickOffset() + renderState.ageInTicks;
        renderState.size = phantom.getPhantomSize();
    }

    protected void scale(PhantomHoldingRenderState renderState, PoseStack poseStack) {
        float f = 1.0F + 0.15F * renderState.size;
        poseStack.scale(f, f, f);
        poseStack.translate(0.0F, 1.3125F, 0.1875F);
    }

    protected void setupRotations(@NotNull PhantomHoldingRenderState renderState, @NotNull PoseStack poseStack, float bodyRot, float scale) {
        super.setupRotations(renderState, poseStack, bodyRot, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(renderState.xRot));
    }
}
