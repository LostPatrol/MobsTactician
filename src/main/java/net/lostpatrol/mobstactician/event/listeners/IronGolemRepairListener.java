package net.lostpatrol.mobstactician.event.listeners;

import net.lostpatrol.mobstactician.MobsTactician;
import net.lostpatrol.mobstactician.entity.ai.irongolem.IronGolemRepairController;
import net.lostpatrol.mobstactician.util.Constants;
import net.lostpatrol.mobstactician.util.TacticalMob;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = MobsTactician.MODID)
public final class IronGolemRepairListener {
    private IronGolemRepairListener() {
    }

    @SubscribeEvent
    public static void onEntityPreTick(EntityTickEvent.Pre event) {
        if (!event.getEntity().level().isClientSide()
                && event.getEntity() instanceof IronGolem ironGolem
                && TacticalMob.isTactical(ironGolem, Constants.ENHANCED_IRON_GOLEM_BOOLEAN)) {
            IronGolemRepairController.tick(ironGolem);
        }
    }
}
