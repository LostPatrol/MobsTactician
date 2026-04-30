package net.lostpatrol.mobstactician.event.listeners;

import net.lostpatrol.mobstactician.MobsTactician;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.monster.Creeper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = MobsTactician.MODID)
public class CreeperFallExplosionListener {
    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (!(event.getEntity() instanceof Creeper creeper)) {
            return;
        }

        if (!event.getSource().is(DamageTypeTags.IS_FALL)) {
            return;
        }

        if (creeper.getTarget() == null) {
            return;
        }

        if (event.getAmount() < creeper.getHealth()) {
            return;
        }

        event.setCanceled(true);
        creeper.explodeCreeper();
    }
}
