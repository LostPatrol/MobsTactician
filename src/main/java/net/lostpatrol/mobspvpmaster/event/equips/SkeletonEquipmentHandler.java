package net.lostpatrol.mobspvpmaster.event.equips;

import net.lostpatrol.mobspvpmaster.MobsPVPMaster;
import net.lostpatrol.mobspvpmaster.entity.ai.skeleton.SkeletonBlockDefenseGoal;
import net.lostpatrol.mobspvpmaster.util.Constants;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = MobsPVPMaster.MODID)
public class SkeletonEquipmentHandler {

    private static final float TACTICAL_SKELETON_CHANCE = 0.2f;

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Skeleton skeleton)) return;

        if (skeleton.getPersistentData().contains(Constants.ENHANCED_SKELETON_BOOLEAN)) {
            if (skeleton.getPersistentData().getBoolean(Constants.ENHANCED_SKELETON_BOOLEAN).orElse(false)) {
                if (!hasGoal(skeleton)) {
                    skeleton.goalSelector.addGoal(1, new SkeletonBlockDefenseGoal(skeleton));
                }
            }
            return;
        }

        if (!isArmorEmpty(skeleton)) {
            skeleton.getPersistentData().putBoolean(Constants.ENHANCED_SKELETON_BOOLEAN, false);
            return;
        }

        RandomSource random = skeleton.getRandom();
        if (random.nextFloat() < TACTICAL_SKELETON_CHANCE) {
            setupTacticalSkeleton(skeleton, event.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT));
        } else {
            skeleton.getPersistentData().putBoolean(Constants.ENHANCED_SKELETON_BOOLEAN, false);
        }
    }

    private static boolean hasGoal(Skeleton skeleton) {
        return skeleton.goalSelector.getAvailableGoals().stream()
                .anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof SkeletonBlockDefenseGoal);
    }

    private static boolean isArmorEmpty(Skeleton skeleton) {
        return skeleton.getItemBySlot(EquipmentSlot.HEAD).isEmpty() &&
               skeleton.getItemBySlot(EquipmentSlot.CHEST).isEmpty() &&
               skeleton.getItemBySlot(EquipmentSlot.LEGS).isEmpty() &&
               skeleton.getItemBySlot(EquipmentSlot.FEET).isEmpty();
    }

    private static void setupTacticalSkeleton(Skeleton skeleton, Registry<Enchantment> enchantmentRegistry) {
        RandomSource random = skeleton.getRandom();
        skeleton.getPersistentData().putBoolean(Constants.ENHANCED_SKELETON_BOOLEAN, true);

        int blockCount = random.nextInt(32, 65);
        skeleton.getPersistentData().putInt(Constants.SKELETON_BLOCK_COUNT, blockCount);

        ItemStack helmet = new ItemStack(Items.DIAMOND_HELMET);
        int enchantType = random.nextInt(8); 
        Holder<Enchantment> enchant = (enchantType < 4) ? 
                enchantmentRegistry.getOrThrow(Enchantments.PROTECTION) : 
                enchantmentRegistry.getOrThrow(Enchantments.PROJECTILE_PROTECTION);
        int level = (enchantType % 4) + 1; 
        
        helmet.enchant(enchant, level);
        skeleton.setItemSlot(EquipmentSlot.HEAD, helmet);
        skeleton.setDropChance(EquipmentSlot.HEAD, 0.1f);

        skeleton.goalSelector.addGoal(1, new SkeletonBlockDefenseGoal(skeleton));
        MobsPVPMaster.LOGGER.info("Spawned a Tactical Skeleton with {} blocks and {} {} on helmet.", 
                blockCount, enchant.getRegisteredName(), level);
    }
}
