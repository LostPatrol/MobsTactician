package net.lostpatrol.mobstactician.entity.ai.skeleton.controller;

import net.lostpatrol.mobstactician.util.Constants;
import net.minecraft.nbt.NbtOps;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;

/**
 * Runtime combat controller for tactical skeleton main-hand weapon switching.
 *
 * This is intentionally not a Goal. It is invoked from entity pre-tick to avoid
 * mutating goal collections while GoalSelector is iterating.
 */
public final class SkeletonWeaponSwitchController {
    // Generic swap gates.
    private static final long WEAPON_SWITCH_COOLDOWN_TICKS = 5;
    private static final double MELEE_SWITCH_DISTANCE_SQR = 4.5 * 5.0;
    private static final double RANGED_SWITCH_DISTANCE_SQR = 5.0 * 5.5;

    // Melee timeout tactic: switched to sword, but cannot land hit in time.
    private static final int MELEE_NO_HIT_SWITCH_TICKS = 80;

    // Tactical ranged lock duration after combo/no-hit trigger.
    private static final int TACTICAL_RANGED_HOLD_TICKS = 60;

    // Combo detection tuning.
    private static final int COMBO_WINDOW_TICKS = 16;
    private static final int COMBO_MIN_INTERVAL_TICKS = 4;
    private static final int COMBO_TRIGGER_HIT_COUNT = 2;
    private static final double COMBO_MAX_DISTANCE_SQR = 6.0 * 6.0;

    private SkeletonWeaponSwitchController() {
    }

    public static boolean onPreTick(Skeleton skeleton) {
        LivingEntity target = skeleton.getTarget();
        if (target == null || !target.isAlive()) {
            resetCombatState(skeleton);
            return false;
        }

        long gameTime = skeleton.level().getGameTime();
        trackRecentHits(skeleton, gameTime);

        if (isForcedRangedActive(skeleton, gameTime)) {
            return ensureRangedWeapon(skeleton, gameTime);
        }

        if (tryActivateRangedTacticWhenComboed(skeleton, gameTime)) {
            return ensureRangedWeapon(skeleton, gameTime);
        }

        double distanceSqr = skeleton.distanceToSqr(target);
        if (shouldActivateRangedTacticWhenMeleeNoHit(skeleton, target, gameTime)) {
            activateForcedRanged(skeleton, gameTime, TACTICAL_RANGED_HOLD_TICKS);
            return ensureRangedWeapon(skeleton, gameTime);
        }

        Boolean preferMelee = null;
        if (distanceSqr < MELEE_SWITCH_DISTANCE_SQR) {
            preferMelee = true;
        } else if (distanceSqr > RANGED_SWITCH_DISTANCE_SQR) {
            preferMelee = false;
        }

        if (preferMelee == null) {
            return false;
        }

        if (preferMelee ? isMeleeWeapon(skeleton.getMainHandItem()) : isRangedWeapon(skeleton.getMainHandItem())) {
            return false;
        }

        if (isSwitchOnCooldown(skeleton, gameTime)) {
            return false;
        }

        if (switchMainhandWeapon(skeleton, preferMelee)) {
            skeleton.getPersistentData().putLong(Constants.SKELETON_LAST_WEAPON_SWITCH_TICK, gameTime);
            if (preferMelee) {
                beginMeleeNoHitTracking(skeleton, target, gameTime);
            } else {
                resetMeleeNoHitTracking(skeleton);
            }
            return true;
        }

        return false;
    }

    /** Update valid incoming-hit streak from skeleton hurt state. */
    private static void trackRecentHits(Skeleton skeleton, long gameTime) {
        long lastHitTick = skeleton.getPersistentData().getLong(Constants.SKELETON_LAST_HIT_GAME_TICK).orElse(-(long) COMBO_WINDOW_TICKS);
        if (gameTime - lastHitTick > COMBO_WINDOW_TICKS) {
            skeleton.getPersistentData().putInt(Constants.SKELETON_RECENT_HIT_STREAK, 0);
        }

        int currentHurtTimestamp = skeleton.getLastHurtByMobTimestamp();
        int trackedHurtTimestamp = skeleton.getPersistentData().getInt(Constants.SKELETON_LAST_HURT_BY_MOB_TIMESTAMP).orElse(-1);
        if (currentHurtTimestamp <= 0 || currentHurtTimestamp == trackedHurtTimestamp) {
            return;
        }

        skeleton.getPersistentData().putInt(Constants.SKELETON_LAST_HURT_BY_MOB_TIMESTAMP, currentHurtTimestamp);

        LivingEntity attacker = skeleton.getLastHurtByMob();
        if (attacker != null && (!attacker.isAlive() || skeleton.distanceToSqr(attacker) > COMBO_MAX_DISTANCE_SQR)) {
            skeleton.getPersistentData().putInt(Constants.SKELETON_RECENT_HIT_STREAK, 0);
            return;
        }

        long hitInterval = gameTime - lastHitTick;
        if (hitInterval < COMBO_MIN_INTERVAL_TICKS) {
            return;
        }

        int hitStreak = skeleton.getPersistentData().getInt(Constants.SKELETON_RECENT_HIT_STREAK).orElse(0);
        hitStreak = hitInterval <= COMBO_WINDOW_TICKS ? hitStreak + 1 : 1;

        skeleton.getPersistentData().putLong(Constants.SKELETON_LAST_HIT_GAME_TICK, gameTime);
        skeleton.getPersistentData().putInt(Constants.SKELETON_RECENT_HIT_STREAK, hitStreak);
    }

    /** Activate ranged tactic when skeleton is being combo-pressured in melee. */
    private static boolean tryActivateRangedTacticWhenComboed(Skeleton skeleton, long gameTime) {
        if (!isMeleeWeapon(skeleton.getMainHandItem())) {
            return false;
        }

        int hitStreak = skeleton.getPersistentData().getInt(Constants.SKELETON_RECENT_HIT_STREAK).orElse(0);
        long lastHitTick = skeleton.getPersistentData().getLong(Constants.SKELETON_LAST_HIT_GAME_TICK).orElse(-(long) COMBO_WINDOW_TICKS);
        if (hitStreak < COMBO_TRIGGER_HIT_COUNT || gameTime - lastHitTick > COMBO_WINDOW_TICKS) {
            return false;
        }

        activateForcedRanged(skeleton, gameTime, TACTICAL_RANGED_HOLD_TICKS);
        skeleton.getPersistentData().putInt(Constants.SKELETON_RECENT_HIT_STREAK, 0);
        return true;
    }

    /** Activate ranged tactic when sword phase exceeds timeout without hitting current target. */
    private static boolean shouldActivateRangedTacticWhenMeleeNoHit(Skeleton skeleton, LivingEntity target, long gameTime) {
        if (!isMeleeWeapon(skeleton.getMainHandItem())) {
            resetMeleeNoHitTracking(skeleton);
            return false;
        }

        String targetUuid = target.getUUID().toString();
        long meleeSinceTick = skeleton.getPersistentData().getLong(Constants.SKELETON_MELEE_NO_HIT_SINCE_TICK).orElse(-1L);
        String trackedTargetUuid = skeleton.getPersistentData().getString(Constants.SKELETON_MELEE_NO_HIT_TARGET_UUID).orElse("");
        if (meleeSinceTick < 0 || !trackedTargetUuid.equals(targetUuid)) {
            beginMeleeNoHitTracking(skeleton, target, gameTime);
            return false;
        }

        int baseTargetHurtTimestamp = skeleton.getPersistentData().getInt(Constants.SKELETON_MELEE_NO_HIT_BASE_TARGET_HURT_TIMESTAMP)
                .orElse(target.getLastHurtByMobTimestamp());
        if (hasLandedHitOnCurrentTarget(skeleton, target, baseTargetHurtTimestamp)) {
            resetMeleeNoHitTracking(skeleton);
            return false;
        }

        return gameTime - meleeSinceTick >= MELEE_NO_HIT_SWITCH_TICKS;
    }

    /** Check if target was newly hurt by this skeleton since tracking baseline. */
    private static boolean hasLandedHitOnCurrentTarget(Skeleton skeleton, LivingEntity target, int baseTargetHurtTimestamp) {
        LivingEntity lastHurtByMob = target.getLastHurtByMob();
        int currentTargetHurtTimestamp = target.getLastHurtByMobTimestamp();
        return lastHurtByMob == skeleton && currentTargetHurtTimestamp > baseTargetHurtTimestamp;
    }

    /** Start melee no-hit tracking for the current target. */
    private static void beginMeleeNoHitTracking(Skeleton skeleton, LivingEntity target, long gameTime) {
        skeleton.getPersistentData().putLong(Constants.SKELETON_MELEE_NO_HIT_SINCE_TICK, gameTime);
        skeleton.getPersistentData().putString(Constants.SKELETON_MELEE_NO_HIT_TARGET_UUID, target.getUUID().toString());
        skeleton.getPersistentData().putInt(Constants.SKELETON_MELEE_NO_HIT_BASE_TARGET_HURT_TIMESTAMP, target.getLastHurtByMobTimestamp());
    }

    /** Clear melee no-hit tracking state. */
    private static void resetMeleeNoHitTracking(Skeleton skeleton) {
        skeleton.getPersistentData().putLong(Constants.SKELETON_MELEE_NO_HIT_SINCE_TICK, -1L);
        skeleton.getPersistentData().putString(Constants.SKELETON_MELEE_NO_HIT_TARGET_UUID, "");
        skeleton.getPersistentData().putInt(Constants.SKELETON_MELEE_NO_HIT_BASE_TARGET_HURT_TIMESTAMP, -1);
    }

    /** Clear all tactical runtime state when target is invalid. */
    private static void resetCombatState(Skeleton skeleton) {
        resetMeleeNoHitTracking(skeleton);
        skeleton.getPersistentData().putInt(Constants.SKELETON_RECENT_HIT_STREAK, 0);
        skeleton.getPersistentData().putLong(Constants.SKELETON_LAST_HIT_GAME_TICK, -(long) COMBO_WINDOW_TICKS);
        skeleton.getPersistentData().putLong(Constants.SKELETON_FORCE_RANGED_UNTIL_TICK, 0L);
    }

    /** Enter forced-ranged mode until the given expiry tick. */
    private static void activateForcedRanged(Skeleton skeleton, long gameTime, int holdTicks) {
        skeleton.getPersistentData().putLong(Constants.SKELETON_FORCE_RANGED_UNTIL_TICK, gameTime + holdTicks);
        resetMeleeNoHitTracking(skeleton);
    }

    /** Whether the forced-ranged lock is still active. */
    private static boolean isForcedRangedActive(Skeleton skeleton, long gameTime) {
        long rangedUntilTick = skeleton.getPersistentData().getLong(Constants.SKELETON_FORCE_RANGED_UNTIL_TICK).orElse(0L);
        return rangedUntilTick > gameTime;
    }

    /** Ensure current weapon is bow while forced-ranged mode is active. */
    private static boolean ensureRangedWeapon(Skeleton skeleton, long gameTime) {
        if (isRangedWeapon(skeleton.getMainHandItem())) {
            return false;
        }
        if (isSwitchOnCooldown(skeleton, gameTime)) {
            return false;
        }
        if (!switchMainhandWeapon(skeleton, false)) {
            return false;
        }
        skeleton.getPersistentData().putLong(Constants.SKELETON_LAST_WEAPON_SWITCH_TICK, gameTime);
        resetMeleeNoHitTracking(skeleton);
        return true;
    }

    /** Shared cooldown guard for any weapon swap operation. */
    private static boolean isSwitchOnCooldown(Skeleton skeleton, long gameTime) {
        long lastSwitchTick = skeleton.getPersistentData().getLong(Constants.SKELETON_LAST_WEAPON_SWITCH_TICK).orElse(-WEAPON_SWITCH_COOLDOWN_TICKS);
        return gameTime - lastSwitchTick < WEAPON_SWITCH_COOLDOWN_TICKS;
    }

    /** Swap main-hand with stored backup weapon when type matches requested mode. */
    private static boolean switchMainhandWeapon(Skeleton skeleton, boolean preferMelee) {
        ItemStack currentWeapon = skeleton.getMainHandItem().copy();
        var registryOps = skeleton.level().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        Optional<ItemStack> optionalStored = skeleton.getPersistentData().read(Constants.SKELETON_STORED_MAINHAND_WEAPON, ItemStack.CODEC, registryOps);

        if (optionalStored.isPresent()) {
            ItemStack storedWeapon = optionalStored.get();
            boolean storedMatchesTarget = preferMelee ? isMeleeWeapon(storedWeapon) : isRangedWeapon(storedWeapon);
            if (!storedMatchesTarget) {
                return false;
            }

            skeleton.setItemSlot(EquipmentSlot.MAINHAND, storedWeapon);
            skeleton.getPersistentData().store(Constants.SKELETON_STORED_MAINHAND_WEAPON, ItemStack.CODEC, registryOps, currentWeapon);
            return true;
        }
        return false;
    }

    private static boolean isMeleeWeapon(ItemStack stack) {
        return stack.is(ItemTags.SWORDS);
    }

    private static boolean isRangedWeapon(ItemStack stack) {
        return stack.is(Items.BOW);
    }
}
