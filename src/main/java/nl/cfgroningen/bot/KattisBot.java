package nl.cfgroningen.bot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.interaction.ApplicationCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import nl.cfgroningen.command.ContributeCommand;
import nl.cfgroningen.command.GenericCommand;
import nl.cfgroningen.command.LinkCommand;
import nl.cfgroningen.command.UniversityCommand;
import nl.cfgroningen.database.BotData;
import nl.cfgroningen.kattis.KattisApi;

@Log
@RequiredArgsConstructor
public class KattisBot {
    @NonNull
    private String token;

    @Getter
    private DiscordApi client;

    @Getter
    private BotData data;

    @Getter
    private KattisApi kattisApi;

    private KattisDataManager dataManager;

    @NonNull
    @Getter
    private String owningUniversityUrl;

    public void initialize() {
        this.data = BotData.load();
        this.kattisApi = new KattisApi();

        this.dataManager = new KattisDataManager(this);

        // Create a new instance of the bot
        new DiscordApiBuilder()
                .setToken(token)
                .addIntents(Intent.GUILD_MEMBERS)
                .login().thenAcceptAsync((client) -> {
                    this.client = client;

                    this.registerCommand(new UniversityCommand(this, dataManager));
                    this.registerCommand(new LinkCommand(this));
                    this.registerCommand(new ContributeCommand(this, dataManager));

                    this.registerAllCommands();
                });
    }

    private void registerAllCommands() {
        Set<SlashCommandBuilder> builder = this.pendingCommands.stream()
                .map(GenericCommand::getCommandDefinition).collect(Collectors.toSet());

        Set<ApplicationCommand> commands = this.client
                .bulkOverwriteGlobalApplicationCommands(builder).join();

        System.out.println("Registered " + commands.size() + " commands");

        for (GenericCommand command : this.pendingCommands)
            command.register();

        Map<Long, GenericCommand> commandMap = new HashMap<>();

        Map<String, GenericCommand> commandsByName = new HashMap<>();
        for (GenericCommand command : this.pendingCommands)
            commandsByName.put(command.getName(), command);

        for (ApplicationCommand command : commands) {
            String name = command.getName();
            GenericCommand genericCommand = commandsByName.get(name);
            if (genericCommand == null) {
                log.warning("Could not find command " + name + " in pending commands");
                continue;
            }

            commandMap.put(command.getId(), genericCommand);
        }

        this.client.addSlashCommandCreateListener(l -> {
            SlashCommandInteraction interaction = l.getSlashCommandInteraction();
            long id = interaction.getCommandId();
            GenericCommand command = commandMap.get(id);
            command.execute(interaction);
        });
    }

    private List<GenericCommand> pendingCommands = new ArrayList<>();

    public void registerCommand(GenericCommand command) {
        this.pendingCommands.add(command);
    }

    public void disconnectBot() {
        this.client.disconnect().join();
    }
}
