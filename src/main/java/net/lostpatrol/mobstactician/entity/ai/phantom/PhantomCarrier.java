package net.lostpatrol.mobstactician.entity.ai.phantom;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

public final class PhantomCarrier {
    private PhantomCarrier() {
    }

    public static boolean isSupportedPassenger(Entity entity) {
        return entity instanceof Zombie || entity instanceof Spider || entity instanceof Creeper;
    }

    public static @Nullable Mob getPassenger(Phantom phantom) {
        Entity passenger = phantom.getFirstPassenger();
        return passenger instanceof Mob mob && isSupportedPassenger(mob) ? mob : null;
    }

    public static boolean pickUp(Phantom phantom, Mob passenger) {
        if (phantom.isVehicle() || passenger.isPassenger() || passenger.isVehicle()) {
            return false;
        }

        passenger.getNavigation().stop();
        if (!passenger.startRiding(phantom, true, true)) {
            return false;
        }

        passenger.refreshDimensions();
        return true;
    }

    public static @Nullable Mob dropPassenger(Phantom phantom) {
        Mob passenger = getPassenger(phantom);
        if (passenger == null) {
            return null;
        }

        passenger.stopRiding();
        passenger.refreshDimensions();
        return passenger;
    }

    public static void syncCarrierGoalControlFlags(Phantom phantom) {
        boolean carryingPassenger = getPassenger(phantom) != null;
        for (WrappedGoal wrappedGoal : phantom.goalSelector.getAvailableGoals()) {
            Goal goal = wrappedGoal.getGoal();
            if (!(goal instanceof Phantom.PhantomCircleAroundAnchorGoal)
                    && !(goal instanceof PhantomPassengerBombAttackGoal)) {
                continue;
            }

            EnumSet<Goal.Flag> flags = EnumSet.noneOf(Goal.Flag.class);
            flags.addAll(goal.getFlags());
            boolean changed = carryingPassenger ? flags.remove(Goal.Flag.MOVE) : flags.add(Goal.Flag.MOVE);
            if (changed) {
                goal.setFlags(flags);
            }
        }
    }

    public static void completePendingDrop(Phantom phantom) {
        for (WrappedGoal wrappedGoal : phantom.goalSelector.getAvailableGoals()) {
            if (wrappedGoal.getGoal() instanceof PhantomPassengerBombAttackGoal bombAttackGoal) {
                bombAttackGoal.completePendingDrop();
                return;
            }
        }
    }
}
