package net.lostpatrol.mobspvpmaster.event.equips;

import net.lostpatrol.mobspvpmaster.util.Constants;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PhantomEquipHandler {
    private static final float ENHANCED_PHANTOM_CHANCE = 0.9f;

    public static void setupEquipmentIfNeeded(Phantom phantom, int difficulty) {
        if (phantom.getPersistentData().contains(Constants.ENHANCED_PHANTOM_BOOLEAN)) {
            return;
        }

        if (phantom.getRandom().nextFloat() < ENHANCED_PHANTOM_CHANCE) {
            setupEnhancedPhantom(phantom, difficulty);
        } else {
            phantom.getPersistentData().putBoolean(Constants.ENHANCED_PHANTOM_BOOLEAN, false);
        }
    }

    public static boolean isEnhancedPhantom(Phantom phantom) {
        return phantom.getPersistentData().getBoolean(Constants.ENHANCED_PHANTOM_BOOLEAN).orElse(false);
    }

    private static void setupEnhancedPhantom(Phantom phantom, int difficulty) {
        phantom.getPersistentData().putBoolean(Constants.ENHANCED_PHANTOM_BOOLEAN, true);

        ItemStack spear = new ItemStack(Items.NETHERITE_SPEAR);
        phantom.setItemSlot(EquipmentSlot.MAINHAND, spear);
        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET, 3 + difficulty);
        phantom.setItemSlot(EquipmentSlot.OFFHAND, rocket);
    }
}
