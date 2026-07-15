package net.lostpatrol.mobstactician.client.render.entity;

import net.lostpatrol.mobstactician.client.render.entity.layers.IronGolemRepairItemLayer;
import net.lostpatrol.mobstactician.client.render.entity.state.IronGolemRepairRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.renderer.entity.layers.IronGolemFlowerLayer;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class EnhancedIronGolemRenderer extends IronGolemRenderer {
    public EnhancedIronGolemRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.layers.removeIf(layer -> layer instanceof IronGolemFlowerLayer);
        this.addLayer(new IronGolemRepairItemLayer(this));
    }

    @Override
    public IronGolemRepairRenderState createRenderState() {
        return new IronGolemRepairRenderState();
    }

    @Override
    public void extractRenderState(IronGolem ironGolem, IronGolemRenderState renderState, float partialTick) {
        super.extractRenderState(ironGolem, renderState, partialTick);
        IronGolemRepairRenderState repairState = (IronGolemRepairRenderState) renderState;
        repairState.actualOfferFlowerTick = renderState.offerFlowerTick;
        ItemStack repairItem = ironGolem.getMainHandItem().is(Items.IRON_INGOT)
                || ironGolem.getMainHandItem().is(Items.IRON_BLOCK)
                ? ironGolem.getMainHandItem()
                : ItemStack.EMPTY;
        this.itemModelResolver.updateForLiving(
                repairState.repairItem,
                repairItem,
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                ironGolem
        );
        if (!repairItem.isEmpty() && renderState.attackTicksRemaining <= 0.0F) {
            renderState.offerFlowerTick = 1;
        }
    }
}
