package net.lostpatrol.mobstactician.event.goals;

import net.lostpatrol.mobstactician.entity.ai.creeper.CreeperPredictiveSwellGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Creeper;

import java.util.ArrayList;
import java.util.List;

public class CreeperGoalHandler {
    private static final int DEFAULT_SWELL_GOAL_PRIORITY = 2;

    public static void ensureGoals(Creeper creeper) {
        int swellGoalPriority = DEFAULT_SWELL_GOAL_PRIORITY;
        boolean hasPredictiveSwellGoal = false;
        List<Goal> vanillaSwellGoals = new ArrayList<>();

        for (WrappedGoal wrappedGoal : creeper.goalSelector.getAvailableGoals()) {
            Goal goal = wrappedGoal.getGoal();

            if (goal instanceof CreeperPredictiveSwellGoal) {
                hasPredictiveSwellGoal = true;
                swellGoalPriority = wrappedGoal.getPriority();
                continue;
            }

            if (goal instanceof SwellGoal) {
                vanillaSwellGoals.add(goal);
                swellGoalPriority = wrappedGoal.getPriority();
            }
        }

        vanillaSwellGoals.forEach(creeper.goalSelector::removeGoal);

        if (!hasPredictiveSwellGoal) {
            creeper.goalSelector.addGoal(swellGoalPriority, new CreeperPredictiveSwellGoal(creeper));
        }
    }
}
