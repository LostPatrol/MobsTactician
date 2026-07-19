package net.lostpatrol.mobstactician.config;

import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DEBUG_MODE = BUILDER
            .comment("Makes every eligible mob tactical. Phantom tactical subtype chances are also treated as 1.0.")
            .define("debugMode", false);

    public static final ModConfigSpec.DoubleValue ZOMBIE_TACTICAL_CHANCE = chance(
            "zombieTacticalChance", 0.2, "Chance for an eligible zombie to become tactical."
    );
    public static final ModConfigSpec.DoubleValue PHANTOM_TACTICAL_CHANCE = chance(
            "phantomTacticalChance", 0.4, "Chance for a phantom to become tactical."
    );
    public static final ModConfigSpec.DoubleValue PHANTOM_SPEAR_CHANCE = chance(
            "phantomSpearChance", 0.5, "Relative share of tactical phantoms using the spear and rocket tactic."
    );
    public static final ModConfigSpec.DoubleValue PHANTOM_CARRIER_CHANCE = chance(
            "phantomCarrierChance", 0.5, "Relative share of tactical phantoms using the mob carrier tactic."
    );
    public static final ModConfigSpec.DoubleValue SKELETON_TACTICAL_CHANCE = chance(
            "skeletonTacticalChance", 0.2, "Chance for an eligible skeleton to become tactical."
    );
    public static final ModConfigSpec.DoubleValue CREEPER_TACTICAL_CHANCE = chance(
            "creeperTacticalChance", 0.2, "Chance for a creeper to become tactical."
    );
    public static final ModConfigSpec.DoubleValue IRON_GOLEM_TACTICAL_CHANCE = chance(
            "ironGolemTacticalChance", 0.2, "Chance for an iron golem to become tactical."
    );
    public static final ModConfigSpec.DoubleValue WITCH_TACTICAL_CHANCE = chance(
            "witchTacticalChance", 0.2, "Chance for a witch to become tactical."
    );

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }

    private static ModConfigSpec.DoubleValue chance(String path, double defaultValue, String comment) {
        return BUILDER.comment(comment).defineInRange(path, defaultValue, 0.0, 1.0);
    }

    public static boolean roll(double chance, RandomSource random) {
        return DEBUG_MODE.get() || random.nextDouble() < chance;
    }
}
