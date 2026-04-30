package net.lostpatrol.mobstactician.event.listeners;

import net.lostpatrol.mobstactician.MobsTactician;
import net.lostpatrol.mobstactician.event.goals.SkeletonGoalHandler;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = MobsTactician.MODID)
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
