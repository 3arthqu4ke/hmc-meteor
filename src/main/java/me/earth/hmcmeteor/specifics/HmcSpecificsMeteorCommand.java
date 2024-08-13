package me.earth.hmcmeteor.specifics;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.earth.headlessmc.api.HeadlessMc;
import me.earth.headlessmc.api.command.AbstractCommand;
import me.earth.headlessmc.mc.brigadier.BrigadierWrapper;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandSource;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;

public class HmcSpecificsMeteorCommand extends AbstractCommand {
    public HmcSpecificsMeteorCommand(HeadlessMc ctx) {
        super(ctx, "meteor", "Executes meteor commands.");
    }

    @Override
    public void execute(String line, String... args) {
        MinecraftClient.getInstance().execute(() -> {
            if (Config.get() == null) {
                ctx.log("Meteor currently not available.");
                return;
            }

            if (MinecraftClient.getInstance().player == null || MinecraftClient.getInstance().getNetworkHandler() == null) {
                ctx.log("You need to be in game to execute meteor commands.");
                return;
            }

            try {
                Commands.dispatch(line.substring("meteor ".length()));
            } catch (CommandSyntaxException e) {
                ChatUtils.error(e.getMessage());
            }
        });
    }

    @Override
    public String getDescription() {
        return "Executes meteor commands.";
    }

    @Override
    public void getCompletions(String line, List<Map.Entry<String, String>> completions, String... args) {
        if (line.toLowerCase(Locale.ENGLISH).startsWith("meteor ")) {
            ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
            if (networkHandler == null) {
                return;
            }

            List<Map.Entry<String, String>> mainThreadCompletions = new ArrayList<>();
            // schedule because we do not want to run into concurrency issues, e.g. when iterating over commands
            CompletableFuture<Void> scheduled = MinecraftClient.getInstance().submit(() -> {
                String unprefixed = line.substring("meteor ".length());
                if (unprefixed.isEmpty()) {
                    for (Command command : Commands.COMMANDS) {
                        mainThreadCompletions.add(new AbstractMap.SimpleEntry<>(command.getName(), command.getDescription()));
                    }

                    return;
                }

                CommandDispatcher<CommandSource> dispatcher = Commands.DISPATCHER;
                CommandSource suggestionsProvider = networkHandler.getCommandSource();
                Collection<String> customTabSugggestions = suggestionsProvider.getChatSuggestions();
                BiFunction<Collection<String>, SuggestionsBuilder, CompletableFuture<Suggestions>> suggestFunction = CommandSource::suggestMatching;
                mainThreadCompletions.addAll(BrigadierWrapper.getCompletions(dispatcher, suggestionsProvider, customTabSugggestions, suggestFunction,
                    "/" + unprefixed));// fake being a normal command, I overlooked this when writing the BrigadierSuggestionsProvider
            });

            try {
                scheduled.get(1, TimeUnit.SECONDS);
                completions.addAll(mainThreadCompletions);
            } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
                // failed to get completions in time
            }
        } else {
            super.getCompletions(line, completions, args);
        }
    }

    @Override
    public boolean matches(String line, String... args) {
        return line.toLowerCase(Locale.ENGLISH).startsWith("meteor");
    }

}
