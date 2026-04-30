package net.lostpatrol.mobstactician.event.drops;

import net.lostpatrol.mobstactician.MobsTactician;
import net.lostpatrol.mobstactician.util.Constants;
import net.lostpatrol.mobstactician.util.Util;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = MobsTactician.MODID)
public class PhantomDropsHandler {

    @SubscribeEvent
    public static void onPhantomDrops(LivingDropsEvent event) {
        if (event.getEntity().level().isClientSide())
            return;

        if (!(event.getEntity() instanceof Phantom phantom))
            return;

        if(! phantom.getPersistentData().getBoolean(Constants.ENHANCED_PHANTOM_BOOLEAN).orElse(false))
            return;

        RandomSource random = phantom.getRandom();

        // Firework rocket drop
        ItemStack fireworkRocket = phantom.getItemBySlot(EquipmentSlot.OFFHAND).copy();
        if (!fireworkRocket.isEmpty() && fireworkRocket.getItem() == Items.FIREWORK_ROCKET) {
            fireworkRocket.setCount(Math.max(1, fireworkRocket.getCount() - 1));
            event.getDrops().add(new ItemEntity(phantom.level(), phantom.getX(), phantom.getY(), phantom.getZ(), fireworkRocket));
        }

        // Weapon drop
        if (!Util.isItemInDrops(event.getDrops(), Items.NETHERITE_SPEAR) && random.nextFloat() < 0.4f) {
            ItemStack weaponDrop = phantom.getItemBySlot(EquipmentSlot.MAINHAND).copy();
            event.getDrops().add(new ItemEntity(phantom.level(), phantom.getX(), phantom.getY(), phantom.getZ(), Util.applyRandomDamage(weaponDrop, random, 0.2f, 0.75f)));
        }
    }
}
