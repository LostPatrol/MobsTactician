package net.lostpatrol.mobstactician.event.equips;

import net.lostpatrol.mobstactician.config.Config;
import net.lostpatrol.mobstactician.util.Constants.ArmorLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.ArrayList;
import java.util.List;

import static net.lostpatrol.mobstactician.util.Constants.ENHANCED_ZOMBIE_BOOLEAN;

public class ZombieEquipHandler {
    private static final float EQUIP_IRON_CHANCE = 0.4f;
    private static final float EQUIP_DIAMOND_CHANCE = 0.3f;
    private static final float EQUIP_NETHERITE_CHANCE = 0.1f;

    public static final List<ResourceKey<Enchantment>> IRON_MACE_ENCHANTMENTS = List.of(
            Enchantments.UNBREAKING,
            Enchantments.BANE_OF_ARTHROPODS,
            Enchantments.SMITE
    );
    public static final List<ResourceKey<Enchantment>> DIAMOND_MACE_ENCHANTMENTS = List.of(
            Enchantments.UNBREAKING,
            Enchantments.SMITE
    );
    public static final List<ResourceKey<Enchantment>> NETHERITE_MACE_COMMON_ENCHANTMENTS = List.of(
            Enchantments.UNBREAKING,
            Enchantments.MENDING
    );
    public static final List<ResourceKey<Enchantment>> NETHERITE_MACE_ATTACK_ENCHANTMENTS = List.of(
            Enchantments.BREACH,
            Enchantments.DENSITY
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

    public static void setupEquipmentIfNeeded(Zombie zombie, Registry<Enchantment> enchantmentRegistry) {
        if (zombie.getPersistentData().contains(ENHANCED_ZOMBIE_BOOLEAN)
                || (isExcludedZombie(zombie) && !Config.DEBUG_MODE.get())) {
            return;
        }

        if (!Config.DEBUG_MODE.get() && !isEquipmentEmpty(zombie)) {
            zombie.getPersistentData().putBoolean(ENHANCED_ZOMBIE_BOOLEAN, false);
            return;
        }

        if (Config.roll(Config.ZOMBIE_TACTICAL_CHANCE.get(), zombie.getRandom())) {
            setupTacticalZombie(zombie, enchantmentRegistry);
        } else {
            zombie.getPersistentData().putBoolean(ENHANCED_ZOMBIE_BOOLEAN, false);
        }
    }

    public static boolean isEnhancedZombie(Zombie zombie) {
        return zombie.getPersistentData().getBoolean(ENHANCED_ZOMBIE_BOOLEAN).orElse(false);
    }

    public static boolean isExcludedZombie(Zombie zombie) {
        return zombie.isBaby() && zombie.getVehicle() instanceof Chicken;
    }

    private static void setupTacticalZombie(Zombie zombie, Registry<Enchantment> enchantmentRegistry) {
        RandomSource random = zombie.getRandom();
        zombie.setLeftHanded(false);

        if (random.nextFloat() < EQUIP_NETHERITE_CHANCE) {
            createWindCharge(zombie, ArmorLevel.NETHERITE);
            createEnchantedMace(zombie, ArmorLevel.NETHERITE, enchantmentRegistry);
            createEnchantedArmor(zombie, ArmorLevel.NETHERITE, enchantmentRegistry);
        } else if (random.nextFloat() < EQUIP_DIAMOND_CHANCE) {
            createWindCharge(zombie, ArmorLevel.DIAMOND);
            createEnchantedMace(zombie, ArmorLevel.DIAMOND, enchantmentRegistry);
            createEnchantedArmor(zombie, ArmorLevel.DIAMOND, enchantmentRegistry);
        } else {
            createWindCharge(zombie, ArmorLevel.IRON);
            createEnchantedMace(zombie, ArmorLevel.IRON, enchantmentRegistry);
            if (random.nextFloat() < EQUIP_IRON_CHANCE) {
                createEnchantedArmor(zombie, ArmorLevel.IRON, enchantmentRegistry);
            }
        }

        zombie.getPersistentData().putBoolean(ENHANCED_ZOMBIE_BOOLEAN, true);
    }

    private static boolean isEquipmentEmpty(Zombie zombie) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!zombie.getItemBySlot(slot).isEmpty()) return false;
        }
        return true;
    }

    public static void createEnchantedMace(Zombie zombie, ArmorLevel armorLevel, Registry<Enchantment> enchantmentRegistry) {
        RandomSource random = zombie.getRandom();
        if (armorLevel == ArmorLevel.IRON){
            zombie.setItemSlot(EquipmentSlot.MAINHAND, createEnchantedSingleWeapon(Items.MACE, ArmorLevel.IRON, zombie.level().getDifficulty(), random, 2, 2, enchantmentRegistry));
        } else if (armorLevel == ArmorLevel.DIAMOND){
            zombie.setItemSlot(EquipmentSlot.MAINHAND, createEnchantedSingleWeapon(Items.MACE, ArmorLevel.DIAMOND, zombie.level().getDifficulty(), random, 3, 3, enchantmentRegistry));
        } else if (armorLevel == ArmorLevel.NETHERITE){
            zombie.setItemSlot(EquipmentSlot.MAINHAND, createEnchantedSingleWeapon(Items.MACE, ArmorLevel.NETHERITE, zombie.level().getDifficulty(), random, 4, 4, enchantmentRegistry));
        }
    }

    public static void createWindCharge(Zombie zombie, ArmorLevel armorLevel){
        if (armorLevel == ArmorLevel.IRON){
            zombie.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.WIND_CHARGE, 2));
        } else if (armorLevel == ArmorLevel.DIAMOND){
            zombie.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.WIND_CHARGE, 5));
        } else if (armorLevel == ArmorLevel.NETHERITE){
            zombie.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.WIND_CHARGE, 8));
        }
    }

    public static ItemStack createEnchantedSingleWeapon(Item item, ArmorLevel armorLevel, Difficulty difficulty, RandomSource random, int maxLevel, int totalLevel, Registry<Enchantment> enchantmentRegistry) {
        List<ResourceKey<Enchantment>> availableEnchantments = switch (armorLevel){
            case IRON -> new ArrayList<>(IRON_MACE_ENCHANTMENTS);
            case DIAMOND -> new ArrayList<>(DIAMOND_MACE_ENCHANTMENTS);
            case NETHERITE -> new ArrayList<>(NETHERITE_MACE_COMMON_ENCHANTMENTS);
        };
        ItemStack enchantedItem = randomEnchant(item, random, maxLevel, totalLevel, availableEnchantments, enchantmentRegistry);
        if (armorLevel == ArmorLevel.NETHERITE) {
            enchantedItem.enchant(
                    enchantmentRegistry.getOrThrow(NETHERITE_MACE_ATTACK_ENCHANTMENTS.get(random.nextInt(NETHERITE_MACE_ATTACK_ENCHANTMENTS.size()))),
                    random.nextInt(1, 3)
            );
        }
        if (random.nextFloat() < getWindBurstChance(armorLevel, difficulty)) {
            enchantedItem.enchant(enchantmentRegistry.getOrThrow(Enchantments.WIND_BURST), random.nextInt(1, 3));
        }
        return enchantedItem;
    }

    private static float getWindBurstChance(ArmorLevel armorLevel, Difficulty difficulty) {
        return switch (armorLevel) {
            case IRON -> 0.0F;
            case DIAMOND -> switch (difficulty) {
                case EASY -> 0.5F;
                case NORMAL -> 0.6F;
                case HARD -> 0.7F;
                case PEACEFUL -> 0.0F;
            };
            case NETHERITE -> switch (difficulty) {
                case EASY -> 0.8F;
                case NORMAL -> 0.9F;
                case HARD -> 0.95F;
                case PEACEFUL -> 0.0F;
            };
        };
    }

    public static void createEnchantedArmor(Zombie zombie, ArmorLevel armorLevel, Registry<Enchantment> enchantmentRegistry) {
        RandomSource random = zombie.getRandom();
        if (armorLevel == ArmorLevel.IRON) {
            zombie.setItemSlot(EquipmentSlot.HEAD, createEnchantedSingleArmor(Items.IRON_HELMET, ArmorLevel.IRON, random, 2, 2, enchantmentRegistry));
            zombie.setItemSlot(EquipmentSlot.CHEST, createEnchantedSingleArmor(Items.IRON_CHESTPLATE, ArmorLevel.IRON, random, 2, 2, enchantmentRegistry));
            zombie.setItemSlot(EquipmentSlot.LEGS, createEnchantedSingleArmor(Items.IRON_LEGGINGS, ArmorLevel.IRON, random, 2, 2, enchantmentRegistry));
            zombie.setItemSlot(EquipmentSlot.FEET, createEnchantedSingleArmor(Items.IRON_BOOTS, ArmorLevel.IRON, random, 2, 2, enchantmentRegistry));
        } else if (armorLevel == ArmorLevel.DIAMOND) {
            zombie.setItemSlot(EquipmentSlot.HEAD, createEnchantedSingleArmor(Items.DIAMOND_HELMET, ArmorLevel.DIAMOND, random, 3, 3, enchantmentRegistry));
            zombie.setItemSlot(EquipmentSlot.CHEST, createEnchantedSingleArmor(Items.DIAMOND_CHESTPLATE, ArmorLevel.DIAMOND, random, 3, 3, enchantmentRegistry));
            zombie.setItemSlot(EquipmentSlot.LEGS, createEnchantedSingleArmor(Items.DIAMOND_LEGGINGS, ArmorLevel.DIAMOND, random, 3, 3, enchantmentRegistry));
            zombie.setItemSlot(EquipmentSlot.FEET, createEnchantedSingleArmor(Items.DIAMOND_BOOTS, ArmorLevel.DIAMOND, random, 3, 3, enchantmentRegistry));
        } else if (armorLevel == ArmorLevel.NETHERITE) {
            zombie.setItemSlot(EquipmentSlot.HEAD, createEnchantedSingleArmor(Items.NETHERITE_HELMET, ArmorLevel.NETHERITE, random, 4, 4, enchantmentRegistry));
            zombie.setItemSlot(EquipmentSlot.CHEST, createEnchantedSingleArmor(Items.NETHERITE_CHESTPLATE, ArmorLevel.NETHERITE, random, 4, 4, enchantmentRegistry));
            zombie.setItemSlot(EquipmentSlot.LEGS, createEnchantedSingleArmor(Items.NETHERITE_LEGGINGS, ArmorLevel.NETHERITE, random, 4, 4, enchantmentRegistry));
            zombie.setItemSlot(EquipmentSlot.FEET, createEnchantedSingleArmor(Items.NETHERITE_BOOTS, ArmorLevel.NETHERITE, random, 4, 4, enchantmentRegistry));
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
