package net.lostpatrol.mobstactician.event.listeners;

import net.lostpatrol.mobstactician.MobsTactician;
import net.lostpatrol.mobstactician.util.Constants;
import net.lostpatrol.mobstactician.util.TacticalMob;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.monster.Creeper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = MobsTactician.MODID)
public final class CreeperFallExplosionListener {
    private CreeperFallExplosionListener() {
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (!(event.getEntity() instanceof Creeper creeper)) {
            return;
        }

        if (!TacticalMob.isTactical(creeper, Constants.ENHANCED_CREEPER_BOOLEAN)) {
            return;
        }

        if (!event.getSource().is(DamageTypeTags.IS_FALL)) {
            return;
        }

        event.setCanceled(true);
        creeper.explodeCreeper();
    }
}
