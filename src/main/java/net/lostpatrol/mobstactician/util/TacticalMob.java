package net.lostpatrol.mobstactician.util;

import net.lostpatrol.mobstactician.config.Config;
import net.minecraft.world.entity.Mob;

public final class TacticalMob {
    private TacticalMob() {
    }

    public static void initialize(Mob mob, String key, double chance) {
        if (!mob.getPersistentData().contains(key)) {
            mob.getPersistentData().putBoolean(key, Config.roll(chance, mob.getRandom()));
        }
    }

    public static boolean isTactical(Mob mob, String key) {
        return mob.getPersistentData().getBoolean(key).orElse(false);
    }
}
