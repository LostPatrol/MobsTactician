package net.lostpatrol.mobstactician.entity.ai.witch;

import java.util.Comparator;
import java.util.EnumSet;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class WitchSupportGoal extends Goal {
    private static final double SEARCH_RANGE = 16.0D;
    private static final double THROW_RANGE_SQR = 12.0D * 12.0D;
    private static final double CONTINUE_RANGE_SQR = 20.0D * 20.0D;
    private static final int EFFECT_REFRESH_THRESHOLD_TICKS = 600;
    private static final int AIM_TICKS = 10;
    private static final int MAX_SUPPORT_TICKS = 200;
    private static final int SEARCH_RETRY_TICKS = 20;
    private static final int THROW_COOLDOWN_TICKS = 60;

    private static final SupportProfile CREEPER_SUPPORT = new SupportProfile(MobEffects.INVISIBILITY, "invisibility");
    private static final SupportProfile SKELETON_SUPPORT = new SupportProfile(MobEffects.SPEED, "swiftness");
    private static final SupportProfile ZOMBIE_SUPPORT = new SupportProfile(MobEffects.STRENGTH, "strength");

    private final Witch witch;
    private @Nullable LivingEntity supportTarget;
    private long nextSearchTick;
    private int supportTicks;
    private int aimTicks;
    private boolean finished;

    public WitchSupportGoal(Witch witch) {
        this.witch = witch;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.witch.isDrinkingPotion()
                || this.witch.level().getGameTime() < this.nextSearchTick) {
            return false;
        }

        this.supportTarget = this.witch.level()
                .getEntitiesOfClass(
                        LivingEntity.class,
                        this.witch.getBoundingBox().inflate(SEARCH_RANGE),
                        this::needsSupport
                )
                .stream()
                .min(Comparator.comparingDouble(this.witch::distanceToSqr))
                .orElse(null);
        if (this.supportTarget == null) {
            this.nextSearchTick = this.witch.level().getGameTime() + SEARCH_RETRY_TICKS;
            return false;
        }

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.finished
                && this.supportTarget != null
                && this.supportTarget.isAlive()
                && !this.witch.isDrinkingPotion()
                && this.supportTicks < MAX_SUPPORT_TICKS
                && this.witch.distanceToSqr(this.supportTarget) <= CONTINUE_RANGE_SQR
                && this.needsSupport(this.supportTarget);
    }

    @Override
    public void start() {
        this.supportTicks = 0;
        this.aimTicks = 0;
        this.finished = false;
    }

    @Override
    public void stop() {
        if (!this.finished && this.supportTicks >= MAX_SUPPORT_TICKS) {
            this.nextSearchTick = this.witch.level().getGameTime() + SEARCH_RETRY_TICKS;
        }
        this.witch.getNavigation().stop();
        this.supportTarget = null;
        this.supportTicks = 0;
        this.aimTicks = 0;
        this.finished = false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.supportTicks++;
        if (this.supportTarget == null) {
            return;
        }

        boolean hasLineOfSight = this.witch.getSensing().hasLineOfSight(this.supportTarget);
        double distanceSqr = this.witch.distanceToSqr(this.supportTarget);
        this.witch.getLookControl().setLookAt(this.supportTarget, 30.0F, 30.0F);
        if (distanceSqr > THROW_RANGE_SQR || !hasLineOfSight) {
            this.aimTicks = 0;
            this.witch.getNavigation().moveTo(this.supportTarget, 1.0D);
            return;
        }

        this.witch.getNavigation().stop();
        if (++this.aimTicks >= AIM_TICKS) {
            this.throwSupportPotion(this.supportTarget);
            this.nextSearchTick = this.witch.level().getGameTime() + THROW_COOLDOWN_TICKS;
            this.finished = true;
        }
    }

    private boolean needsSupport(LivingEntity target) {
        SupportProfile profile = getSupportProfile(target);
        if (profile == null || !target.isAlive()) {
            return false;
        }

        MobEffectInstance currentEffect = target.getEffect(profile.effect());
        return currentEffect == null || currentEffect.getDuration() <= EFFECT_REFRESH_THRESHOLD_TICKS;
    }

    private void throwSupportPotion(LivingEntity target) {
        SupportProfile profile = getSupportProfile(target);
        if (profile == null || !(this.witch.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 targetVelocity = target.getDeltaMovement();
        double dx = target.getX() + targetVelocity.x - this.witch.getX();
        double dy = target.getEyeY() - 1.1F - this.witch.getY();
        double dz = target.getZ() + targetVelocity.z - this.witch.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        ItemStack potion = WitchTacticalPotions.create(
                Items.SPLASH_POTION,
                profile.effect(),
                profile.potionName(),
                this.witch.getRandom()
        );
        Projectile.spawnProjectileUsingShoot(
                ThrownSplashPotion::new,
                serverLevel,
                potion,
                this.witch,
                dx,
                dy + horizontalDistance * 0.2D,
                dz,
                0.75F,
                8.0F
        );

        if (!this.witch.isSilent()) {
            this.witch.level().playSound(
                    null,
                    this.witch.getX(),
                    this.witch.getY(),
                    this.witch.getZ(),
                    SoundEvents.WITCH_THROW,
                    this.witch.getSoundSource(),
                    1.0F,
                    0.8F + this.witch.getRandom().nextFloat() * 0.4F
            );
        }
    }

    private static @Nullable SupportProfile getSupportProfile(LivingEntity target) {
        if (target instanceof Creeper) {
            return CREEPER_SUPPORT;
        }
        if (target instanceof Skeleton) {
            return SKELETON_SUPPORT;
        }
        if (target instanceof Zombie) {
            return ZOMBIE_SUPPORT;
        }
        return null;
    }

    private record SupportProfile(Holder<MobEffect> effect, String potionName) {
    }
}
