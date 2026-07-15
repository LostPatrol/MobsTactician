package net.lostpatrol.mobstactician.event.goals;

import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;

public final class IronGolemGoalHandler {
    private IronGolemGoalHandler() {
    }

    public static void ensureGoals(IronGolem ironGolem) {
        boolean hasFloatGoal = ironGolem.goalSelector.getAvailableGoals().stream()
                .anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof FloatGoal);
        if (!hasFloatGoal) {
            ironGolem.goalSelector.addGoal(0, new FloatGoal(ironGolem));
        }
    }
}
