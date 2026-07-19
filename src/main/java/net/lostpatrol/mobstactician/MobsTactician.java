package net.lostpatrol.mobstactician;

import com.mojang.logging.LogUtils;
import net.lostpatrol.mobstactician.config.Config;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(MobsTactician.MODID)
public class MobsTactician {
    public static final String MODID = "mobstactician";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MobsTactician(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }
}
