package nl.cfgroningen.command;

import nl.cfgroningen.bot.KattisBot;
import nl.cfgroningen.bot.KattisDataManager;
import nl.cfgroningen.bot.Session;
import nl.cfgroningen.scores.UniversityScoreInformation;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

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
        return SlashCommand.with("start_session", "Begin new competition session");
    }

    @Override
    public String getName() { return "start_session"; }

    @Override
    public void execute(SlashCommandInteraction interaction) {
        session.startSession(interaction);
    }
}