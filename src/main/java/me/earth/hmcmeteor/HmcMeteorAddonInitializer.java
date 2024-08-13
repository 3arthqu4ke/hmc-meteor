package me.earth.hmcmeteor;

import com.mojang.logging.LogUtils;
import me.earth.headlessmc.api.HeadlessMc;
import me.earth.headlessmc.api.HeadlessMcApi;
import me.earth.headlessmc.api.HeadlessMcImpl;
import me.earth.headlessmc.api.classloading.ApiClassloadingHelper;
import me.earth.headlessmc.api.command.Command;
import me.earth.headlessmc.api.command.CommandContext;
import me.earth.headlessmc.api.command.CopyContext;
import me.earth.headlessmc.api.command.line.CommandLine;
import me.earth.headlessmc.api.config.ConfigImpl;
import me.earth.headlessmc.api.exit.ExitManager;
import me.earth.headlessmc.logging.LoggingService;
import me.earth.headlessmc.mc.commands.MinecraftContext;
import me.earth.hmcmeteor.specifics.HmcSpecificsMeteorCommand;
import me.earth.hmcmeteor.specifics.SimpleContext;
import org.slf4j.Logger;

public class HmcMeteorAddonInitializer {
    public static final Logger LOG = LogUtils.getLogger();

    public static void initialize() {
        HeadlessMc hmc = HeadlessMcApi.getInstance();
        if (hmc == null) {
            LOG.error("Failed to find HeadlessMc instance!");
            HeadlessMc headlessMc = new HeadlessMcImpl(ConfigImpl::empty, new CommandLine(), new ExitManager(), new LoggingService());
            headlessMc.getLoggingService().init();
            Object remote = ApiClassloadingHelper.installOnOtherInstances(headlessMc);
            if (remote != null) {
                LOG.info("Managed to install HeadlessMc on a remote instance!");
                SimpleContext commands = new SimpleContext(headlessMc);
                headlessMc.getCommandLine().setBaseContext(commands);
                headlessMc.getCommandLine().setCommandContext(commands);
            }

            return;
        }

        CommandContext context = hmc.getCommandLine().getBaseContext();
        for (Command command : context) {
            if (command instanceof HmcSpecificsMeteorCommand) {
                LOG.info("HMC-Meteor already installed.");
                return;
            }
        }

        if (context instanceof MinecraftContext) {
            LOG.info("Installing hmc-meteor command.");
            MinecraftContext minecraftContext = (MinecraftContext) context;
            minecraftContext.add(new HmcSpecificsMeteorCommand(hmc));
        } else {
            LOG.warn("Context not a MinecraftContext!");
            CopyContext commands = new CopyContext(hmc, true);
            commands.add(new HmcSpecificsMeteorCommand(hmc));
            hmc.getCommandLine().setBaseContext(commands);
            hmc.getCommandLine().setCommandContext(commands);
        }
    }

}
