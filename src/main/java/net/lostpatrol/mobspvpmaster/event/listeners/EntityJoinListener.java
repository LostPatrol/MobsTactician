package net.lostpatrol.mobspvpmaster.event.listeners;

import net.lostpatrol.mobspvpmaster.MobsPVPMaster;
import net.lostpatrol.mobspvpmaster.event.equips.PhantomEquipHandler;
import net.lostpatrol.mobspvpmaster.event.equips.SkeletonEquipHandler;
import net.lostpatrol.mobspvpmaster.event.equips.ZombieEquipHandler;
import net.lostpatrol.mobspvpmaster.event.goals.CreeperGoalHandler;
import net.lostpatrol.mobspvpmaster.event.goals.PhantomGoalHandler;
import net.lostpatrol.mobspvpmaster.event.goals.SkeletonGoalHandler;
import net.lostpatrol.mobspvpmaster.event.goals.ZombieGoalHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = MobsPVPMaster.MODID)
public class EntityJoinListener {
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
        }
    }
}
