package net.lostpatrol.mobstactician.entity.ai.irongolem;

import net.lostpatrol.mobstactician.util.Constants;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.UUID;

public final class IronGolemRepairController {
    private static final int REPAIR_USE_TICKS = 20;
    private static final int ALLY_RESISTANCE_TICKS = 100;
    private static final double ALLY_REPAIR_RANGE = 16.0;
    private static final double ALLY_REPAIR_RANGE_SQR = ALLY_REPAIR_RANGE * ALLY_REPAIR_RANGE;
    private static final float REPAIR_HEALTH = 25.0F;
    private static final float IRON_BLOCK_REPAIR_CHANCE = 0.1F;

    private IronGolemRepairController() {
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

        IronGolem repairTarget = findRepairTarget(ironGolem);
        if (repairTarget == null) {
            resetRepairCycle(ironGolem, gameTime);
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
        if (gameTime - lastRepairTick < requiredDelay || !ironGolem.getMainHandItem().isEmpty()) {
            return;
        }

        startRepair(ironGolem, repairTarget, gameTime);
    }

    private static void startRepair(IronGolem ironGolem, IronGolem repairTarget, long gameTime) {
        ironGolem.getPersistentData().putFloat(
                Constants.IRON_GOLEM_SELF_REPAIR_ORIGINAL_DROP_CHANCE,
                ironGolem.getDropChances().byEquipment(EquipmentSlot.MAINHAND)
        );
        ironGolem.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        ItemStack repairMaterial = new ItemStack(
                ironGolem.getRandom().nextFloat() < IRON_BLOCK_REPAIR_CHANCE ? Items.IRON_BLOCK : Items.IRON_INGOT
        );
        ironGolem.setItemSlot(EquipmentSlot.MAINHAND, repairMaterial);
        ironGolem.getPersistentData().putString(Constants.IRON_GOLEM_REPAIR_TARGET_UUID, repairTarget.getUUID().toString());
        ironGolem.getPersistentData().putBoolean(Constants.IRON_GOLEM_SELF_REPAIR_ACTIVE, true);
        ironGolem.getPersistentData().putLong(Constants.IRON_GOLEM_SELF_REPAIR_FINISH_TICK, gameTime + REPAIR_USE_TICKS);
    }

    private static void tickActiveRepair(IronGolem ironGolem, long gameTime) {
        if (!isRepairMaterial(ironGolem.getMainHandItem())) {
            finishDisplay(ironGolem);
            ironGolem.getPersistentData().putLong(Constants.IRON_GOLEM_LAST_SELF_REPAIR_TICK, gameTime);
            return;
        }

        IronGolem repairTarget;
        if (needsRepair(ironGolem) && isInCombat(ironGolem)) {
            repairTarget = ironGolem;
            ironGolem.getPersistentData().putString(Constants.IRON_GOLEM_REPAIR_TARGET_UUID, ironGolem.getUUID().toString());
        } else {
            repairTarget = resolveActiveRepairTarget(ironGolem);
            if (repairTarget == null) {
                finishDisplay(ironGolem);
                resetRepairCycle(ironGolem, gameTime);
                return;
            }
        }

        long finishTick = ironGolem.getPersistentData().getLong(Constants.IRON_GOLEM_SELF_REPAIR_FINISH_TICK).orElse(gameTime);
        if (gameTime < finishTick) {
            return;
        }

        boolean usingIronBlock = ironGolem.getMainHandItem().is(Items.IRON_BLOCK);
        float repairHealth = usingIronBlock ? REPAIR_HEALTH * 9.0F : REPAIR_HEALTH;
        float missingHealth = repairTarget.getMaxHealth() - repairTarget.getHealth();
        float overflowHealing = usingIronBlock ? Math.max(0.0F, repairHealth - missingHealth) : 0.0F;
        boolean repairedAlly = repairTarget != ironGolem;
        float healthBeforeRepair = repairTarget.getHealth();
        finishDisplay(ironGolem);
        repairTarget.heal(repairHealth);
        if (overflowHealing > 0.0F) {
            addAbsorption(repairTarget, overflowHealing);
        }
        if (repairedAlly && repairTarget.getHealth() > healthBeforeRepair) {
            repairTarget.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, ALLY_RESISTANCE_TICKS, 0), ironGolem);
        }
        ironGolem.playSound(SoundEvents.SMITHING_TABLE_USE, 1.0F, 1.0F);
        ironGolem.getPersistentData().putLong(Constants.IRON_GOLEM_LAST_SELF_REPAIR_TICK, gameTime);
        ironGolem.getPersistentData().putInt(Constants.IRON_GOLEM_FIRST_SELF_REPAIR_DELAY, 0);
    }

    private static IronGolem findRepairTarget(IronGolem ironGolem) {
        if (needsRepair(ironGolem) && isInCombat(ironGolem)) {
            return ironGolem;
        }

        IronGolem mostInjured = needsRepair(ironGolem) ? ironGolem : null;
        for (IronGolem ally : ironGolem.level().getEntitiesOfClass(
                IronGolem.class,
                ironGolem.getBoundingBox().inflate(ALLY_REPAIR_RANGE),
                candidate -> candidate != ironGolem && candidate.isAlive() && needsRepair(candidate)
        )) {
            if (ironGolem.distanceToSqr(ally) <= ALLY_REPAIR_RANGE_SQR
                    && (mostInjured == null || isMoreInjured(ally, mostInjured, ironGolem))) {
                mostInjured = ally;
            }
        }
        return mostInjured;
    }

    private static boolean isMoreInjured(IronGolem candidate, IronGolem current, IronGolem healer) {
        float candidateRatio = candidate.getHealth() / candidate.getMaxHealth();
        float currentRatio = current.getHealth() / current.getMaxHealth();
        int ratioComparison = Float.compare(candidateRatio, currentRatio);
        if (ratioComparison != 0) {
            return ratioComparison < 0;
        }

        float candidateMissingHealth = candidate.getMaxHealth() - candidate.getHealth();
        float currentMissingHealth = current.getMaxHealth() - current.getHealth();
        int missingHealthComparison = Float.compare(candidateMissingHealth, currentMissingHealth);
        if (missingHealthComparison != 0) {
            return missingHealthComparison > 0;
        }

        int distanceComparison = Double.compare(healer.distanceToSqr(candidate), healer.distanceToSqr(current));
        return distanceComparison != 0
                ? distanceComparison < 0
                : candidate.getUUID().compareTo(current.getUUID()) < 0;
    }

    private static IronGolem resolveActiveRepairTarget(IronGolem ironGolem) {
        String targetUuid = ironGolem.getPersistentData().getString(Constants.IRON_GOLEM_REPAIR_TARGET_UUID).orElse("");
        if (targetUuid.isEmpty()) {
            return null;
        }

        try {
            Entity entity = ironGolem.level().getEntity(UUID.fromString(targetUuid));
            if (!(entity instanceof IronGolem repairTarget)
                    || !repairTarget.isAlive()
                    || !needsRepair(repairTarget)
                    || repairTarget != ironGolem && ironGolem.distanceToSqr(repairTarget) > ALLY_REPAIR_RANGE_SQR) {
                return null;
            }
            return repairTarget;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static boolean needsRepair(IronGolem ironGolem) {
        return ironGolem.getHealth() < ironGolem.getMaxHealth();
    }

    private static boolean isInCombat(IronGolem ironGolem) {
        LivingEntity target = ironGolem.getTarget();
        return target != null && target.isAlive();
    }

    private static void resetRepairCycle(IronGolem ironGolem, long gameTime) {
        ironGolem.getPersistentData().putLong(Constants.IRON_GOLEM_LAST_SELF_REPAIR_TICK, gameTime);
        ironGolem.getPersistentData().remove(Constants.IRON_GOLEM_FIRST_SELF_REPAIR_DELAY);
        ironGolem.getPersistentData().remove(Constants.IRON_GOLEM_REPAIR_TARGET_UUID);
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
        ironGolem.getPersistentData().remove(Constants.IRON_GOLEM_REPAIR_TARGET_UUID);
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
