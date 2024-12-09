package nl.cfgroningen.bot;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import nl.cfgroningen.command.*;
import nl.cfgroningen.database.BotData;
import nl.cfgroningen.kattis.KattisApi;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.interaction.ApplicationCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

import java.util.*;
import java.util.stream.Collectors;

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
                .login().thenAcceptAsync(loggedInClient -> {
                    this.client = loggedInClient;

                    this.registerCommand(new UniversityCommand(this, dataManager));
                    this.registerCommand(new LinkCommand(this));
                    this.registerCommand(new ContributeCommand(this, dataManager));
                    this.registerCommand(new SessionCommand(this, dataManager));

                    this.registerAllCommands();
                });
    }

    private void registerAllCommands() {
        Set<SlashCommandBuilder> builder = this.pendingCommands.stream()
                .map(GenericCommand::getCommandDefinition)
                .collect(Collectors.toSet());

        Set<ApplicationCommand> commands = this.client
                .bulkOverwriteGlobalApplicationCommands(builder).join();

        log.info("Registered " + commands.size() + " commands");

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

        // Check if session needs to be restarted
        if (this.data.getCachedData().getSession() != null) {
            SessionCommand sessionCommand = (SessionCommand) commandsByName.get("session");
            if (!sessionCommand.startSession(null, this.data.getCachedData().getSession())) {
                this.data.getCachedData().getSession().stopSession(this);
                this.data.getCachedData().setSession(null);

                log.warning("Failed to start session");
            }
        }
    }

    private final List<GenericCommand> pendingCommands = new ArrayList<>();

    public void registerCommand(GenericCommand command) {
        this.pendingCommands.add(command);
    }

    public void disconnectBot() {
        this.client.disconnect().join();
    }
}
