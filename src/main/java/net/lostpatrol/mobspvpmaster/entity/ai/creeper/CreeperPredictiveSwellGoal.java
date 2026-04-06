package net.lostpatrol.mobspvpmaster.entity.ai.creeper;

import net.lostpatrol.mobspvpmaster.MobsPVPMaster;
import net.lostpatrol.mobspvpmaster.util.Util;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.EnumSet;

public class CreeperPredictiveSwellGoal extends Goal {
    private static final double VANILLA_SWELL_RANGE = 3.0D;
    private static final double VANILLA_SWELL_RANGE_SQR = VANILLA_SWELL_RANGE * VANILLA_SWELL_RANGE;
    private static final double VANILLA_CONTINUE_RANGE = 7.0D;
    private static final double VANILLA_CONTINUE_RANGE_SQR = VANILLA_CONTINUE_RANGE * VANILLA_CONTINUE_RANGE;
    private static final double PREDICTIVE_TRIGGER_MAX_DISTANCE = 10.0D;
    private static final double PREDICTIVE_TRIGGER_MAX_DISTANCE_SQR = PREDICTIVE_TRIGGER_MAX_DISTANCE * PREDICTIVE_TRIGGER_MAX_DISTANCE;
    private static final int DISTANCE_PREDICTION_TICKS = 10;
    private static final double MIN_APPROACH_SPEED = 0.012D;
    private static final double MIN_APPROACH_COS = 0.8660254037844386D; // cos(30°)
    private static final double DISTANCE_BUFFER = 1D;


    public static final Logger logger = MobsPVPMaster.LOGGER;

    private final Creeper creeper;
    private @Nullable LivingEntity target;
    private int sampledPlayerId = Integer.MIN_VALUE;
    private @Nullable Vec3 lastSampledPlayerPos;
    private Vec3 cachedPlayerVelocity = Vec3.ZERO;
    private long lastVelocitySampleTick = Long.MIN_VALUE;

    public CreeperPredictiveSwellGoal(Creeper creeper) {
        this.creeper = creeper;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        LivingEntity livingEntity = this.creeper.getTarget();
        logger.info("shouldPredictivelyPrime?:{}", shouldPredictivelyPrime(livingEntity));
        return this.creeper.getSwellDir() > 0
                || livingEntity != null && (this.creeper.distanceToSqr(livingEntity) < VANILLA_SWELL_RANGE_SQR || shouldPredictivelyPrime(livingEntity));
    }

    @Override
    public void start() {
        this.creeper.getNavigation().stop();
        this.target = this.creeper.getTarget();
    }

    @Override
    public void stop() {
        this.target = null;
        this.sampledPlayerId = Integer.MIN_VALUE;
        this.lastSampledPlayerPos = null;
        this.cachedPlayerVelocity = Vec3.ZERO;
        this.lastVelocitySampleTick = Long.MIN_VALUE;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.target == null) {
            this.creeper.setSwellDir(-1);
        } else if (this.creeper.distanceTo(this.target) > VANILLA_CONTINUE_RANGE - 2) {
            this.creeper.setSwellDir(-1);
        } else if (!this.creeper.getSensing().hasLineOfSight(this.target)) {
            this.creeper.setSwellDir(-1);
        } else {
            this.creeper.setSwellDir(1);
        }
//
//        if (this.creeper.distanceToSqr(this.target) <= VANILLA_SWELL_RANGE_SQR || shouldPredictivelyPrime(this.target)) {
//            this.creeper.setSwellDir(1);
//            return;
//        }
//
//        this.creeper.setSwellDir(-1);
    }

    private boolean shouldPredictivelyPrime(LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) {
            return false;
        }

        if (!player.isAlive() || player.isSpectator() || player.isCreative()) {
            return false;
        }

        if (!isHoldingMeleeWeapon(player.getMainHandItem())) {
            return false;
        }

        if (!Util.isPlayerAimingAtEntity(player, this.creeper, 0.7D, true)) {
            return false;
        }

        return isApproachingExplosionRange(player);
    }

    private boolean isHoldingMeleeWeapon(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES) || stack.is(Items.MACE) || stack.is(Items.TRIDENT)) {
            return true;
        }

//        final double[] attackDamageBonus = {0.0D};
//        stack.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
//            if (attribute.equals(Attributes.ATTACK_DAMAGE)) {
//                attackDamageBonus[0] += modifier.amount();
//            }
//        });
//        return attackDamageBonus[0] >= 2.0D;

        return true;
    }

    private boolean isApproachingExplosionRange(Player player) {
        double difficultyScale = resolveDifficultyScale();
        double maxPredictiveDistance = PREDICTIVE_TRIGGER_MAX_DISTANCE + 1.5D * difficultyScale;
        int predictionTicks = DISTANCE_PREDICTION_TICKS + (int)Math.round(4.0D * difficultyScale);
        double distanceBuffer = DISTANCE_BUFFER + 1.25D * difficultyScale;
        double minApproachCos = Math.max(0.0D, MIN_APPROACH_COS - 0.08D * difficultyScale);

        Vec3 creeperPos = this.creeper.position();
        Vec3 playerPos = player.position();
        double dx = creeperPos.x - playerPos.x;
        double dz = creeperPos.z - playerPos.z;
        double distanceSqr = dx * dx + dz * dz;
        if (distanceSqr <= VANILLA_SWELL_RANGE_SQR || distanceSqr > maxPredictiveDistance * maxPredictiveDistance) {
            return false;
        }

        Vec3 playerVelocity = samplePlayerVelocity(player);
        double vx = playerVelocity.x;
        double vz = playerVelocity.z;
        double playerSpeedSqr = vx * vx + vz * vz;
        if (playerSpeedSqr < MIN_APPROACH_SPEED * MIN_APPROACH_SPEED) {
            return false;
        }

        // (vx, vz)* (dx, dz) = v^2 r^2 cos^2
        double dot = vx * dx + vz * dz;
        if (dot <= 0.0D) {
            return false;
        }

        double minApproachCosSqr = minApproachCos * minApproachCos;
        if (dot * dot < minApproachCosSqr * playerSpeedSqr * distanceSqr) {
            return false;
        }

        double distance = Math.sqrt(distanceSqr);
        double approachSpeed = dot / distance;
        double predictedDistance = distance - approachSpeed * predictionTicks;
        return predictedDistance <= VANILLA_SWELL_RANGE + distanceBuffer;
    }

    private double resolveDifficultyScale() {
        return switch (this.creeper.level().getDifficulty()) {
            case HARD -> 1.0D;
            case NORMAL -> 0.5D;
            default -> 0.0D;
        };
    }

    private Vec3 samplePlayerVelocity(Player player) {
        long currentTick = this.creeper.level().getGameTime();
        if (this.sampledPlayerId != player.getId()) {
            this.sampledPlayerId = player.getId();
            this.lastSampledPlayerPos = player.position();
            this.cachedPlayerVelocity = Vec3.ZERO;
            this.lastVelocitySampleTick = currentTick;
            return this.cachedPlayerVelocity;
        }

        if (this.lastVelocitySampleTick == currentTick) {
            return this.cachedPlayerVelocity;
        }

        Vec3 currentPos = player.position();
        if (this.lastSampledPlayerPos == null) {
            this.cachedPlayerVelocity = Vec3.ZERO;
        } else {
            this.cachedPlayerVelocity = currentPos.subtract(this.lastSampledPlayerPos);
        }

        this.lastSampledPlayerPos = currentPos;
        this.lastVelocitySampleTick = currentTick;
        return this.cachedPlayerVelocity;
    }
}
