package nl.cfgroningen.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;

import nl.cfgroningen.bot.KattisBot;
import nl.cfgroningen.bot.KattisDataManager;
import nl.cfgroningen.bot.KattisVisualSummaryGenerator;
import nl.cfgroningen.bot.Session;

public class SessionCommand extends GenericCommand {
    private KattisDataManager dataManager;
    private KattisVisualSummaryGenerator visualSummaryGenerator;

    public SessionCommand(KattisBot bot, KattisDataManager dataManager) {
        super(bot);
        this.dataManager = dataManager;
        this.visualSummaryGenerator = new KattisVisualSummaryGenerator();
    }

    @Override
    public SlashCommandBuilder getCommandDefinition() {
        return SlashCommand.with("session", "Begin new competition session",
                Arrays.asList(
                        SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, "start",
                                "Starts a coding Session"),
                        SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, "stop",
                                "Stops a coding Session"),
                        SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, "summary",
                                "Gives a summary of score evolution")));
    }

    @Override
    public String getName() {
        return "session";
    }

    @Override
    public void execute(SlashCommandInteraction interaction) {
        SlashCommandInteractionOption option = interaction.getOptionByIndex(0).orElseThrow();

        String subcommand = interaction.getFullCommandName().split(" ")[1];

        Session ongoingSession = bot.getData().getCachedData().getSession();

        if (subcommand.equalsIgnoreCase("start")) {
            if (ongoingSession != null) {
                interaction.createImmediateResponder().addEmbed(getSessionAlreadyStarted()).respond();
                return;
            }

            // Start Session
            Session newSession = new Session();
            try {
                newSession.startSession(this.dataManager, this.bot);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                interaction.createImmediateResponder().addEmbed(getSomethingWentWrong()).respond();
            }
            interaction.createImmediateResponder().addEmbed(getSessionStarted()).respond();

            bot.getData().getCachedData().setSession(newSession);
            bot.getData().save();
        } else if (subcommand.equalsIgnoreCase("stop")) {
            if (ongoingSession == null) {
                interaction.createImmediateResponder().addEmbed(getNoSessionToStop()).respond();
                return;
            }

            interaction.createImmediateResponder().addEmbed(getSessionStopped()).respond();
            bot.getData().getCachedData().setSession(null);
            bot.getData().save();
        } else if (subcommand.equalsIgnoreCase("summary")) {
            if (ongoingSession == null) {
                interaction.createImmediateResponder().addEmbed(getSessionStopped()).respond();
                return;
            }


            try {
                File tempFile = Files.createTempFile("rfovciujn", ".jpg").toFile();
                FileOutputStream stream = new FileOutputStream(tempFile);
                stream.write(this.visualSummaryGenerator.generateVisualSummary());
                stream.close();

                System.out.println(tempFile.getAbsolutePath());

                interaction.createImmediateResponder()
                        .addEmbed(new EmbedBuilder().setTitle("loading boss"))
                        .respond()
                        .join()
                        .removeAllEmbeds()
                        .addEmbed(
                                new EmbedBuilder().setImage(tempFile)
                        ).update();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private EmbedBuilder getSomethingWentWrong() {
        return new EmbedBuilder()
                .setTitle("Something went wrong")
                .setDescription("Something went terribly wrong");
    }

    private EmbedBuilder getSessionStarted() {
        return new EmbedBuilder()
                .setTitle("Session started")
                .setDescription("The session has been started!");
    }

    private EmbedBuilder getSessionStopped() {
        return new EmbedBuilder()
                .setTitle("Session stopped!")
                .setDescription("The session has been stopped!");

        // TODO: Implement a summary of all scores during the session
    }

    private EmbedBuilder getNoSessionToStop() {
        return new EmbedBuilder()
                .setTitle("No ongoing session")
                .setDescription("There are no ongoing sessions, can't stop the session");
    }

    private EmbedBuilder getSessionAlreadyStarted() {
        return new EmbedBuilder()
                .setTitle("Session already started")
                .setDescription("The session is already started");
    }
}