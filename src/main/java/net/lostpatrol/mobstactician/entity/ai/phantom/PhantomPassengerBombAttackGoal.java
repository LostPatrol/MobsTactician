package net.lostpatrol.mobstactician.entity.ai.phantom;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class PhantomPassengerBombAttackGoal extends Goal {
    private static final int CAT_SEARCH_TICK_DELAY = 20;
    private static final double DROP_DISTANCE_IN_FRONT = 2.0;
    private static final double PASSENGER_DROP_HEIGHT_ABOVE_PLAYER_FEET = 0.5;
    private static final double DROP_HORIZONTAL_DISTANCE_SQR = 2.25;
    private static final double DROP_VERTICAL_DISTANCE = 0.75;

    private final Phantom phantom;
    private boolean isScaredOfCat;
    private int catSearchTick;
    private @Nullable Player pendingDropTarget;

    public PhantomPassengerBombAttackGoal(Phantom phantom) {
        this.phantom = phantom;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.phantom.getTarget() instanceof Player player
                && player.isAlive()
                && !player.isSpectator()
                && !player.isCreative()
                && this.phantom.attackPhase == Phantom.AttackPhase.SWOOP
                && PhantomCarrier.getPassenger(this.phantom) != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.canUse()) {
            return false;
        }

        if (this.phantom.tickCount > this.catSearchTick) {
            this.catSearchTick = this.phantom.tickCount + CAT_SEARCH_TICK_DELAY;
            List<Cat> cats = this.phantom.level().getEntitiesOfClass(
                    Cat.class,
                    this.phantom.getBoundingBox().inflate(16.0),
                    EntitySelector.ENTITY_STILL_ALIVE
            );
            cats.forEach(Cat::hiss);
            this.isScaredOfCat = !cats.isEmpty();
        }
        return !this.isScaredOfCat;
    }

    @Override
    public void start() {
        this.isScaredOfCat = false;
    }

    @Override
    public void stop() {
        this.phantom.setTarget(null);
        this.phantom.attackPhase = Phantom.AttackPhase.CIRCLE;
    }

    @Override
    public void tick() {
        if (!(this.phantom.getTarget() instanceof Player player)) {
            return;
        }

        Mob passenger = PhantomCarrier.getPassenger(this.phantom);
        if (passenger == null) {
            return;
        }

        Vec3 dropTarget = this.getPhantomDropTarget(player, passenger);
        this.phantom.moveTargetPoint = dropTarget;
        double horizontalDistanceSqr = this.phantom.distanceToSqr(dropTarget.x, this.phantom.getY(), dropTarget.z);
        double verticalDistance = Math.abs(this.phantom.getY() - dropTarget.y);
        if (horizontalDistanceSqr <= DROP_HORIZONTAL_DISTANCE_SQR && verticalDistance <= DROP_VERTICAL_DISTANCE) {
            this.pendingDropTarget = player;
            this.phantom.attackPhase = Phantom.AttackPhase.CIRCLE;
        } else if (this.phantom.horizontalCollision) {
            this.phantom.attackPhase = Phantom.AttackPhase.CIRCLE;
        }
    }

    public void completePendingDrop() {
        Player player = this.pendingDropTarget;
        this.pendingDropTarget = null;
        if (player == null) {
            return;
        }

        Mob passenger = PhantomCarrier.getPassenger(this.phantom);
        if (passenger == null) {
            return;
        }

        this.phantom.positionRider(passenger);
        PhantomCarrier.dropPassenger(this.phantom);
        passenger.setTarget(player);
        Vec3 throwVelocity = this.phantom.getDeltaMovement().add(0.0, -0.2, 0.0);
        passenger.setDeltaMovement(throwVelocity);
    }

    private Vec3 getPhantomDropTarget(Player player, Mob passenger) {
        Vec3 playerToPhantom = new Vec3(
                this.phantom.getX() - player.getX(),
                0.0,
                this.phantom.getZ() - player.getZ()
        );
        Vec3 forward = playerToPhantom.horizontalDistanceSqr() > 1.0E-6
                ? playerToPhantom.normalize().scale(DROP_DISTANCE_IN_FRONT)
                : Vec3.ZERO;
        Vec3 passengerTarget = new Vec3(
                player.getX() + forward.x,
                player.getY() + PASSENGER_DROP_HEIGHT_ABOVE_PLAYER_FEET,
                player.getZ() + forward.z
        );
        Vec3 passengerOffset = this.phantom.getPassengerRidingPosition(passenger)
                .subtract(passenger.getVehicleAttachmentPoint(this.phantom))
                .subtract(this.phantom.position());
        return passengerTarget.subtract(passengerOffset);
    }
}
