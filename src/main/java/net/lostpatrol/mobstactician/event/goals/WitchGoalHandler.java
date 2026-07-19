package net.lostpatrol.mobstactician.event.goals;

import net.lostpatrol.mobstactician.entity.ai.witch.WitchProactiveFireResistanceGoal;
import net.lostpatrol.mobstactician.entity.ai.witch.WitchSupportGoal;
import net.lostpatrol.mobstactician.util.Constants;
import net.lostpatrol.mobstactician.util.TacticalMob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Witch;

public final class WitchGoalHandler {
    private WitchGoalHandler() {
    }

    public static void ensureGoals(Witch witch) {
        if (!TacticalMob.isTactical(witch, Constants.ENHANCED_WITCH_BOOLEAN)) {
            return;
        }

        boolean hasFireResistanceGoal = false;
        boolean hasSupportGoal = false;

        for (WrappedGoal wrappedGoal : witch.goalSelector.getAvailableGoals()) {
            if (wrappedGoal.getGoal() instanceof WitchProactiveFireResistanceGoal) {
                hasFireResistanceGoal = true;
            } else if (wrappedGoal.getGoal() instanceof WitchSupportGoal) {
                hasSupportGoal = true;
            }
        }

        if (!hasFireResistanceGoal) {
            witch.goalSelector.addGoal(0, new WitchProactiveFireResistanceGoal(witch));
        }
        if (!hasSupportGoal) {
            witch.goalSelector.addGoal(1, new WitchSupportGoal(witch));
        }
    }
}
