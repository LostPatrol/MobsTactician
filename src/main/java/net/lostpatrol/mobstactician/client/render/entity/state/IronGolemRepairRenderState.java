package net.lostpatrol.mobstactician.client.render.entity.state;

import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class IronGolemRepairRenderState extends IronGolemRenderState {
    public final ItemStackRenderState repairItem = new ItemStackRenderState();
    public int actualOfferFlowerTick;
}
