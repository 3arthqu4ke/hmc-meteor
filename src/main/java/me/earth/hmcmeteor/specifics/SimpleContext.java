package me.earth.hmcmeteor.specifics;

import me.earth.headlessmc.api.HeadlessMc;
import me.earth.headlessmc.api.command.AbstractCommand;
import me.earth.headlessmc.api.command.CommandUtil;
import me.earth.headlessmc.runtime.commands.RuntimeContext;
import me.earth.headlessmc.runtime.commands.RuntimeQuitCommand;
import net.minecraft.client.MinecraftClient;

public class SimpleContext extends RuntimeContext {
    public SimpleContext(HeadlessMc ctx) {
        super(ctx);
        commands.removeIf(command -> command instanceof RuntimeQuitCommand);
        commands.add(new HmcSpecificsMeteorCommand(ctx));
        commands.add(new AbstractCommand(ctx, "quit", "Quits the game.") {
            @Override
            public void execute(String line, String... args) {
                if (CommandUtil.hasFlag("-force", args)) {
                    ctx.log("Forcing Minecraft to Exit!");
                    System.exit(0);
                    return;
                }

                MinecraftClient mc = MinecraftClient.getInstance();
                if (CommandUtil.hasFlag("-s", args)) {
                    ctx.log("Quitting Minecraft...");
                    mc.stop();
                } else {
                    mc.execute(() -> {
                        ctx.log("Quitting Minecraft...");
                        mc.stop();
                    });
                }
            }

            @Override
            public boolean matches(String line, String... args) {
                if (args.length > 0) {
                    String lower = args[0].toLowerCase();
                    return lower.equalsIgnoreCase("quit")
                        || lower.equalsIgnoreCase("exit")
                        || lower.equalsIgnoreCase("stop");
                }

                return false;
            }
        });
    }

}
