package net.lostpatrol.mobspvpmaster.client.render.entity.layers;

import net.lostpatrol.mobspvpmaster.client.model.EnhancedPhantomModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.lostpatrol.mobspvpmaster.client.render.entity.state.PhantomHoldingRenderState;
import net.minecraft.resources.Identifier;

public class EnhancedPhantomEyesLayer extends EyesLayer<PhantomHoldingRenderState, EnhancedPhantomModel> {
    private static final RenderType PHANTOM_EYES = RenderType.eyes(Identifier.ofDefault("textures/entity/phantom_eyes.png"));

    public EnhancedPhantomEyesLayer(RenderLayerParent<PhantomHoldingRenderState, EnhancedPhantomModel> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public RenderType renderType() {
        return PHANTOM_EYES;
    }
}
