package net.lostpatrol.mobstactician.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.lostpatrol.mobstactician.MobsTactician;
import net.lostpatrol.mobstactician.config.Config;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Locale;

@EventBusSubscriber(modid = MobsTactician.MODID)
public final class MobsTacticianCommand {
    private MobsTacticianCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("mobstactician")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(context -> show(context.getSource()))
                        .then(Commands.literal("debugMode")
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(context -> setDebugMode(
                                                context.getSource(), BoolArgumentType.getBool(context, "enabled")
                                        ))))
                        .then(chanceCommand("zombieChance", Config.ZOMBIE_TACTICAL_CHANCE))
                        .then(chanceCommand("phantomChance", Config.PHANTOM_TACTICAL_CHANCE))
                        .then(chanceCommand("phantomSpearChance", Config.PHANTOM_SPEAR_CHANCE))
                        .then(chanceCommand("phantomCarrierChance", Config.PHANTOM_CARRIER_CHANCE))
                        .then(chanceCommand("skeletonChance", Config.SKELETON_TACTICAL_CHANCE))
                        .then(chanceCommand("creeperChance", Config.CREEPER_TACTICAL_CHANCE))
                        .then(chanceCommand("ironGolemChance", Config.IRON_GOLEM_TACTICAL_CHANCE))
                        .then(chanceCommand("witchChance", Config.WITCH_TACTICAL_CHANCE))
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> chanceCommand(
            String name, ModConfigSpec.DoubleValue configValue
    ) {
        return Commands.literal(name)
                .then(Commands.argument("chance", DoubleArgumentType.doubleArg(0.0, 1.0))
                        .executes(context -> setChance(
                                context.getSource(),
                                name,
                                configValue,
                                DoubleArgumentType.getDouble(context, "chance")
                        )));
    }

    private static int setDebugMode(CommandSourceStack source, boolean enabled) {
        Config.DEBUG_MODE.set(enabled);
        Config.SPEC.save();
        source.sendSuccess(() -> Component.literal("Mobs Tactician debugMode = " + enabled), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int setChance(
            CommandSourceStack source, String name, ModConfigSpec.DoubleValue configValue, double chance
    ) {
        configValue.set(chance);
        Config.SPEC.save();
        source.sendSuccess(
                () -> Component.literal("Mobs Tactician " + name + " = " + format(chance)),
                true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int show(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal(
                "Mobs Tactician: debugMode=" + Config.DEBUG_MODE.get()
                        + ", zombieChance=" + format(Config.ZOMBIE_TACTICAL_CHANCE.get())
                        + ", phantomChance=" + format(Config.PHANTOM_TACTICAL_CHANCE.get())
                        + ", phantomSpearChance=" + format(Config.PHANTOM_SPEAR_CHANCE.get())
                        + ", phantomCarrierChance=" + format(Config.PHANTOM_CARRIER_CHANCE.get())
                        + ", skeletonChance=" + format(Config.SKELETON_TACTICAL_CHANCE.get())
                        + ", creeperChance=" + format(Config.CREEPER_TACTICAL_CHANCE.get())
                        + ", ironGolemChance=" + format(Config.IRON_GOLEM_TACTICAL_CHANCE.get())
                        + ", witchChance=" + format(Config.WITCH_TACTICAL_CHANCE.get())
        ), false);
        return Command.SINGLE_SUCCESS;
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }
}
