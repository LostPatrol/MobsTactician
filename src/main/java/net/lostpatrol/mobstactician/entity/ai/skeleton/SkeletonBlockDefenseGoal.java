package net.lostpatrol.mobstactician.entity.ai.skeleton;

import net.lostpatrol.mobstactician.MobsTactician;
import net.lostpatrol.mobstactician.util.Constants;
import net.lostpatrol.mobstactician.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.EnumSet;

/**
 * SkeletonBlockDefenseGoal
 * 使战术型小白在察觉玩家拉弓时，迅速摆臂并搭建掩体。
 * 1. 禁止移动（通过 Flag.MOVE 和 navigation.stop）。
 * 2. 放置延迟为 (5 - 难度) tick。
 * 3. 模拟副手持有剩余方块并挥动的动画。
 */
public class SkeletonBlockDefenseGoal extends Goal {
    public static final Logger logger = MobsTactician.LOGGER;

    private final Skeleton skeleton;
    private LivingEntity target;
    private int cooldown = 0;
    private int actionTicks = -1;
    private int tickCounter = 0;
    private BlockPos targetBasePos;
    private int placementInterval;
    private Block defenseBlock = Blocks.COBBLESTONE;

    private ItemStack oldOffhandItem = ItemStack.EMPTY;
    private float oldOffhandDropChance = 0.0F;

    public SkeletonBlockDefenseGoal(Skeleton skeleton) {
        this.skeleton = skeleton;
        // 独占移动和看向
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        if (!this.skeleton.getMainHandItem().is(Items.BOW)) return false;

        int blocks = this.skeleton.getPersistentData().getInt(Constants.SKELETON_BLOCK_COUNT).orElse(0);
        if (blocks <= 0) return false;

        this.target = this.skeleton.getTarget();
        if (this.target instanceof Player player && player.isAlive()) {
            return isPlayerAimingAtSkeleton(player) && isPlayerAboutToShoot(player);
        }
        return false;
    }

    private boolean isPlayerAimingAtSkeleton(Player player) {
        return Util.isPlayerAimingAtEntity(player, this.skeleton, 0.98D, false);
    }

    private boolean isPlayerAboutToShoot(Player player) {
        return player.isUsingItem() && player.getUseItem().is(Items.BOW) && player.getTicksUsingItem() >= 16;
    }

    @Override
    public void start() {
        if (this.target != null) {
            // 好像没用
            this.skeleton.getNavigation().stop();
            this.defenseBlock = resolveDefenseBlock();

            this.oldOffhandItem = this.skeleton.getItemBySlot(EquipmentSlot.OFFHAND).copy();
            this.oldOffhandDropChance = this.skeleton.getDropChances().byEquipment(EquipmentSlot.OFFHAND);
            updateOffhandDisplay();

            Vec3 skeletonPos = this.skeleton.position();
            Vec3 targetPos = this.target.position();
            Vec3 toTarget = new Vec3(targetPos.x - skeletonPos.x, 0, targetPos.z - skeletonPos.z).normalize();
            this.targetBasePos = BlockPos.containing(skeletonPos.x + toTarget.x * 1.5, skeletonPos.y, skeletonPos.z + toTarget.z * 1.5);

            int difficulty = this.skeleton.level().getDifficulty().getId();
            this.placementInterval = Math.max(1, 5 - difficulty);

            this.tickCounter = 0;
            this.actionTicks = 5+this.placementInterval;
            this.cooldown = 40;
        }
    }

    @Override
    public void tick() {
        //还是没用好像
        this.skeleton.getNavigation().stop();
        this.skeleton.getMoveControl().strafe(0.0F, 0.0F);
        Vec3 delta = this.skeleton.getDeltaMovement();
        this.skeleton.setDeltaMovement(0, delta.y, 0); // 保留重力(y)，清除水平(x, z)

        if (this.target != null) {
            this.skeleton.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        }

        if (this.tickCounter == 0) {
            executePlacement(this.targetBasePos);
        } else if (this.tickCounter == this.placementInterval) {
            executePlacement(this.targetBasePos.above());
        }

        this.tickCounter++;
        if (this.actionTicks > 0) {
            this.actionTicks--;
        }
    }

    private void executePlacement(BlockPos pos) {
        int blocks = this.skeleton.getPersistentData().getInt(Constants.SKELETON_BLOCK_COUNT).orElse(0);
        if (blocks <= 0) return;

        if (tryPlaceBlock(this.skeleton.level(), pos)) {
            this.skeleton.swing(InteractionHand.OFF_HAND);

            int remaining = Math.max(0, blocks - 1);
            this.skeleton.getPersistentData().putInt(Constants.SKELETON_BLOCK_COUNT, remaining);

            updateOffhandDisplay();

            Level level = this.skeleton.level();
            SoundType soundType = this.defenseBlock.defaultBlockState().getSoundType();
            level.playSound(null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS, 1.0F, 0.8F);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.defenseBlock.defaultBlockState()),
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.05);
            }
        }
    }

    private void updateOffhandDisplay() {
        int blocks = this.skeleton.getPersistentData().getInt(Constants.SKELETON_BLOCK_COUNT).orElse(0);
        if (blocks > 0) {
            ItemStack blockStack = new ItemStack(this.defenseBlock.asItem(), Math.min(blocks, 64));
            this.skeleton.setItemSlot(EquipmentSlot.OFFHAND, blockStack);
            // 设置不掉落，防止被刷方块
            this.skeleton.setDropChance(EquipmentSlot.OFFHAND, 0.0f);
        } else {
            this.skeleton.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }

    private boolean tryPlaceBlock(Level level, BlockPos pos) {
        if (level.getBlockState(pos).canBeReplaced() && level.getFluidState(pos).isEmpty()) {
            return level.setBlockAndUpdate(pos, this.defenseBlock.defaultBlockState());
        }
        return false;
    }

    private Block resolveDefenseBlock() {
        String itemId = this.skeleton.getPersistentData().getString(Constants.SKELETON_DEFENSE_BLOCK_ITEM_ID).orElse("minecraft:cobblestone");
        Identifier identifier = Identifier.tryParse(itemId);
        if (identifier == null) {
            return Blocks.COBBLESTONE;
        }

        Item item = BuiltInRegistries.ITEM.getValue(identifier);
        if (item instanceof BlockItem blockItem) {
            return blockItem.getBlock();
        }
        return Blocks.COBBLESTONE;
    }

    @Override
    public void stop() {
        this.skeleton.setItemSlot(EquipmentSlot.OFFHAND, this.oldOffhandItem);
        this.skeleton.setDropChance(EquipmentSlot.OFFHAND, this.oldOffhandDropChance);
        this.actionTicks = -1;
    }

    @Override
    public boolean canContinueToUse() {
        return this.actionTicks > 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
