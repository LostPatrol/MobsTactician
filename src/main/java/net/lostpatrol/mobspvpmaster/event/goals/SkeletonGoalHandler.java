package net.lostpatrol.mobspvpmaster.event.goals;

import net.lostpatrol.mobspvpmaster.entity.ai.skeleton.SkeletonBlockDefenseGoal;
import net.lostpatrol.mobspvpmaster.entity.ai.skeleton.SkeletonShieldTimingGoal;
import net.lostpatrol.mobspvpmaster.entity.ai.skeleton.controller.SkeletonWeaponSwitchController;
import net.lostpatrol.mobspvpmaster.event.equips.SkeletonEquipHandler;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.skeleton.Skeleton;

import java.util.ArrayList;
import java.util.List;

public class SkeletonGoalHandler {
    private static final double SHIELD_TIMING_BOW_SPEED = 1.0;
    private static final float SHIELD_TIMING_BOW_RADIUS = 15.0F;
    private static final int SHIELD_TIMING_INTERVAL_HARD = 20;
    private static final int SHIELD_TIMING_INTERVAL_NORMAL = 40;

    public static void onJoin(Skeleton skeleton) {
        if (!SkeletonEquipHandler.isEnhancedSkeleton(skeleton)) {
            return;
        }
        ensureEnhancedSkeletonGoals(skeleton);
    }

    public static void onPreTick(Skeleton skeleton) {
        if (!SkeletonEquipHandler.isEnhancedSkeleton(skeleton)) {
            return;
        }

        ensureEnhancedSkeletonGoals(skeleton);

        if (SkeletonWeaponSwitchController.onPreTick(skeleton)) {
            // Weapon changes can trigger vanilla reassessWeaponGoal, re-assert our goal layout.
            ensureEnhancedSkeletonGoals(skeleton);
        }
    }

    private static boolean hasBlockDefenseGoal(Skeleton skeleton) {
        return skeleton.goalSelector.getAvailableGoals().stream()
                .anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof SkeletonBlockDefenseGoal);
    }

    private static SkeletonShieldTimingGoal findShieldTimingGoal(Skeleton skeleton) {
        for (WrappedGoal wrappedGoal : skeleton.goalSelector.getAvailableGoals()) {
            if (wrappedGoal.getGoal() instanceof SkeletonShieldTimingGoal shieldTimingGoal) {
                return shieldTimingGoal;
            }
        }
        return null;
    }

    private static void removeVanillaRangedBowGoals(Skeleton skeleton) {
        List<Goal> toRemove = new ArrayList<>();
        for (WrappedGoal wrappedGoal : skeleton.goalSelector.getAvailableGoals()) {
            Goal goal = wrappedGoal.getGoal();
            if (goal instanceof RangedBowAttackGoal && !(goal instanceof SkeletonShieldTimingGoal)) {
                toRemove.add(goal);
            }
        }
        toRemove.forEach(skeleton.goalSelector::removeGoal);
    }

    private static int resolveShieldTimingAttackInterval(Skeleton skeleton) {
        return skeleton.level().getDifficulty() == Difficulty.HARD ? SHIELD_TIMING_INTERVAL_HARD : SHIELD_TIMING_INTERVAL_NORMAL;
    }

    private static void ensureEnhancedSkeletonGoals(Skeleton skeleton) {
        if (!hasBlockDefenseGoal(skeleton)) {
            skeleton.goalSelector.addGoal(3, new SkeletonBlockDefenseGoal(skeleton));
        }

        SkeletonShieldTimingGoal shieldTimingGoal = findShieldTimingGoal(skeleton);
        int interval = resolveShieldTimingAttackInterval(skeleton);
        if (shieldTimingGoal == null) {
            shieldTimingGoal = new SkeletonShieldTimingGoal(skeleton, SHIELD_TIMING_BOW_SPEED, interval, SHIELD_TIMING_BOW_RADIUS);
            skeleton.goalSelector.addGoal(4, shieldTimingGoal);
        } else {
            shieldTimingGoal.setMinAttackInterval(interval);
        }

        removeVanillaRangedBowGoals(skeleton);
    }
}
