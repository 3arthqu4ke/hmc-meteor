package me.earth.hmcmeteor.launcher;

import me.earth.headlessmc.api.command.CommandException;
import me.earth.headlessmc.api.command.CommandUtil;
import me.earth.headlessmc.launcher.Launcher;
import me.earth.headlessmc.launcher.command.AbstractLauncherCommand;
import me.earth.headlessmc.launcher.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.stream.Stream;

public class HmcMeteorDownloadCommand extends AbstractLauncherCommand {
    public HmcMeteorDownloadCommand(Launcher ctx) {
        super(ctx, "meteor", "Downloads the Meteor client.");
        args.put("-clear", "Delete versions of meteor in your mods folder.");
    }

    @Override
    public void execute(String line, String... args) throws CommandException {
        if (args.length < 2) {
            throw new CommandException("Please specify a version, e.g. 0.5.8");
        }

        Path modsFolder = ctx.getGameDir().getDir("mods").toPath();
        if (CommandUtil.hasFlag("-clear", args)) {
            ctx.log("Clearing meteor versions from " + modsFolder);
            boolean found = false;
            if (Files.exists(modsFolder)) {
                try (Stream<Path> stream = Files.list(modsFolder)) {
                    Iterator<Path> itr = stream.iterator();
                    while (itr.hasNext()) {
                        Path modFile = itr.next();
                        String modFilename = modFile.getFileName().toString();
                        if (modFilename.toLowerCase(Locale.ENGLISH).endsWith(".jar")
                            && modFilename.toLowerCase(Locale.ENGLISH).startsWith("meteor-client")) {
                            ctx.log("Deleting " + modFilename);
                            found = true;
                            Files.delete(modFile);
                        }
                    }
                } catch (IOException e) {
                    throw new CommandException("Failed to clear meteor versions from " + modsFolder + ": " + e.getMessage(), e);
                }
            }

            if (!found) {
                ctx.log("No meteor mod files found.");
            }

            return;
        }

        String version = args[1];
        /* TODO: read maven-metadata to get latest and snapshots
        if (snapshot && !version.endsWith("-SNAPSHOT")) {
            version += "-SNAPSHOT";
        }         */

        String filename = "meteor-client-" + version + ".jar";
        String url = "https://maven.meteordev.org/"
            + (/*snapshot ? "snapshots" : */"releases")
            + "/meteordevelopment/meteor-client/"
            + version + "/" + filename;

        try {
            File modsDir = ctx.getGameDir().getDir("mods");
            ctx.log("Downloading meteor " + version + " from " + url + " to " + modsDir);
            IOUtil.download(url, modsDir.toPath().resolve(filename).toAbsolutePath().toString());
            ctx.log("Download successful.");
        } catch (IOException e) {
            throw new CommandException("Failed to download meteor: " + e.getMessage());
        }
    }

}
