package net.lostpatrol.mobspvpmaster.event.drops;

import net.lostpatrol.mobspvpmaster.MobsPVPMaster;
import net.lostpatrol.mobspvpmaster.util.Constants;
import net.lostpatrol.mobspvpmaster.util.Util;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = MobsPVPMaster.MODID)
public class ZombieDropsHandler {
    // Enhanced Zombie Should Drop More
    @SubscribeEvent
    public static void onEnhancedZombieDrops(LivingDropsEvent event) {
        if (event.getEntity().level().isClientSide())
            return;

        if (!(event.getEntity() instanceof Zombie zombie))
            return;

        if(! zombie.getPersistentData().getBoolean(Constants.ENHANCED_ZOMBIE_BOOLEAN).orElse(false))
            return;

        RandomSource random = zombie.getRandom();

        // Wind charge drop
        ItemStack windChargeStack = zombie.getItemBySlot(EquipmentSlot.OFFHAND).copy();
        if (!windChargeStack.isEmpty() && windChargeStack.getItem() == Items.WIND_CHARGE) {
            windChargeStack.setCount(Math.max(1, windChargeStack.getCount() - 1));
            event.getDrops().add(new ItemEntity(zombie.level(), zombie.getX(), zombie.getY(), zombie.getZ(), windChargeStack));
        }

        // Mace drop
        if (!Util.isItemInDrops(event.getDrops(), Items.MACE) && random.nextFloat() < 0.4f) {
            ItemStack maceDrop = zombie.getItemBySlot(EquipmentSlot.MAINHAND).copy();
            event.getDrops().add(new ItemEntity(zombie.level(), zombie.getX(), zombie.getY(), zombie.getZ(), Util.applyRandomDamage(maceDrop, random, 0.2F, 0.65F)));
        }

        // Armors drop
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack armorStack = zombie.getItemBySlot(slot);
            if (armorStack.isEmpty())
                continue;
            if (Util.isItemInDrops(event.getDrops(), armorStack.getItem()))
                continue;
            if (random.nextFloat() < 0.2f) {
                ItemStack armorDrop = armorStack.copy();
                event.getDrops().add(new ItemEntity(zombie.level(), zombie.getX(), zombie.getY(), zombie.getZ(), Util.applyRandomDamage(armorDrop, random, 0.2F, 0.65F)));
            }
        }
    }
}
