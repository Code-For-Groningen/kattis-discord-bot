package nl.cfgroningen.command;

import nl.cfgroningen.bot.KattisBot;
import nl.cfgroningen.bot.KattisDataManager;
import nl.cfgroningen.bot.Session;
import nl.cfgroningen.scores.UniversityScoreInformation;
import org.javacord.api.interaction.*;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class SessionCommand extends GenericCommand {

    private KattisDataManager dataManager;
    private Session session;

    public SessionCommand(KattisBot bot, KattisDataManager dataManager) {
        super(bot);
        this.dataManager = dataManager;
    }

    @Override
    public SlashCommandBuilder getCommandDefinition() {
        return SlashCommand.with("start_session", "Begin new competition session",
                Arrays.asList(
                        SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, "start",
                                "Starts a coding Session"),
                        SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, "stop",
                                "Stops a coding Session")
                ));
    }

    @Override
    public String getName() { return "Session"; }

    @Override
    public void execute(SlashCommandInteraction interaction) {
        session.startSession(interaction);
    }
}