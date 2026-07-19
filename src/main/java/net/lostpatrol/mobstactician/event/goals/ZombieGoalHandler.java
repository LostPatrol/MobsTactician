package net.lostpatrol.mobstactician.event.goals;

import net.lostpatrol.mobstactician.entity.ai.zombie.ZombieAerialMaceAttackGoal;
import net.lostpatrol.mobstactician.event.equips.ZombieEquipHandler;
import net.lostpatrol.mobstactician.util.Constants.ArmorLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ZombieGoalHandler {
    public static void ensureGoals(Zombie zombie) {
        if (!ZombieEquipHandler.isEnhancedZombie(zombie)) {
            return;
        }

        if (!hasAerialMaceGoal(zombie)) {
            ArmorLevel armorLevel = deduceArmorLevel(zombie);
            zombie.goalSelector.addGoal(1, new ZombieAerialMaceAttackGoal(zombie, armorLevel, 1.1, false));
        }
    }

    private static boolean hasAerialMaceGoal(Zombie zombie) {
        return zombie.goalSelector.getAvailableGoals().stream()
                .anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof ZombieAerialMaceAttackGoal);
    }

    private static ArmorLevel deduceArmorLevel(Zombie zombie) {
        Item helmet = zombie.getItemBySlot(EquipmentSlot.HEAD).getItem();
        if (helmet == Items.NETHERITE_HELMET) return ArmorLevel.NETHERITE;
        if (helmet == Items.DIAMOND_HELMET) return ArmorLevel.DIAMOND;
        return ArmorLevel.IRON;
    }
}
