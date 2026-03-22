package net.lostpatrol.mobspvpmaster.event.goals;

import net.lostpatrol.mobspvpmaster.entity.ai.phantom.PhantomRocketChargeAttackGoal;
import net.lostpatrol.mobspvpmaster.event.equips.PhantomEquipHandler;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Phantom;

import java.util.ArrayList;
import java.util.List;

public class PhantomGoalHandler {
    public static void ensureGoals(Phantom phantom) {
        if (!PhantomEquipHandler.isEnhancedPhantom(phantom)) {
            return;
        }
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

        if (hasRocketChargeGoal(phantom)) {
            return;
        }

        toRemove.forEach(phantom.goalSelector::removeGoal);
        phantom.goalSelector.addGoal(2, new PhantomRocketChargeAttackGoal(phantom));
        if (vanillaAttackGoal != null) phantom.goalSelector.addGoal(3, vanillaAttackGoal);
        if (vanillaCircleGoal != null) phantom.goalSelector.addGoal(4, vanillaCircleGoal);
    }

    private static boolean hasRocketChargeGoal(Phantom phantom) {
        return phantom.goalSelector.getAvailableGoals().stream()
                .anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof PhantomRocketChargeAttackGoal);
    }
}
