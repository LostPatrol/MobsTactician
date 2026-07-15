package net.lostpatrol.mobstactician.event.listeners;

import net.lostpatrol.mobstactician.MobsTactician;
import net.lostpatrol.mobstactician.event.equips.PhantomEquipHandler;
import net.lostpatrol.mobstactician.event.equips.SkeletonEquipHandler;
import net.lostpatrol.mobstactician.event.equips.ZombieEquipHandler;
import net.lostpatrol.mobstactician.event.goals.CreeperGoalHandler;
import net.lostpatrol.mobstactician.event.goals.IronGolemGoalHandler;
import net.lostpatrol.mobstactician.event.goals.PhantomGoalHandler;
import net.lostpatrol.mobstactician.event.goals.SkeletonGoalHandler;
import net.lostpatrol.mobstactician.event.goals.WitchGoalHandler;
import net.lostpatrol.mobstactician.event.goals.ZombieGoalHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = MobsTactician.MODID)
public final class EntityJoinListener {
    private EntityJoinListener() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (event.getEntity() instanceof Zombie zombie) {
            ZombieEquipHandler.setupEquipmentIfNeeded(zombie, event.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT));
            ZombieGoalHandler.ensureGoals(zombie);
            return;
        }

        if (event.getEntity() instanceof Skeleton skeleton) {
            SkeletonEquipHandler.setupEquipmentIfNeeded(skeleton, event.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT));
            SkeletonGoalHandler.onJoin(skeleton);
            return;
        }

        if (event.getEntity() instanceof Phantom phantom) {
            PhantomEquipHandler.setupEquipmentIfNeeded(phantom, event.getLevel().getDifficulty().ordinal());
            PhantomGoalHandler.ensureGoals(phantom);
            return;
        }

        if (event.getEntity() instanceof Creeper creeper) {
            CreeperGoalHandler.ensureGoals(creeper);
            return;
        }

        if (event.getEntity() instanceof IronGolem ironGolem) {
            IronGolemGoalHandler.ensureGoals(ironGolem);
            return;
        }

        if (event.getEntity() instanceof Witch witch) {
            WitchGoalHandler.ensureGoals(witch);
        }
    }
}
