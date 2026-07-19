package net.lostpatrol.mobstactician.client.render.entity.state;

import net.minecraft.client.renderer.entity.state.PhantomRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class PhantomHoldingRenderState extends PhantomRenderState {
    public final ItemStackRenderState mainHandItem = new ItemStackRenderState();
    public final ItemStackRenderState offhandItem = new ItemStackRenderState();
}
