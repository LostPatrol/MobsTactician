package net.lostpatrol.mobspvpmaster.util;

import net.lostpatrol.mobspvpmaster.MobsPVPMaster;

public class Constants {
    public static final String ENHANCED_ZOMBIE_BOOLEAN = MobsPVPMaster.MODID + ".enhanced_zombie";
    public static final String ENHANCED_PHANTOM_BOOLEAN = MobsPVPMaster.MODID + ".enhanced_phantom";
    public static final String ENHANCED_SKELETON_BOOLEAN = MobsPVPMaster.MODID + ".enhanced_skeleton";
    public static final String SKELETON_BLOCK_COUNT = MobsPVPMaster.MODID + ".block_count";
    public static final String SKELETON_DEFENSE_BLOCK_ITEM_ID = MobsPVPMaster.MODID + ".defense_block_item_id";
    public static final String SKELETON_STORED_MAINHAND_WEAPON = MobsPVPMaster.MODID + ".stored_mainhand_weapon";
    public static final String SKELETON_LAST_WEAPON_SWITCH_TICK = MobsPVPMaster.MODID + ".last_weapon_switch_tick";

    public enum ArmorLevel {
        IRON,
        DIAMOND,
        NETHERITE
    }
}
