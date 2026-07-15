package net.lostpatrol.mobstactician.entity.ai.irongolem;

import net.lostpatrol.mobstactician.util.Constants;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class IronGolemSelfRepairController {
    private static final int REPAIR_USE_TICKS = 20;
    private static final float REPAIR_HEALTH = 25.0F;
    private static final float IRON_BLOCK_REPAIR_CHANCE = 0.1F;

    private IronGolemSelfRepairController() {
    }

    public static void tick(IronGolem ironGolem) {
        if (!ironGolem.isAlive()) {
            return;
        }
        long gameTime = ironGolem.level().getGameTime();
        if (ironGolem.getPersistentData().getBoolean(Constants.IRON_GOLEM_SELF_REPAIR_ACTIVE).orElse(false)) {
            tickActiveRepair(ironGolem, gameTime);
            return;
        }

        if (ironGolem.getHealth() >= ironGolem.getMaxHealth()) {
            ironGolem.getPersistentData().putLong(Constants.IRON_GOLEM_LAST_SELF_REPAIR_TICK, gameTime);
            ironGolem.getPersistentData().remove(Constants.IRON_GOLEM_FIRST_SELF_REPAIR_DELAY);
            return;
        }

        if (!ironGolem.getPersistentData().contains(Constants.IRON_GOLEM_LAST_SELF_REPAIR_TICK)) {
            ironGolem.getPersistentData().putLong(Constants.IRON_GOLEM_LAST_SELF_REPAIR_TICK, gameTime);
            return;
        }

        long lastRepairTick = ironGolem.getPersistentData().getLong(Constants.IRON_GOLEM_LAST_SELF_REPAIR_TICK).orElse(gameTime);
        int repairInterval = getRepairInterval(ironGolem.level().getDifficulty());
        if (!ironGolem.getPersistentData().contains(Constants.IRON_GOLEM_FIRST_SELF_REPAIR_DELAY)) {
            int firstRepairDelay = ironGolem.getRandom().nextInt(20, repairInterval + 1);
            ironGolem.getPersistentData().putInt(Constants.IRON_GOLEM_FIRST_SELF_REPAIR_DELAY, firstRepairDelay);
        }
        int firstRepairDelay = ironGolem.getPersistentData().getInt(Constants.IRON_GOLEM_FIRST_SELF_REPAIR_DELAY).orElse(0);
        int requiredDelay = firstRepairDelay > 0
                ? Math.max(0, Math.min(firstRepairDelay, repairInterval) - REPAIR_USE_TICKS)
                : repairInterval;
        if (gameTime - lastRepairTick < requiredDelay
                || !ironGolem.getMainHandItem().isEmpty()) {
            return;
        }

        ironGolem.getPersistentData().putFloat(
                Constants.IRON_GOLEM_SELF_REPAIR_ORIGINAL_DROP_CHANCE,
                ironGolem.getDropChances().byEquipment(EquipmentSlot.MAINHAND)
        );
        ironGolem.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        ItemStack repairMaterial = new ItemStack(
                ironGolem.getRandom().nextFloat() < IRON_BLOCK_REPAIR_CHANCE ? Items.IRON_BLOCK : Items.IRON_INGOT
        );
        ironGolem.setItemSlot(EquipmentSlot.MAINHAND, repairMaterial);
        ironGolem.getPersistentData().putBoolean(Constants.IRON_GOLEM_SELF_REPAIR_ACTIVE, true);
        ironGolem.getPersistentData().putLong(Constants.IRON_GOLEM_SELF_REPAIR_FINISH_TICK, gameTime + REPAIR_USE_TICKS);
    }

    private static void tickActiveRepair(IronGolem ironGolem, long gameTime) {
        if (ironGolem.getHealth() >= ironGolem.getMaxHealth()) {
            finishDisplay(ironGolem);
            ironGolem.getPersistentData().putLong(Constants.IRON_GOLEM_LAST_SELF_REPAIR_TICK, gameTime);
            ironGolem.getPersistentData().remove(Constants.IRON_GOLEM_FIRST_SELF_REPAIR_DELAY);
            return;
        }
        if (!isRepairMaterial(ironGolem.getMainHandItem())) {
            finishDisplay(ironGolem);
            ironGolem.getPersistentData().putLong(Constants.IRON_GOLEM_LAST_SELF_REPAIR_TICK, gameTime);
            return;
        }

        long finishTick = ironGolem.getPersistentData().getLong(Constants.IRON_GOLEM_SELF_REPAIR_FINISH_TICK).orElse(gameTime);
        if (gameTime < finishTick) {
            return;
        }

        boolean usingIronBlock = ironGolem.getMainHandItem().is(Items.IRON_BLOCK);
        float repairHealth = usingIronBlock ? REPAIR_HEALTH * 9.0F : REPAIR_HEALTH;
        float missingHealth = ironGolem.getMaxHealth() - ironGolem.getHealth();
        float overflowHealing = usingIronBlock ? Math.max(0.0F, repairHealth - missingHealth) : 0.0F;
        finishDisplay(ironGolem);
        ironGolem.heal(repairHealth);
        if (overflowHealing > 0.0F) {
            addAbsorption(ironGolem, overflowHealing);
        }
        ironGolem.playSound(SoundEvents.SMITHING_TABLE_USE, 1.0F, 1.0F);
        ironGolem.getPersistentData().putLong(Constants.IRON_GOLEM_LAST_SELF_REPAIR_TICK, gameTime);
        ironGolem.getPersistentData().putInt(Constants.IRON_GOLEM_FIRST_SELF_REPAIR_DELAY, 0);
    }

    private static void finishDisplay(IronGolem ironGolem) {
        if (isRepairMaterial(ironGolem.getMainHandItem())) {
            ironGolem.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        float originalDropChance = ironGolem.getPersistentData()
                .getFloat(Constants.IRON_GOLEM_SELF_REPAIR_ORIGINAL_DROP_CHANCE)
                .orElse(0.085F);
        ironGolem.setDropChance(EquipmentSlot.MAINHAND, originalDropChance);
        ironGolem.getPersistentData().remove(Constants.IRON_GOLEM_SELF_REPAIR_ACTIVE);
        ironGolem.getPersistentData().remove(Constants.IRON_GOLEM_SELF_REPAIR_FINISH_TICK);
        ironGolem.getPersistentData().remove(Constants.IRON_GOLEM_SELF_REPAIR_ORIGINAL_DROP_CHANCE);
    }

    private static boolean isRepairMaterial(ItemStack itemStack) {
        return itemStack.is(Items.IRON_INGOT) || itemStack.is(Items.IRON_BLOCK);
    }

    private static void addAbsorption(IronGolem ironGolem, float amount) {
        float targetAbsorption = ironGolem.getAbsorptionAmount() + amount;
        AttributeInstance maxAbsorption = ironGolem.getAttribute(Attributes.MAX_ABSORPTION);
        if (maxAbsorption != null && maxAbsorption.getValue() < targetAbsorption) {
            double requiredIncrease = targetAbsorption - maxAbsorption.getValue();
            maxAbsorption.setBaseValue(maxAbsorption.getBaseValue() + requiredIncrease);
        }
        ironGolem.setAbsorptionAmount(targetAbsorption);
    }

    private static int getRepairInterval(Difficulty difficulty) {
        return switch (difficulty) {
            case PEACEFUL -> 1200;
            case EASY -> 600;
            case NORMAL -> 300;
            case HARD -> 100;
        };
    }
}
