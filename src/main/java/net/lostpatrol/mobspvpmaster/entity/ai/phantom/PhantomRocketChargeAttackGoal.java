package net.lostpatrol.mobspvpmaster.entity.ai.phantom;

import net.lostpatrol.mobspvpmaster.MobsPVPMaster;
import net.lostpatrol.mobspvpmaster.util.Constants;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.EnumSet;
import java.util.List;

public class PhantomRocketChargeAttackGoal extends Goal{
    public static final Logger logger = MobsPVPMaster.LOGGER;

    private final Phantom phantom;
    private static final int ROCKET_DELAY_TICKS = 30;
    private static final int ROCKET_DURATION = 20;
    private boolean hasUsedRocket;
    private int rocketCooldown;
    private boolean isAccelerating;
    private int acceleratedTicks;
    private int acceleratingDuration;

    public PhantomRocketChargeAttackGoal(Phantom phantom) {
        this.phantom = phantom;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    private static final int CAT_SEARCH_TICK_DELAY = 20;
    private boolean isScaredOfCat;
    private int catSearchTick;

    @Override
    public boolean canUse() {
        if (this.phantom.getTarget() == null || this.phantom.attackPhase != Phantom.AttackPhase.SWOOP) {
            return false;
        }
        if (!this.phantom.getPersistentData().getBoolean(Constants.ENHANCED_PHANTOM_BOOLEAN).orElse(false)){
            return false;
        }
        return phantom.getMainHandItem().getItem() == Items.NETHERITE_SPEAR && phantom.getOffhandItem().getItem() == Items.FIREWORK_ROCKET;
    }

    @Override
    public boolean canContinueToUse() {
        logger.info("PhantomRocketChargeAttackGoal.canContinueToUse?");
        LivingEntity livingentity = this.phantom.getTarget();
        if (livingentity == null) {
            return false;
        } else if (!livingentity.isAlive()) {
            return false;
        } else if (livingentity instanceof Player player && (livingentity.isSpectator() || player.isCreative())) {
            return false;
        } else if (!this.canUse()) {
            return false;
        } else {
            if (this.phantom.tickCount > this.catSearchTick) {
                this.catSearchTick = this.phantom.tickCount + 20;
                List<Cat> list = this.phantom.level()
                        .getEntitiesOfClass(Cat.class, this.phantom.getBoundingBox().inflate(16.0), EntitySelector.ENTITY_STILL_ALIVE);
                for (Cat cat : list) {
                    cat.hiss();
                }
                this.isScaredOfCat = !list.isEmpty();
            }
            return !this.isScaredOfCat;
        }
    }

    @Override
    public void start() {
        this.hasUsedRocket = false;
        this.rocketCooldown = ROCKET_DELAY_TICKS;
        this.isAccelerating = false;
        this.acceleratedTicks = 0;
        this.acceleratingDuration = ROCKET_DURATION + this.phantom.level().getDifficulty().ordinal() * 5;
    }

    @Override
    public void stop() {
        this.hasUsedRocket = false;
        this.rocketCooldown = 0;
        this.isAccelerating = false;
        this.acceleratedTicks = 0;
        this.phantom.setTarget(null);
        this.phantom.attackPhase = Phantom.AttackPhase.CIRCLE;
    }

    @Override
    public void tick() {
        LivingEntity livingentity = this.phantom.getTarget();
        if (livingentity != null) {
            this.phantom.moveTargetPoint = new Vec3(livingentity.getX(), livingentity.getY(0.5), livingentity.getZ());

            if (!hasUsedRocket && rocketCooldown > 0) {
                rocketCooldown--;
                if (rocketCooldown <= 0 && this.phantom.getOffhandItem().getItem() == Items.FIREWORK_ROCKET) {
                    this.useRocket();
                    this.hasUsedRocket = true;
                    this.isAccelerating = true;
                    this.acceleratedTicks = 0;
                }
            }

            if (this.isAccelerating) {
                if (acceleratedTicks < this.acceleratingDuration) {
                    Vec3 lookVec = this.phantom.getLookAngle();
                    double spawnX = this.phantom.getX() + lookVec.x * 1.5;
                    double spawnY = this.phantom.getY() + lookVec.y * 1.5 + 1.0;
                    double spawnZ = this.phantom.getZ() + lookVec.z * 1.5;

                    Vec3 direction = new Vec3(
                            livingentity.getX() - spawnX,
                            (livingentity.getY() + livingentity.getEyeHeight() * 0.5) - spawnY,
                            livingentity.getZ() - spawnZ
                    ).normalize();

                    Vec3 currentMotion = this.phantom.getDeltaMovement();
                    Vec3 rocketBoost = direction.scale(0.1 + this.phantom.level().getDifficulty().ordinal() * 0.05);
                    this.phantom.setDeltaMovement(currentMotion.add(rocketBoost));

                    phantom.travelFallFlying(direction);
                    this.acceleratedTicks++;
                } else {
                    this.isAccelerating = false;
                }
            }

            if (this.phantom.getBoundingBox().inflate(0.2F).intersects(livingentity.getBoundingBox())) {
                this.phantom.doHurtTarget(getServerLevel(this.phantom.level()), livingentity);
                this.phantom.attackPhase = Phantom.AttackPhase.CIRCLE;
                if (!this.phantom.isSilent()) {
                    this.phantom.level().levelEvent(1039, this.phantom.blockPosition(), 0);
                }
            } else if (this.phantom.horizontalCollision || this.phantom.hurtTime > 0) {
                this.phantom.attackPhase = Phantom.AttackPhase.CIRCLE;
            }
        }
    }

    private void useRocket() {
        Level level = this.phantom.level();

        if (level instanceof ServerLevel serverlevel) {
            ItemStack rocketStack = phantom.getOffhandItem();
            rocketStack.shrink(1);
            Projectile.spawnProjectile(new FireworkRocketEntity(level, rocketStack, phantom), serverlevel, rocketStack);
            level.playSound(null, phantom, SoundEvents.LEAD_BREAK, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }
}
