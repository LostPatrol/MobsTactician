package net.lostpatrol.mobstactician.entity.ai.skeleton;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Bow goal with timing hold logic:
 * when aiming at a blocking player, keep drawing and release on shield drop.
 */
public class SkeletonShieldTimingGoal extends Goal {
    private final Skeleton skeleton;
    private final double speedModifier;
    private int attackIntervalMin;
    private final float attackRadiusSqr;
    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public SkeletonShieldTimingGoal(Skeleton skeleton, double speedModifier, int attackIntervalMin, float attackRadius) {
        this.skeleton = skeleton;
        this.speedModifier = speedModifier;
        this.attackIntervalMin = attackIntervalMin;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public void setMinAttackInterval(int attackCooldown) {
        this.attackIntervalMin = attackCooldown;
    }

    @Override
    public boolean canUse() {
        return this.skeleton.getTarget() != null && this.isHoldingBow();
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse() || !this.skeleton.getNavigation().isDone()) && this.isHoldingBow();
    }

    @Override
    public void start() {
        super.start();
        this.skeleton.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.skeleton.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = -1;
        this.skeleton.stopUsingItem();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.skeleton.getTarget();
        if (target == null) {
            return;
        }

        double distanceSqr = this.skeleton.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean hasSight = this.skeleton.getSensing().hasLineOfSight(target);
        boolean sawTarget = this.seeTime > 0;
        if (hasSight != sawTarget) {
            this.seeTime = 0;
        }

        if (hasSight) {
            this.seeTime++;
        } else {
            this.seeTime--;
        }

        if (!(distanceSqr > this.attackRadiusSqr) && this.seeTime >= 20) {
            this.skeleton.getNavigation().stop();
            this.strafingTime++;
        } else {
            this.skeleton.getNavigation().moveTo(target, this.speedModifier);
            this.strafingTime = -1;
        }

        if (this.strafingTime >= 20) {
            if (this.skeleton.getRandom().nextFloat() < 0.3F) {
                this.strafingClockwise = !this.strafingClockwise;
            }

            if (this.skeleton.getRandom().nextFloat() < 0.3F) {
                this.strafingBackwards = !this.strafingBackwards;
            }

            this.strafingTime = 0;
        }

        if (this.strafingTime > -1) {
            if (distanceSqr > this.attackRadiusSqr * 0.75F) {
                this.strafingBackwards = false;
            } else if (distanceSqr < this.attackRadiusSqr * 0.25F) {
                this.strafingBackwards = true;
            }

            this.skeleton.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
            if (this.skeleton.getControlledVehicle() instanceof Mob vehicle) {
                vehicle.lookAt(target, 30.0F, 30.0F);
            }

            this.skeleton.lookAt(target, 30.0F, 30.0F);
        } else {
            this.skeleton.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (this.skeleton.isUsingItem()) {
            if (!hasSight && this.seeTime < -60) {
                this.skeleton.stopUsingItem();
            } else if (hasSight) {
                int useTicks = this.skeleton.getTicksUsingItem();
                if (useTicks >= 20) {
                    if (this.shouldHoldShot(target)) {
                        return;
                    }

                    this.skeleton.stopUsingItem();
                    this.skeleton.performRangedAttack(target, BowItem.getPowerForTime(useTicks));
                    this.attackTime = this.attackIntervalMin;
                }
            }
        } else if (--this.attackTime <= 0 && this.seeTime >= -60) {
            this.skeleton.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.skeleton, item -> item instanceof BowItem));
        }
    }

    private boolean isHoldingBow() {
        return this.skeleton.isHolding(item -> item.getItem() instanceof BowItem);
    }

    private boolean shouldHoldShot(LivingEntity target) {
        if (!(target instanceof Player player) || !player.isBlocking()) {
            return false;
        }

        ItemStack blockingItem = player.getItemBlockingWith();
        return blockingItem != null && blockingItem.is(Items.SHIELD);
    }
}
