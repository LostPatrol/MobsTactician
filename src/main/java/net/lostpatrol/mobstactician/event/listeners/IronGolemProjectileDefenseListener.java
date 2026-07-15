package net.lostpatrol.mobstactician.event.listeners;

import net.lostpatrol.mobstactician.MobsTactician;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = MobsTactician.MODID)
public final class IronGolemProjectileDefenseListener {
    private static final byte ATTACK_ANIMATION_EVENT = 4;

    private IronGolemProjectileDefenseListener() {
    }

    @SubscribeEvent
    public static void onThrowablePreTick(EntityTickEvent.Pre event) {
        if (event.getEntity().level().isClientSide()
                || !(event.getEntity() instanceof ThrowableProjectile throwable)
                || !isPreTickDeflectable(throwable)) {
            return;
        }

        Vec3 originalMovement = throwable.getDeltaMovement();
        double inertia = throwable.isInWater() ? 0.8 : 0.99;
        Vec3 tickMovement = originalMovement.add(0.0, -throwable.getGravity(), 0.0).scale(inertia);
        throwable.setDeltaMovement(tickMovement);
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(throwable, entity -> canHitEntity(throwable, entity));

        if (!(hitResult instanceof EntityHitResult entityHitResult)
                || !(entityHitResult.getEntity() instanceof IronGolem ironGolem)
                || ignoresPlayerProjectile(ironGolem, throwable)) {
            throwable.setDeltaMovement(originalMovement);
            return;
        }

        throwable.setPos(entityHitResult.getLocation());
        if (deflect(throwable, ironGolem)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        Projectile projectile = event.getProjectile();
        if (projectile.level().isClientSide()
                || !(event.getRayTraceResult() instanceof EntityHitResult entityHitResult)
                || !(entityHitResult.getEntity() instanceof IronGolem ironGolem)
                || !isDeflectable(projectile)
                || ignoresPlayerProjectile(ironGolem, projectile)) {
            return;
        }

        if (deflect(projectile, ironGolem)) {
            event.setCanceled(true);
        }
    }

    private static boolean deflect(Projectile projectile, IronGolem ironGolem) {
        if (projectile.deflect(
                ProjectileDeflection.REVERSE,
                ironGolem,
                EntityReference.of(projectile.getOwner()),
                false
        )) {
            ironGolem.level().broadcastEntityEvent(ironGolem, ATTACK_ANIMATION_EVENT);
            return true;
        }
        return false;
    }

    private static boolean ignoresPlayerProjectile(IronGolem ironGolem, Projectile projectile) {
        return ironGolem.isPlayerCreated() && projectile.getOwner() instanceof Player;
    }

    private static boolean canHitEntity(Projectile projectile, Entity target) {
        if (!target.canBeHitByProjectile()) {
            return false;
        }
        Entity owner = projectile.getOwner();
        return owner == null || projectile.leftOwner || !owner.isPassengerOfSameVehicle(target);
    }

    private static boolean isPreTickDeflectable(ThrowableProjectile projectile) {
        return projectile instanceof AbstractThrownPotion
                || projectile instanceof Snowball
                || projectile instanceof ThrownEgg;
    }

    private static boolean isDeflectable(Projectile projectile) {
        if (projectile instanceof AbstractArrow) {
            return true;
        }
        if (projectile instanceof FireworkRocketEntity fireworkRocket) {
            return fireworkRocket.isShotAtAngle();
        }

        EntityType<?> type = projectile.getType();
        return type == EntityType.FIREBALL
                || type == EntityType.SMALL_FIREBALL
                || type == EntityType.DRAGON_FIREBALL
                || type == EntityType.SHULKER_BULLET
                || type == EntityType.WITHER_SKULL
                || type == EntityType.BREEZE_WIND_CHARGE;
    }
}
