package net.lostpatrol.mobstactician.event.listeners;

import net.lostpatrol.mobstactician.MobsTactician;
import net.lostpatrol.mobstactician.entity.ai.phantom.PhantomCarrier;
import net.lostpatrol.mobstactician.event.goals.SkeletonGoalHandler;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = MobsTactician.MODID)
public class EntityTickListener {
    @SubscribeEvent
    public static void onEntityPreTick(EntityTickEvent.Pre event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof Phantom phantom) {
            PhantomCarrier.syncCarrierGoalControlFlags(phantom);
        }

        if (event.getEntity() instanceof Mob mob && PhantomCarrier.isSupportedPassenger(mob)) {
            boolean carriedByPhantom = event.getEntity().getVehicle() instanceof Phantom;
            boolean hangingPassenger = mob instanceof Creeper || mob instanceof Spider;
            boolean hasCarrierAttachment = mob.getAttachments()
                    .get(EntityAttachment.VEHICLE, 0, 0.0F)
                    .y > 0.0;
            if (hangingPassenger && carriedByPhantom != hasCarrierAttachment) {
                event.getEntity().refreshDimensions();
            }
            if (carriedByPhantom && !event.getEntity().level().isClientSide()) {
                mob.setTarget(null);
                event.setCanceled(true);
                return;
            }
        }

        if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof Skeleton skeleton) {
            SkeletonGoalHandler.onPreTick(skeleton);
        }

    }

    @SubscribeEvent
    public static void onEntityPostTick(EntityTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide()
                && event.getEntity() instanceof Phantom phantom
                && PhantomCarrier.getPassenger(phantom) != null) {
            PhantomCarrier.completePendingDrop(phantom);
        }
    }

    @SubscribeEvent
    public static void onEntitySize(EntityEvent.Size event) {
        if ((event.getEntity() instanceof Creeper || event.getEntity() instanceof Spider)
                && event.getEntity().getVehicle() instanceof Phantom phantom) {
            EntityDimensions passengerSize = event.getNewSize();
            if (event.getEntity() instanceof Spider && phantom.getBbWidth() <= passengerSize.width()) {
                float hangingWidth = Math.nextDown(phantom.getBbWidth());
                passengerSize = new EntityDimensions(
                        hangingWidth,
                        passengerSize.height(),
                        passengerSize.eyeHeight(),
                        passengerSize.attachments(),
                        passengerSize.fixed()
                );
            }

            float attachmentHeight = phantom.getBbHeight() + passengerSize.height();
            EntityAttachments.Builder attachments = EntityAttachments.builder()
                    .attach(EntityAttachment.VEHICLE, 0.0F, attachmentHeight, 0.0F);
            event.setNewSize(passengerSize.withAttachments(attachments));
        }
    }

    @SubscribeEvent
    public static void onPhantomDamaged(LivingIncomingDamageEvent event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof Phantom phantom) {
            PhantomCarrier.dropPassenger(phantom);
        }
    }
}
