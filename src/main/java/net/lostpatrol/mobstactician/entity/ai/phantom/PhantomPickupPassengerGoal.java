package net.lostpatrol.mobstactician.entity.ai.phantom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class PhantomPickupPassengerGoal extends Goal {
    private static final int IDLE_TICKS = 100;
    private static final int FAILED_SEARCH_DELAY_TICKS = 40;
    private static final double SEARCH_RANGE = 24.0;
    private static final double SEARCH_HEIGHT_ABOVE = 16.0;
    private static final double SEARCH_DEPTH_BELOW = 64.0;
    private final Phantom phantom;
    private int nextSearchTick;
    private @Nullable Mob pickupTarget;

    public PhantomPickupPassengerGoal(Phantom phantom) {
        this.phantom = phantom;
        this.nextSearchTick = phantom.tickCount + IDLE_TICKS;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.phantom.getTarget() != null
                || this.phantom.attackPhase != Phantom.AttackPhase.CIRCLE
                || this.phantom.isVehicle()
                || PhantomCarrier.hasRocketSpearLoadout(this.phantom)) {
            this.nextSearchTick = this.phantom.tickCount + IDLE_TICKS;
            return false;
        }
        if (this.phantom.tickCount < this.nextSearchTick) {
            return false;
        }

        this.pickupTarget = this.findPickupTarget();
        if (this.pickupTarget == null) {
            this.nextSearchTick = this.phantom.tickCount + FAILED_SEARCH_DELAY_TICKS;
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        this.phantom.attackPhase = Phantom.AttackPhase.CIRCLE;
        this.setAnchorAboveTarget();
    }

    @Override
    public boolean canContinueToUse() {
        return this.pickupTarget != null
                && this.pickupTarget.isAlive()
                && this.pickupTarget.getTarget() == null
                && !this.pickupTarget.isPassenger()
                && !this.pickupTarget.isVehicle()
                && this.phantom.getTarget() == null
                && !this.phantom.isVehicle()
                && this.phantom.attackPhase == Phantom.AttackPhase.CIRCLE
                && !PhantomCarrier.hasRocketSpearLoadout(this.phantom);
    }

    @Override
    public void stop() {
        this.pickupTarget = null;
        this.nextSearchTick = this.phantom.tickCount + IDLE_TICKS;
        this.phantom.attackPhase = Phantom.AttackPhase.CIRCLE;
    }

    @Override
    public void tick() {
        if (this.pickupTarget == null) {
            return;
        }

        this.phantom.moveTargetPoint = new Vec3(
                this.pickupTarget.getX(),
                this.pickupTarget.getBoundingBox().maxY + 0.5,
                this.pickupTarget.getZ()
        );

        if (this.phantom.getBoundingBox().inflate(0.5).intersects(this.pickupTarget.getBoundingBox())) {
            PhantomCarrier.pickUp(this.phantom, this.pickupTarget);
        } else if (this.phantom.horizontalCollision || this.phantom.hurtTime > 0) {
            this.pickupTarget = null;
        }
    }

    private @Nullable Mob findPickupTarget() {
        AABB searchBox = this.phantom.getBoundingBox()
                .inflate(SEARCH_RANGE, 0.0, SEARCH_RANGE)
                .expandTowards(0.0, SEARCH_HEIGHT_ABOVE, 0.0)
                .expandTowards(0.0, -SEARCH_DEPTH_BELOW, 0.0);
        List<Mob> candidates = this.phantom.level().getEntitiesOfClass(
                Mob.class,
                searchBox,
                mob -> PhantomCarrier.isSupportedPassenger(mob)
                        && mob.isAlive()
                        && mob.getTarget() == null
                        && !mob.isPassenger()
                        && !mob.isVehicle()
                        && (!(mob instanceof Creeper creeper) || !creeper.isIgnited())
        );

        return candidates.stream()
                .min(Comparator.comparingInt(PhantomPickupPassengerGoal::priority)
                        .thenComparingDouble(this.phantom::distanceToSqr))
                .orElse(null);
    }

    private static int priority(Mob mob) {
        if (mob instanceof Creeper creeper) {
            return creeper.isPowered() ? 0 : 1;
        }
        return 2;
    }

    private void setAnchorAboveTarget() {
        if (this.pickupTarget != null && this.phantom.anchorPoint != null) {
            this.phantom.anchorPoint = this.pickupTarget.blockPosition().above(20 + this.phantom.getRandom().nextInt(20));
            if (this.phantom.anchorPoint.getY() < this.phantom.level().getSeaLevel()) {
                this.phantom.anchorPoint = new BlockPos(
                        this.phantom.anchorPoint.getX(), this.phantom.level().getSeaLevel() + 1, this.phantom.anchorPoint.getZ()
                );
            }
        }
    }
}
