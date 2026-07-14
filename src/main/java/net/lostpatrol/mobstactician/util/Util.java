package net.lostpatrol.mobstactician.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;

public class Util {
    public static boolean isItemInDrops(Collection<ItemEntity> drops, Item item) {
        for (ItemEntity itemEntity : drops) {
            if (itemEntity.getItem().getItem() == item) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack applyRandomDamage(ItemStack itemStack, RandomSource random, float minDurability, float randomRange) {
        if (itemStack.isDamageableItem()) {
            int maxDamage = itemStack.getMaxDamage();

            float damagePercentage = minDurability + (random.nextFloat() * randomRange);
            int damageAmount = (int) (maxDamage * damagePercentage);

            damageAmount = Math.min(damageAmount, maxDamage - 1);
            itemStack.setDamageValue(damageAmount);
        }
        return itemStack;
    }

    public static boolean isPlayerAimingAtEntity(Player player, LivingEntity target, double minAimDot, boolean requireLineOfSight) {
        if (requireLineOfSight && !player.hasLineOfSight(target)) {
            return false;
        }

        Vec3 toTarget = target.getEyePosition().subtract(player.getEyePosition());
        if (toTarget.lengthSqr() < 1.0E-6D) {
            return true;
        }

        Vec3 look = player.getViewVector(1.0F).normalize();
        return look.dot(toTarget.normalize()) >= minAimDot;
    }

    public static boolean isBlockingWithShieldToward(Player player, LivingEntity threat) {
        ItemStack blockingItem = player.getItemBlockingWith();
        if (blockingItem == null || !blockingItem.is(Items.SHIELD)) {
            return false;
        }

        Vec3 toThreat = threat.position().subtract(player.position());
        Vec3 horizontalDirection = new Vec3(toThreat.x, 0.0D, toThreat.z);
        if (horizontalDirection.lengthSqr() < 1.0E-6D) {
            return true;
        }

        Vec3 horizontalFacing = Vec3.directionFromRotation(0.0F, player.getYHeadRot());
        return horizontalFacing.dot(horizontalDirection.normalize()) >= 0.0D;
    }
}
