package net.lostpatrol.mobspvpmaster.event.drops;

import net.lostpatrol.mobspvpmaster.MobsPVPMaster;
import net.lostpatrol.mobspvpmaster.util.Constants;
import net.lostpatrol.mobspvpmaster.util.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = MobsPVPMaster.MODID)
public class SkeletonDropsHandler {
    @SubscribeEvent
    public static void onEnhancedSkeletonDrops(LivingDropsEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (!(event.getEntity() instanceof Skeleton skeleton)) {
            return;
        }

        if (!skeleton.getPersistentData().getBoolean(Constants.ENHANCED_SKELETON_BOOLEAN).orElse(false)) {
            return;
        }

        RandomSource random = skeleton.getRandom();

        int remainingBlocks = skeleton.getPersistentData().getInt(Constants.SKELETON_BLOCK_COUNT).orElse(0);
        if (remainingBlocks > 0 && random.nextFloat() < 0.5f) {
            int dropCount = Math.min(32, Math.max(4, remainingBlocks / 2));
            ItemStack blocks = new ItemStack(resolveDefenseBlockItem(skeleton), dropCount);
            event.getDrops().add(new ItemEntity(skeleton.level(), skeleton.getX(), skeleton.getY(), skeleton.getZ(), blocks));
        }

        ItemStack mainHand = skeleton.getItemBySlot(EquipmentSlot.MAINHAND).copy();
        if (!mainHand.isEmpty() && !Util.isItemInDrops(event.getDrops(), mainHand.getItem()) && random.nextFloat() < 0.45f) {
            ItemStack drop = Util.applyRandomDamage(mainHand, random, 0.2f, 0.65f);
            event.getDrops().add(new ItemEntity(skeleton.level(), skeleton.getX(), skeleton.getY(), skeleton.getZ(), drop));
        }

        var registryOps = skeleton.level().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        ItemStack storedWeapon = skeleton.getPersistentData().read(Constants.SKELETON_STORED_MAINHAND_WEAPON, ItemStack.CODEC, registryOps).orElse(ItemStack.EMPTY);
        if (!storedWeapon.isEmpty() && !Util.isItemInDrops(event.getDrops(), storedWeapon.getItem()) && random.nextFloat() < 0.35f) {
            ItemStack drop = Util.applyRandomDamage(storedWeapon.copy(), random, 0.2f, 0.65f);
            event.getDrops().add(new ItemEntity(skeleton.level(), skeleton.getX(), skeleton.getY(), skeleton.getZ(), drop));
        }

        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack armor = skeleton.getItemBySlot(slot);
            if (armor.isEmpty() || Util.isItemInDrops(event.getDrops(), armor.getItem())) {
                continue;
            }
            if (random.nextFloat() < 0.22f) {
                ItemStack drop = Util.applyRandomDamage(armor.copy(), random, 0.2f, 0.7f);
                event.getDrops().add(new ItemEntity(skeleton.level(), skeleton.getX(), skeleton.getY(), skeleton.getZ(), drop));
            }
        }
    }

    private static Item resolveDefenseBlockItem(Skeleton skeleton) {
        String itemId = skeleton.getPersistentData().getString(Constants.SKELETON_DEFENSE_BLOCK_ITEM_ID).orElse("minecraft:cobblestone");
        Identifier identifier = Identifier.tryParse(itemId);
        if (identifier == null) {
            return Items.COBBLESTONE;
        }

        Item item = BuiltInRegistries.ITEM.getValue(identifier);
        return item == null ? Items.COBBLESTONE : item;
    }
}
