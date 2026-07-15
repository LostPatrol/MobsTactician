package net.lostpatrol.mobstactician.util;

import net.lostpatrol.mobstactician.MobsTactician;

public class Constants {
    public static final String ENHANCED_ZOMBIE_BOOLEAN = MobsTactician.MODID + ".enhanced_zombie";
    public static final String ENHANCED_PHANTOM_BOOLEAN = MobsTactician.MODID + ".enhanced_phantom";
    public static final String ENHANCED_SKELETON_BOOLEAN = MobsTactician.MODID + ".enhanced_skeleton";
    /** Remaining defensive block count for tactical skeleton. */
    public static final String SKELETON_BLOCK_COUNT = MobsTactician.MODID + ".block_count";
    /** Defense block item id string used by block-defense goal. */
    public static final String SKELETON_DEFENSE_BLOCK_ITEM_ID = MobsTactician.MODID + ".defense_block_item_id";
    /** Backup main-hand weapon for bow/sword swapping. */
    public static final String SKELETON_STORED_MAINHAND_WEAPON = MobsTactician.MODID + ".stored_mainhand_weapon";
    /** Last successful weapon switch game tick. */
    public static final String SKELETON_LAST_WEAPON_SWITCH_TICK = MobsTactician.MODID + ".last_weapon_switch_tick";
    /** Start tick of current melee "not yet hit target" window. */
    public static final String SKELETON_MELEE_NO_HIT_SINCE_TICK = MobsTactician.MODID + ".melee_no_hit_since_tick";
    /** UUID of target tracked in current melee "not yet hit" window. */
    public static final String SKELETON_MELEE_NO_HIT_TARGET_UUID = MobsTactician.MODID + ".melee_no_hit_target_uuid";
    /** Target hurt timestamp baseline captured when melee no-hit window starts. */
    public static final String SKELETON_MELEE_NO_HIT_BASE_TARGET_HURT_TIMESTAMP = MobsTactician.MODID + ".melee_no_hit_base_target_hurt_timestamp";
    /** Last processed skeleton hurt timestamp to avoid duplicate hit sampling. */
    public static final String SKELETON_LAST_HURT_BY_MOB_TIMESTAMP = MobsTactician.MODID + ".last_hurt_by_mob_timestamp";
    /** Recent valid incoming-hit streak count for combo tactic. */
    public static final String SKELETON_RECENT_HIT_STREAK = MobsTactician.MODID + ".recent_hit_streak";
    /** Last accepted incoming-hit game tick for combo tactic. */
    public static final String SKELETON_LAST_HIT_GAME_TICK = MobsTactician.MODID + ".last_hit_game_tick";
    /** Forced-ranged mode expiration tick. */
    public static final String SKELETON_FORCE_RANGED_UNTIL_TICK = MobsTactician.MODID + ".force_ranged_until_tick";
    public static final String IRON_GOLEM_LAST_SELF_REPAIR_TICK = MobsTactician.MODID + ".iron_golem_last_self_repair_tick";
    public static final String IRON_GOLEM_FIRST_SELF_REPAIR_DELAY = MobsTactician.MODID + ".iron_golem_first_self_repair_delay";
    public static final String IRON_GOLEM_SELF_REPAIR_FINISH_TICK = MobsTactician.MODID + ".iron_golem_self_repair_finish_tick";
    public static final String IRON_GOLEM_SELF_REPAIR_ACTIVE = MobsTactician.MODID + ".iron_golem_self_repair_active";
    public static final String IRON_GOLEM_SELF_REPAIR_ORIGINAL_DROP_CHANCE = MobsTactician.MODID + ".iron_golem_self_repair_original_drop_chance";

    public enum ArmorLevel {
        IRON,
        DIAMOND,
        NETHERITE
    }
}
