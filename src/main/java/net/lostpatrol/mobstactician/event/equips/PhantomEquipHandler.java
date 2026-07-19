package net.lostpatrol.mobstactician.event.equips;

import net.lostpatrol.mobstactician.config.Config;
import net.lostpatrol.mobstactician.util.Constants;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class PhantomEquipHandler {
    private PhantomEquipHandler() {
    }

    public static void setupEquipmentIfNeeded(Phantom phantom, int difficulty) {
        if (phantom.getPersistentData().contains(Constants.ENHANCED_PHANTOM_BOOLEAN)) {
            migrateSubtypeData(phantom);
            return;
        }

        boolean tactical = Config.roll(Config.PHANTOM_TACTICAL_CHANCE.get(), phantom.getRandom());
        phantom.getPersistentData().putBoolean(Constants.ENHANCED_PHANTOM_BOOLEAN, tactical);
        if (!tactical) {
            setSubtypes(phantom, false, false);
            return;
        }

        boolean spear;
        boolean carrier;
        if (Config.DEBUG_MODE.get()) {
            spear = true;
            carrier = true;
        } else {
            double spearShare = Config.PHANTOM_SPEAR_CHANCE.get();
            double carrierShare = Config.PHANTOM_CARRIER_CHANCE.get();
            double totalShare = spearShare + carrierShare;
            spear = totalShare > 0.0 && phantom.getRandom().nextDouble() * totalShare < spearShare;
            carrier = totalShare > 0.0 && !spear;
        }
        setSubtypes(phantom, spear, carrier);
        if (spear) {
            setupSpearLoadout(phantom, difficulty);
        }
    }

    public static boolean isEnhancedPhantom(Phantom phantom) {
        return phantom.getPersistentData().getBoolean(Constants.ENHANCED_PHANTOM_BOOLEAN).orElse(false);
    }

    public static boolean isSpearPhantom(Phantom phantom) {
        return isEnhancedPhantom(phantom)
                && phantom.getPersistentData().getBoolean(Constants.PHANTOM_SPEAR_BOOLEAN).orElse(false);
    }

    public static boolean isCarrierPhantom(Phantom phantom) {
        return isEnhancedPhantom(phantom)
                && phantom.getPersistentData().getBoolean(Constants.PHANTOM_CARRIER_BOOLEAN).orElse(false);
    }

    private static void migrateSubtypeData(Phantom phantom) {
        if (phantom.getPersistentData().contains(Constants.PHANTOM_SPEAR_BOOLEAN)
                && phantom.getPersistentData().contains(Constants.PHANTOM_CARRIER_BOOLEAN)) {
            return;
        }

        boolean enhanced = isEnhancedPhantom(phantom);
        setSubtypes(phantom, enhanced, false);
    }

    private static void setSubtypes(Phantom phantom, boolean spear, boolean carrier) {
        phantom.getPersistentData().putBoolean(Constants.PHANTOM_SPEAR_BOOLEAN, spear);
        phantom.getPersistentData().putBoolean(Constants.PHANTOM_CARRIER_BOOLEAN, carrier);
    }

    private static void setupSpearLoadout(Phantom phantom, int difficulty) {
        phantom.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SPEAR));
        phantom.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.FIREWORK_ROCKET, 3 + difficulty));
    }
}
