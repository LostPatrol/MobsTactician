package net.lostpatrol.mobspvpmaster.event.equips;

import net.lostpatrol.mobspvpmaster.MobsPVPMaster;
import net.lostpatrol.mobspvpmaster.entity.ai.zombie.ZombieAerialMaceAttackGoal;
import net.lostpatrol.mobspvpmaster.util.Constants.ArmorLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.ArrayList;
import java.util.List;

import static net.lostpatrol.mobspvpmaster.util.Constants.ENHANCED_ZOMBIE_BOOLEAN;

@EventBusSubscriber(modid = MobsPVPMaster.MODID)
public class ZombieEquipmentHandler {
    private static final float EQUIP_MACE_CHANCE = 0.1f;

    private static final float EQUIP_IRON_CHANCE = 0.4f;
    private static final float EQUIP_DIAMOND_CHANCE = 0.3f;
    private static final float EQUIP_NETHERITE_CHANCE = 0.1f;
    private static final int WIND_BULLET_COOLDOWN = 100;

    public static final int[] LEVEL_CHANCES = new int[]{
            0,
            0
    };

    public static final List<ResourceKey<Enchantment>> IRON_WEAPON_COMMON_ENCHANTMENTS = List.of(
            Enchantments.UNBREAKING,
            Enchantments.BANE_OF_ARTHROPODS,
            Enchantments.SMITE
    );

    public static final List<ResourceKey<Enchantment>> DIAMOND_WEAPON_COMMON_ENCHANTMENTS = List.of(
            Enchantments.UNBREAKING,
            Enchantments.SMITE
    );

    public static final List<ResourceKey<Enchantment>> NETHERITE_WEAPON_COMMON_ENCHANTMENTS = List.of(
            Enchantments.UNBREAKING,
            Enchantments.MENDING
    );

    public static final List<ResourceKey<Enchantment>> NETHERITE_MACE_EXTRA_ENCHANTMENTS = List.of(
            Enchantments.BREACH,
            Enchantments.DENSITY,
            Enchantments.WIND_BURST
    );

    public static final List<ResourceKey<Enchantment>> IRON_ARMOR_COMMON_ENCHANTMENTS = List.of(
            Enchantments.BLAST_PROTECTION,
            Enchantments.FIRE_PROTECTION,
            Enchantments.PROJECTILE_PROTECTION,
            Enchantments.UNBREAKING
    );

    public static final List<ResourceKey<Enchantment>> DIAMOND_ARMOR_COMMON_ENCHANTMENTS = List.of(
            Enchantments.BLAST_PROTECTION,
            Enchantments.FIRE_PROTECTION,
            Enchantments.PROJECTILE_PROTECTION,
            Enchantments.PROTECTION,
            Enchantments.UNBREAKING
    );

    public static final List<ResourceKey<Enchantment>> NETHERITE_ARMOR_COMMON_ENCHANTMENTS = List.of(
            Enchantments.PROTECTION,
            Enchantments.UNBREAKING,
            Enchantments.MENDING
    );

    public static final List<ResourceKey<Enchantment>> NETHERITE_BOOTS_EXTRA_ENCHANTMENTS = List.of(
            Enchantments.DEPTH_STRIDER,
            Enchantments.SOUL_SPEED
    );


    public static void createEnchantedWeapon(Zombie zombie, ArmorLevel armorLevel, Registry<Enchantment> enchantmentRegistry) {
        RandomSource random = zombie.getRandom();
        if (armorLevel == ArmorLevel.IRON){
            zombie.setItemSlot(EquipmentSlot.MAINHAND, createEnchantedSingleWeapon(Items.MACE, ArmorLevel.IRON, random, 2, 2, enchantmentRegistry));
            zombie.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.WIND_CHARGE, 2));
        }
        else if (armorLevel == ArmorLevel.DIAMOND){
            zombie.setItemSlot(EquipmentSlot.MAINHAND, createEnchantedSingleWeapon(Items.MACE, ArmorLevel.DIAMOND, random, 3, 3, enchantmentRegistry));
            zombie.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.WIND_CHARGE, 5));
        }
        else if (armorLevel == ArmorLevel.NETHERITE){
            zombie.setItemSlot(EquipmentSlot.MAINHAND, createEnchantedSingleWeapon(Items.MACE, ArmorLevel.NETHERITE, random, 4, 4, enchantmentRegistry));
            zombie.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.WIND_CHARGE, 8));
        }
    }

    public static ItemStack createEnchantedSingleWeapon(Item item, ArmorLevel armorLevel, RandomSource random, int maxLevel, int totalLevel, Registry<Enchantment> enchantmentRegistry) {
        List<ResourceKey<Enchantment>> availableEnchantments = switch (armorLevel){
            case IRON -> new ArrayList<>(IRON_WEAPON_COMMON_ENCHANTMENTS);
            case DIAMOND -> new ArrayList<>(DIAMOND_WEAPON_COMMON_ENCHANTMENTS);
            case NETHERITE -> new ArrayList<>(NETHERITE_WEAPON_COMMON_ENCHANTMENTS);
        };

        ItemStack enchantedItem = randomEnchant(item, random, maxLevel, totalLevel, availableEnchantments, enchantmentRegistry);

        if (armorLevel == ArmorLevel.NETHERITE){
            enchantedItem.enchant(enchantmentRegistry.getOrThrow(NETHERITE_MACE_EXTRA_ENCHANTMENTS.get(random.nextInt(2))), random.nextInt(1,3));
        }
        return enchantedItem;
    }


    public static void createEnchantedArmor(Zombie zombie, ArmorLevel armorLevel, Registry<Enchantment> enchantmentRegistry) {
        RandomSource random = zombie.getRandom();
        if (armorLevel == ArmorLevel.IRON) {
            zombie.setItemSlot(EquipmentSlot.HEAD, createEnchantedSingleArmor(Items.IRON_HELMET, ArmorLevel.IRON, random, 2, 2, enchantmentRegistry));
            zombie.setItemSlot(EquipmentSlot.CHEST, createEnchantedSingleArmor(Items.IRON_CHESTPLATE, ArmorLevel.IRON, random, 2, 2, enchantmentRegistry));
            zombie.setItemSlot(EquipmentSlot.LEGS, createEnchantedSingleArmor(Items.IRON_LEGGINGS, ArmorLevel.IRON, random, 2, 2, enchantmentRegistry));
            zombie.setItemSlot(EquipmentSlot.FEET, createEnchantedSingleArmor(Items.IRON_BOOTS, ArmorLevel.IRON, random, 2, 2, enchantmentRegistry));
        }
        else if (armorLevel == ArmorLevel.DIAMOND) {
            zombie.setItemSlot(EquipmentSlot.HEAD, createEnchantedSingleArmor(Items.DIAMOND_HELMET, ArmorLevel.DIAMOND, random, 3, 3, enchantmentRegistry));
            zombie.setItemSlot(EquipmentSlot.CHEST, createEnchantedSingleArmor(Items.DIAMOND_CHESTPLATE, ArmorLevel.DIAMOND, random, 3, 3, enchantmentRegistry));
            zombie.setItemSlot(EquipmentSlot.LEGS, createEnchantedSingleArmor(Items.DIAMOND_LEGGINGS, ArmorLevel.DIAMOND, random, 3, 3, enchantmentRegistry));
            zombie.setItemSlot(EquipmentSlot.FEET, createEnchantedSingleArmor(Items.DIAMOND_BOOTS, ArmorLevel.DIAMOND, random, 3, 3, enchantmentRegistry));
        }
        else if (armorLevel == ArmorLevel.NETHERITE) {
            zombie.setItemSlot(EquipmentSlot.HEAD, createEnchantedSingleArmor(Items.NETHERITE_HELMET, ArmorLevel.NETHERITE, random, 4, 4, enchantmentRegistry));
            zombie.setItemSlot(EquipmentSlot.CHEST, createEnchantedSingleArmor(Items.NETHERITE_CHESTPLATE, ArmorLevel.NETHERITE, random, 4, 4, enchantmentRegistry));
            zombie.setItemSlot(EquipmentSlot.LEGS, createEnchantedSingleArmor(Items.NETHERITE_LEGGINGS, ArmorLevel.NETHERITE, random, 4, 4, enchantmentRegistry));
            zombie.setItemSlot(EquipmentSlot.FEET, createEnchantedSingleArmor(Items.NETHERITE_BOOTS, ArmorLevel.NETHERITE, random, 4, 4, enchantmentRegistry));
        }
    }

    public static ItemStack createEnchantedSingleArmor(Item item, ArmorLevel armorLevel, RandomSource random, int maxLevel, int totalLevel, Registry<Enchantment> enchantmentRegistry){
        List<ResourceKey<Enchantment>> availableEnchantments = switch (armorLevel) {
            case IRON -> new ArrayList<>(IRON_ARMOR_COMMON_ENCHANTMENTS);
            case DIAMOND -> new ArrayList<>(DIAMOND_ARMOR_COMMON_ENCHANTMENTS);
            case NETHERITE -> new ArrayList<>(NETHERITE_ARMOR_COMMON_ENCHANTMENTS);
        };

        ItemStack enchantedItem = randomEnchant(item, random, maxLevel, totalLevel, availableEnchantments, enchantmentRegistry);

        // Extra enchantment for boots:
        if (item == Items.IRON_BOOTS)
            enchantedItem.enchant(enchantmentRegistry.getOrThrow(Enchantments.FEATHER_FALLING), 2);
        else if (item == Items.DIAMOND_BOOTS)
            enchantedItem.enchant(enchantmentRegistry.getOrThrow(Enchantments.FEATHER_FALLING), 3);
        else if (item == Items.NETHERITE_BOOTS){
            enchantedItem.enchant(enchantmentRegistry.getOrThrow(Enchantments.FEATHER_FALLING), 4);

            int level = random.nextInt(2) + 1;
            enchantedItem.enchant(enchantmentRegistry.getOrThrow(Enchantments.SOUL_SPEED), level);
            enchantedItem.enchant(enchantmentRegistry.getOrThrow(Enchantments.DEPTH_STRIDER), 3-level);
        }
        return enchantedItem;
    }


    // Create an enchanted item, choose enchantments from availableEnchantments, 0 < total level <= totalLevel, 0 < max level <= maxLevel
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
                    enchantmentLevel = 0;
                    break;
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

    private static boolean isHandsEmpty(Zombie zombie){
        return zombie.getItemBySlot(EquipmentSlot.MAINHAND) == ItemStack.EMPTY
                && zombie.getItemBySlot(EquipmentSlot.OFFHAND) == ItemStack.EMPTY;
    }


    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide())
            return;

        if (!(event.getEntity() instanceof Zombie zombie))
            return;

        if (zombie.isBaby() && zombie.getVehicle() instanceof Chicken)
            return;

        if (!isHandsEmpty(zombie))
            return;

        RandomSource random = zombie.getRandom();

        if (random.nextFloat() < EQUIP_MACE_CHANCE) {
            zombie.setLeftHanded(false);
            ArmorLevel armorLevel = ArmorLevel.IRON;

            Registry<Enchantment> enchantmentRegistry = event.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

            if (random.nextFloat() < EQUIP_NETHERITE_CHANCE) {
                createEnchantedWeapon(zombie, ArmorLevel.NETHERITE, enchantmentRegistry);
                createEnchantedArmor(zombie, ArmorLevel.NETHERITE, enchantmentRegistry);
                armorLevel = ArmorLevel.NETHERITE;
            }
            else if (random.nextFloat() < EQUIP_DIAMOND_CHANCE) {
                createEnchantedWeapon(zombie, ArmorLevel.DIAMOND, enchantmentRegistry);
                createEnchantedArmor(zombie, ArmorLevel.DIAMOND, enchantmentRegistry);
                armorLevel = ArmorLevel.DIAMOND;
            }
            else if (random.nextFloat() < EQUIP_IRON_CHANCE) {
                createEnchantedWeapon(zombie, ArmorLevel.IRON, enchantmentRegistry);
                createEnchantedArmor(zombie, ArmorLevel.IRON, enchantmentRegistry);
            } else {
                createEnchantedWeapon(zombie, ArmorLevel.IRON, enchantmentRegistry);
            }

            zombie.getPersistentData().putBoolean(ENHANCED_ZOMBIE_BOOLEAN, true);

            zombie.goalSelector.addGoal(1, new ZombieAerialMaceAttackGoal(zombie, armorLevel, 1.1, false));
        }
    }
}
