package net.lostpatrol.mobspvpmaster.entity.ai.skeleton.controller;

import net.lostpatrol.mobspvpmaster.util.Constants;
import net.minecraft.nbt.NbtOps;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
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
    private static final long WEAPON_SWITCH_COOLDOWN_TICKS = 5;
    private static final double MELEE_SWITCH_DISTANCE_SQR = 4.5 * 5.0;
    private static final double RANGED_SWITCH_DISTANCE_SQR = 5.0 * 5.5;

    private SkeletonWeaponSwitchController() {
    }

    public static boolean onPreTick(Skeleton skeleton) {
        LivingEntity target = skeleton.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        double distanceSqr = skeleton.distanceToSqr(target);
        boolean preferMelee;
        if (distanceSqr < MELEE_SWITCH_DISTANCE_SQR) {
            preferMelee = true;
        } else if (distanceSqr > RANGED_SWITCH_DISTANCE_SQR) {
            preferMelee = false;
        } else {
            return false;
        }

        if (preferMelee ? isMeleeWeapon(skeleton.getMainHandItem()) : isRangedWeapon(skeleton.getMainHandItem())) {
            return false;
        }

        long gameTime = skeleton.level().getGameTime();
        long lastSwitchTick = skeleton.getPersistentData().getLong(Constants.SKELETON_LAST_WEAPON_SWITCH_TICK).orElse(-WEAPON_SWITCH_COOLDOWN_TICKS);
        if (gameTime - lastSwitchTick < WEAPON_SWITCH_COOLDOWN_TICKS) {
            return false;
        }

        if (switchMainhandWeapon(skeleton, preferMelee)) {
            skeleton.getPersistentData().putLong(Constants.SKELETON_LAST_WEAPON_SWITCH_TICK, gameTime);
            return true;
        }

        return false;
    }

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
