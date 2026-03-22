package net.lostpatrol.mobspvpmaster.event.listeners;

import net.lostpatrol.mobspvpmaster.MobsPVPMaster;
import net.lostpatrol.mobspvpmaster.event.goals.SkeletonGoalHandler;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = MobsPVPMaster.MODID)
public class EntityTickListener {
    @SubscribeEvent
    public static void onEntityPreTick(EntityTickEvent.Pre event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (event.getEntity() instanceof Skeleton skeleton) {
            SkeletonGoalHandler.onPreTick(skeleton);
        }
    }
}
