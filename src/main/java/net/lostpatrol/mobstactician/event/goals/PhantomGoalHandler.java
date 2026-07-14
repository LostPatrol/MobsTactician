package net.lostpatrol.mobstactician.event.goals;

import net.lostpatrol.mobstactician.entity.ai.phantom.PhantomPassengerBombAttackGoal;
import net.lostpatrol.mobstactician.entity.ai.phantom.PhantomPickupPassengerGoal;
import net.lostpatrol.mobstactician.entity.ai.phantom.PhantomRocketChargeAttackGoal;
import net.lostpatrol.mobstactician.event.equips.PhantomEquipHandler;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Phantom;

import java.util.ArrayList;
import java.util.List;

public class PhantomGoalHandler {
    public static void ensureGoals(Phantom phantom) {
        rerankPhantomGoals(phantom);
    }

    private static void rerankPhantomGoals(Phantom phantom) {
        Goal vanillaAttackGoal = null;
        Goal vanillaCircleGoal = null;
        List<Goal> toRemove = new ArrayList<>();

        for (WrappedGoal wrappedGoal : phantom.goalSelector.getAvailableGoals()) {
            Goal goal = wrappedGoal.getGoal();
            if (goal instanceof Phantom.PhantomSweepAttackGoal) {
                vanillaAttackGoal = goal;
                toRemove.add(goal);
            } else if (goal instanceof Phantom.PhantomCircleAroundAnchorGoal) {
                vanillaCircleGoal = goal;
                toRemove.add(goal);
            }
        }

        toRemove.forEach(phantom.goalSelector::removeGoal);
        if (!hasGoal(phantom, PhantomPassengerBombAttackGoal.class)) {
            phantom.goalSelector.addGoal(2, new PhantomPassengerBombAttackGoal(phantom));
        }
        if (!hasGoal(phantom, PhantomPickupPassengerGoal.class)) {
            phantom.goalSelector.addGoal(2, new PhantomPickupPassengerGoal(phantom));
        }
        if (PhantomEquipHandler.isEnhancedPhantom(phantom) && !hasGoal(phantom, PhantomRocketChargeAttackGoal.class)) {
            phantom.goalSelector.addGoal(2, new PhantomRocketChargeAttackGoal(phantom));
        }
        if (vanillaAttackGoal != null) phantom.goalSelector.addGoal(3, vanillaAttackGoal);
        if (vanillaCircleGoal != null) phantom.goalSelector.addGoal(4, vanillaCircleGoal);
    }

    private static boolean hasGoal(Phantom phantom, Class<? extends Goal> goalType) {
        return phantom.goalSelector.getAvailableGoals().stream()
                .anyMatch(wrappedGoal -> goalType.isInstance(wrappedGoal.getGoal()));
    }
}
