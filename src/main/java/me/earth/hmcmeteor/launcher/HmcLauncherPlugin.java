package me.earth.hmcmeteor.launcher;

import me.earth.headlessmc.api.command.CopyContext;
import me.earth.headlessmc.launcher.Launcher;
import me.earth.headlessmc.launcher.plugin.HeadlessMcPlugin;

public class HmcLauncherPlugin implements HeadlessMcPlugin {
    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void init(Launcher launcher) {
        CopyContext commands = new CopyContext(launcher, true);
        commands.add(new HmcMeteorDownloadCommand(launcher));
        launcher.getCommandLine().setCommandContext(commands);
        launcher.getCommandLine().setBaseContext(commands);
    }

    @Override
    public String getName() {
        return "meteor";
    }

    @Override
    public String getDescription() {
        return "Adds a command for downloading meteor.";
    }

}
