package net.lostpatrol.mobspvpmaster.entity.ai.skeleton;

import net.lostpatrol.mobspvpmaster.MobsPVPMaster;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import org.slf4j.Logger;

public class SkeletonPeekShootGoal {
    public static final Logger logger = MobsPVPMaster.LOGGER;

    private final Skeleton skeleton;



    public SkeletonPeekShootGoal(Skeleton skeleton) {
        this.skeleton = skeleton;
    }
}
