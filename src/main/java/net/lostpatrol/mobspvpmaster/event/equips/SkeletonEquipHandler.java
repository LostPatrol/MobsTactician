package net.lostpatrol.mobspvpmaster.event.equips;

import net.lostpatrol.mobspvpmaster.util.Constants;
import net.lostpatrol.mobspvpmaster.util.Constants.ArmorLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.ArrayList;
import java.util.List;

import static net.lostpatrol.mobspvpmaster.util.Constants.ENHANCED_SKELETON_BOOLEAN;

public class SkeletonEquipHandler {
    private static final float TACTICAL_SKELETON_CHANCE = 1f;
    private static final float EQUIP_IRON_CHANCE = 0.4f;
    private static final float EQUIP_DIAMOND_CHANCE = 0.3f;
    private static final float EQUIP_NETHERITE_CHANCE = 0.1f;
    private static final int IRON_BLOCK_MIN = 12;
    private static final int IRON_BLOCK_MAX = 20;
    private static final int DIAMOND_BLOCK_MIN = 28;
    private static final int DIAMOND_BLOCK_MAX = 40;
    private static final int NETHERITE_BLOCK_MIN = 48;
    private static final int NETHERITE_BLOCK_MAX = 64;

    private static final List<ResourceKey<Enchantment>> IRON_BOW_ENCHANTMENTS = List.of(
            Enchantments.POWER,
            Enchantments.UNBREAKING,
            Enchantments.INFINITY
    );
    private static final List<ResourceKey<Enchantment>> DIAMOND_BOW_ENCHANTMENTS = List.of(
            Enchantments.POWER,
            Enchantments.PUNCH,
            Enchantments.UNBREAKING,
            Enchantments.INFINITY
    );
    private static final List<ResourceKey<Enchantment>> NETHERITE_BOW_ENCHANTMENTS = List.of(
            Enchantments.POWER,
            Enchantments.PUNCH,
            Enchantments.FLAME,
            Enchantments.UNBREAKING,
            Enchantments.MENDING
    );

    private static final List<ResourceKey<Enchantment>> IRON_SWORD_ENCHANTMENTS = List.of(
            Enchantments.UNBREAKING,
            Enchantments.BANE_OF_ARTHROPODS,
            Enchantments.SMITE
            );
    private static final List<ResourceKey<Enchantment>> DIAMOND_SWORD_ENCHANTMENTS = List.of(
            Enchantments.SHARPNESS,
            Enchantments.KNOCKBACK,
            Enchantments.SWEEPING_EDGE,
            Enchantments.SMITE
    );
    private static final List<ResourceKey<Enchantment>> NETHERITE_SWORD_ENCHANTMENTS = List.of(
            Enchantments.SHARPNESS,
            Enchantments.FIRE_ASPECT,
            Enchantments.KNOCKBACK,
            Enchantments.SWEEPING_EDGE,
            Enchantments.MENDING
    );

    public static final List<ResourceKey<Enchantment>> IRON_ARMOR_ENCHANTMENTS = List.of(
            Enchantments.BLAST_PROTECTION,
            Enchantments.FIRE_PROTECTION,
            Enchantments.PROJECTILE_PROTECTION,
            Enchantments.UNBREAKING
    );
    public static final List<ResourceKey<Enchantment>> DIAMOND_ARMOR_ENCHANTMENTS = List.of(
            Enchantments.BLAST_PROTECTION,
            Enchantments.FIRE_PROTECTION,
            Enchantments.PROJECTILE_PROTECTION,
            Enchantments.PROTECTION,
            Enchantments.UNBREAKING
    );
    public static final List<ResourceKey<Enchantment>> NETHERITE_ARMOR_ENCHANTMENTS = List.of(
            Enchantments.PROTECTION,
            Enchantments.UNBREAKING,
            Enchantments.MENDING
    );

    public static void setupEquipmentIfNeeded(Skeleton skeleton, Registry<Enchantment> enchantmentRegistry) {
        if (skeleton.getPersistentData().contains(Constants.ENHANCED_SKELETON_BOOLEAN)) {
            return;
        }

        if (!isArmorEmpty(skeleton)) {
            skeleton.getPersistentData().putBoolean(Constants.ENHANCED_SKELETON_BOOLEAN, false);
            return;
        }

        if (skeleton.getRandom().nextFloat() < TACTICAL_SKELETON_CHANCE) {
            setupTacticalSkeleton(skeleton, enchantmentRegistry);
        } else {
            skeleton.getPersistentData().putBoolean(Constants.ENHANCED_SKELETON_BOOLEAN, false);
        }
    }

    public static boolean isEnhancedSkeleton(Skeleton skeleton) {
        return skeleton.getPersistentData().getBoolean(Constants.ENHANCED_SKELETON_BOOLEAN).orElse(false);
    }

    private static boolean isArmorEmpty(Skeleton skeleton) {
        return skeleton.getItemBySlot(EquipmentSlot.HEAD).isEmpty() &&
                skeleton.getItemBySlot(EquipmentSlot.CHEST).isEmpty() &&
                skeleton.getItemBySlot(EquipmentSlot.LEGS).isEmpty() &&
                skeleton.getItemBySlot(EquipmentSlot.FEET).isEmpty();
    }

    private static void setupTacticalSkeleton(Skeleton skeleton, Registry<Enchantment> enchantmentRegistry) {
        RandomSource random = skeleton.getRandom();
//        skeleton.setLeftHanded(false);

        if (random.nextFloat() < EQUIP_NETHERITE_CHANCE) {
            createEnchantedBow(skeleton, ArmorLevel.NETHERITE, enchantmentRegistry);
            storeMeleeWeaponForSwitch(skeleton, createEnchantedSword(skeleton, ArmorLevel.NETHERITE, enchantmentRegistry));
            setupDefensiveBlocks(skeleton, ArmorLevel.NETHERITE);
            createEnchantedArmor(skeleton, ArmorLevel.NETHERITE, enchantmentRegistry);
        } else if (random.nextFloat() < EQUIP_DIAMOND_CHANCE) {
            createEnchantedBow(skeleton, ArmorLevel.DIAMOND, enchantmentRegistry);
            storeMeleeWeaponForSwitch(skeleton, createEnchantedSword(skeleton, ArmorLevel.DIAMOND, enchantmentRegistry));
            setupDefensiveBlocks(skeleton, ArmorLevel.DIAMOND);
            createEnchantedArmor(skeleton, ArmorLevel.DIAMOND, enchantmentRegistry);
        } else {
            createEnchantedBow(skeleton, ArmorLevel.IRON, enchantmentRegistry);
            storeMeleeWeaponForSwitch(skeleton, createEnchantedSword(skeleton, ArmorLevel.IRON, enchantmentRegistry));
            setupDefensiveBlocks(skeleton, ArmorLevel.IRON);
            if (random.nextFloat() < EQUIP_IRON_CHANCE) {
                createEnchantedArmor(skeleton, ArmorLevel.IRON, enchantmentRegistry);
            }
        }

        skeleton.getPersistentData().putBoolean(ENHANCED_SKELETON_BOOLEAN, true);
    }

    public static void createEnchantedBow(Skeleton skeleton, ArmorLevel armorLevel, Registry<Enchantment> enchantmentRegistry) {
        RandomSource random = skeleton.getRandom();
        if (armorLevel == ArmorLevel.IRON){
            skeleton.setItemSlot(EquipmentSlot.MAINHAND, createEnchantedSingleWeapon(Items.BOW, ArmorLevel.IRON, random, 2, 2, enchantmentRegistry));
        } else if (armorLevel == ArmorLevel.DIAMOND){
            skeleton.setItemSlot(EquipmentSlot.MAINHAND, createEnchantedSingleWeapon(Items.BOW, ArmorLevel.DIAMOND, random, 3, 3, enchantmentRegistry));
        } else if (armorLevel == ArmorLevel.NETHERITE){
            skeleton.setItemSlot(EquipmentSlot.MAINHAND, createEnchantedSingleWeapon(Items.BOW, ArmorLevel.NETHERITE, random, 4, 4, enchantmentRegistry));
        }
    }

    public static ItemStack createEnchantedSword(Skeleton skeleton, ArmorLevel armorLevel, Registry<Enchantment> enchantmentRegistry) {
        RandomSource random = skeleton.getRandom();
        if (armorLevel == ArmorLevel.IRON){
            return createEnchantedSingleWeapon(Items.IRON_SWORD, ArmorLevel.IRON, random, 2, 2, enchantmentRegistry);
        } else if (armorLevel == ArmorLevel.DIAMOND){
            return createEnchantedSingleWeapon(Items.DIAMOND_SWORD, ArmorLevel.DIAMOND, random, 3, 3, enchantmentRegistry);
        } else if (armorLevel == ArmorLevel.NETHERITE){
            return createEnchantedSingleWeapon(Items.NETHERITE_SWORD, ArmorLevel.NETHERITE, random, 4, 4, enchantmentRegistry);
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack createEnchantedSingleWeapon(Item item, ArmorLevel armorLevel, RandomSource random, int maxLevel, int totalLevel, Registry<Enchantment> enchantmentRegistry) {
        List<ResourceKey<Enchantment>> availableEnchantments = switch (armorLevel){
            case IRON -> new ArrayList<>(item == Items.BOW ? IRON_BOW_ENCHANTMENTS : IRON_SWORD_ENCHANTMENTS);
            case DIAMOND -> new ArrayList<>(item == Items.BOW ? DIAMOND_BOW_ENCHANTMENTS : DIAMOND_SWORD_ENCHANTMENTS);
            case NETHERITE -> new ArrayList<>(item == Items.BOW ? NETHERITE_BOW_ENCHANTMENTS : NETHERITE_SWORD_ENCHANTMENTS);
        };
        return randomEnchant(item, random, maxLevel, totalLevel, availableEnchantments, enchantmentRegistry);
    }

    private static void storeMeleeWeaponForSwitch(Skeleton skeleton, ItemStack weapon) {
        if (weapon.isEmpty()) {
            return;
        }
        var registryOps = skeleton.level().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        skeleton.getPersistentData().store(Constants.SKELETON_STORED_MAINHAND_WEAPON, ItemStack.CODEC, registryOps, weapon);
    }

    private static void setupDefensiveBlocks(Skeleton skeleton, ArmorLevel armorLevel) {
        Item blockItem;
        int minCount;
        int maxCount;

        if (armorLevel == ArmorLevel.IRON) {
            blockItem = Items.DIRT;
            minCount = IRON_BLOCK_MIN;
            maxCount = IRON_BLOCK_MAX;
        } else if (armorLevel == ArmorLevel.DIAMOND) {
            blockItem = Items.COBBLESTONE;
            minCount = DIAMOND_BLOCK_MIN;
            maxCount = DIAMOND_BLOCK_MAX;
        } else {
            blockItem = Items.COBBLESTONE;
            minCount = NETHERITE_BLOCK_MIN;
            maxCount = NETHERITE_BLOCK_MAX;
        }

        int blockCount = skeleton.getRandom().nextInt(minCount, maxCount + 1);
        skeleton.getPersistentData().putInt(Constants.SKELETON_BLOCK_COUNT, blockCount);
        skeleton.getPersistentData().putString(Constants.SKELETON_DEFENSE_BLOCK_ITEM_ID, BuiltInRegistries.ITEM.getKey(blockItem).toString());
    }

    public static void createEnchantedArmor(Skeleton skeleton, ArmorLevel armorLevel, Registry<Enchantment> enchantmentRegistry) {
        RandomSource random = skeleton.getRandom();
        if (armorLevel == ArmorLevel.IRON) {
            skeleton.setItemSlot(EquipmentSlot.HEAD, createEnchantedSingleArmor(Items.IRON_HELMET, ArmorLevel.IRON, random, 2, 2, enchantmentRegistry));
            skeleton.setItemSlot(EquipmentSlot.CHEST, createEnchantedSingleArmor(Items.IRON_CHESTPLATE, ArmorLevel.IRON, random, 2, 2, enchantmentRegistry));
            skeleton.setItemSlot(EquipmentSlot.LEGS, createEnchantedSingleArmor(Items.IRON_LEGGINGS, ArmorLevel.IRON, random, 2, 2, enchantmentRegistry));
            skeleton.setItemSlot(EquipmentSlot.FEET, createEnchantedSingleArmor(Items.IRON_BOOTS, ArmorLevel.IRON, random, 2, 2, enchantmentRegistry));
        } else if (armorLevel == ArmorLevel.DIAMOND) {
            skeleton.setItemSlot(EquipmentSlot.HEAD, createEnchantedSingleArmor(Items.DIAMOND_HELMET, ArmorLevel.DIAMOND, random, 3, 3, enchantmentRegistry));
            skeleton.setItemSlot(EquipmentSlot.CHEST, createEnchantedSingleArmor(Items.DIAMOND_CHESTPLATE, ArmorLevel.DIAMOND, random, 3, 3, enchantmentRegistry));
            skeleton.setItemSlot(EquipmentSlot.LEGS, createEnchantedSingleArmor(Items.DIAMOND_LEGGINGS, ArmorLevel.DIAMOND, random, 3, 3, enchantmentRegistry));
            skeleton.setItemSlot(EquipmentSlot.FEET, createEnchantedSingleArmor(Items.DIAMOND_BOOTS, ArmorLevel.DIAMOND, random, 3, 3, enchantmentRegistry));
        } else if (armorLevel == ArmorLevel.NETHERITE) {
            skeleton.setItemSlot(EquipmentSlot.HEAD, createEnchantedSingleArmor(Items.NETHERITE_HELMET, ArmorLevel.NETHERITE, random, 4, 4, enchantmentRegistry));
            skeleton.setItemSlot(EquipmentSlot.CHEST, createEnchantedSingleArmor(Items.NETHERITE_CHESTPLATE, ArmorLevel.NETHERITE, random, 4, 4, enchantmentRegistry));
            skeleton.setItemSlot(EquipmentSlot.LEGS, createEnchantedSingleArmor(Items.NETHERITE_LEGGINGS, ArmorLevel.NETHERITE, random, 4, 4, enchantmentRegistry));
            skeleton.setItemSlot(EquipmentSlot.FEET, createEnchantedSingleArmor(Items.NETHERITE_BOOTS, ArmorLevel.NETHERITE, random, 4, 4, enchantmentRegistry));
        }
    }

    public static ItemStack createEnchantedSingleArmor(Item item, ArmorLevel armorLevel, RandomSource random, int maxLevel, int totalLevel, Registry<Enchantment> enchantmentRegistry){
        List<ResourceKey<Enchantment>> availableEnchantments = switch (armorLevel) {
            case IRON -> new ArrayList<>(IRON_ARMOR_ENCHANTMENTS);
            case DIAMOND -> new ArrayList<>(DIAMOND_ARMOR_ENCHANTMENTS);
            case NETHERITE -> new ArrayList<>(NETHERITE_ARMOR_ENCHANTMENTS);
        };
        ItemStack enchantedItem = randomEnchant(item, random, maxLevel, totalLevel, availableEnchantments, enchantmentRegistry);
        if (item == Items.IRON_BOOTS) enchantedItem.enchant(enchantmentRegistry.getOrThrow(Enchantments.FEATHER_FALLING), 2);
        else if (item == Items.DIAMOND_BOOTS) enchantedItem.enchant(enchantmentRegistry.getOrThrow(Enchantments.FEATHER_FALLING), 3);
        else if (item == Items.NETHERITE_BOOTS){
            enchantedItem.enchant(enchantmentRegistry.getOrThrow(Enchantments.FEATHER_FALLING), 4);
            int level = random.nextInt(2) + 1;
            enchantedItem.enchant(enchantmentRegistry.getOrThrow(Enchantments.SOUL_SPEED), level);
            enchantedItem.enchant(enchantmentRegistry.getOrThrow(Enchantments.DEPTH_STRIDER), 3-level);
        }
        return enchantedItem;
    }

    public static ItemStack randomEnchant(Item item, RandomSource random, int maxLevel, int totalLevel, List<ResourceKey<Enchantment>> availableEnchantments, Registry<Enchantment> enchantmentRegistry){
        int currentTotalLevel = 0;
        List<Holder<Enchantment>> selectedEnchantments = new ArrayList<>();
        List<Integer> selectedLevels = new ArrayList<>();
        List<ResourceKey<Enchantment>> remainingEnchantments = new ArrayList<>(availableEnchantments);
        while (!remainingEnchantments.isEmpty() && currentTotalLevel < totalLevel) {
            int randomIndex = random.nextInt(remainingEnchantments.size());
            ResourceKey<Enchantment> enchantmentKey = remainingEnchantments.get(randomIndex);
            remainingEnchantments.remove(randomIndex);
            Holder<Enchantment> enchantmentHolder = enchantmentRegistry.getOrThrow(enchantmentKey);
            int actualMaxLevel = Math.min(maxLevel, enchantmentHolder.value().getMaxLevel());
            int enchantmentLevel = random.nextInt(actualMaxLevel);
            for(Holder<Enchantment> selected : selectedEnchantments){
                if (!Enchantment.areCompatible(enchantmentHolder, selected)){
                    enchantmentLevel = 0; break;
                }
            }
            if (currentTotalLevel + enchantmentLevel <= totalLevel) {
                selectedEnchantments.add(enchantmentHolder);
                selectedLevels.add(enchantmentLevel);
                currentTotalLevel += enchantmentLevel;
            }
        }
        ItemStack itemStack = new ItemStack(item);
        for (int i = 0; i < selectedEnchantments.size(); i++) {
            itemStack.enchant(selectedEnchantments.get(i), selectedLevels.get(i));
        }
        return itemStack;
    }
}
