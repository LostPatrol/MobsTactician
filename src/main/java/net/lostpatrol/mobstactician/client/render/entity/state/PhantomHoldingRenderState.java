package net.lostpatrol.mobstactician.client.render.entity.state;

import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class PhantomHoldingRenderState extends HoldingEntityRenderState {
    public float flapTime;
    public int size;

    public final ItemStackRenderState mainHandItem = new ItemStackRenderState();
    public final ItemStackRenderState offhandItem = new ItemStackRenderState();
}