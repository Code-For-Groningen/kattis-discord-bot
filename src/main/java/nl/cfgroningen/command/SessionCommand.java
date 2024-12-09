package nl.cfgroningen.command;

import lombok.extern.java.Log;
import nl.cfgroningen.bot.KattisBot;
import nl.cfgroningen.bot.KattisDataManager;
import nl.cfgroningen.bot.KattisVisualSummaryGenerator;
import nl.cfgroningen.bot.Session;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

@Log
public class SessionCommand extends GenericCommand {
    private KattisDataManager dataManager;
    private KattisVisualSummaryGenerator visualSummaryGenerator;

    public SessionCommand(KattisBot bot, KattisDataManager dataManager) {
        super(bot);
        this.dataManager = dataManager;
        this.visualSummaryGenerator = new KattisVisualSummaryGenerator();

        bot.getClient().addMessageDeleteListener(event -> {
            if (bot.getData().getCachedData().getSession() == null) {
                return;
            }

            bot.getData().getCachedData().getSession().getSummaryMessageIds().removeIf(entry -> entry.getKey().equals(event.getMessageId()) && entry.getValue().equals(event.getChannel().getId()));
        });
    }

    @Override
    public SlashCommandBuilder getCommandDefinition() {
        return SlashCommand.with("session", "Begin new competition session", Arrays.asList(SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, "start", "Starts a coding Session"), SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, "stop", "Stops a coding Session"), SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, "summary", "Gives a summary of score evolution")));
    }

    @Override
    public String getName() {
        return "session";
    }

    @Override
    public void execute(SlashCommandInteraction interaction) {
        String subcommand = interaction.getFullCommandName().split(" ")[1];

        Session ongoingSession = bot.getData().getCachedData().getSession();

        if (subcommand.equalsIgnoreCase("start")) {
            handleSessionStart(interaction, ongoingSession);
        } else if (subcommand.equalsIgnoreCase("stop")) {
            handleSessionStop(interaction, ongoingSession);
        } else if (subcommand.equalsIgnoreCase("summary")) {
            handleSessionSummary(interaction, ongoingSession);
        }
    }

    private void handleSessionSummary(SlashCommandInteraction interaction, Session ongoingSession) {
        if (ongoingSession == null) {
            interaction.createImmediateResponder().addEmbed(getSessionStopped()).respond();
            return;
        }

        // Start the loading message
        interaction.createImmediateResponder().addEmbed(getLoadingMessage()).respond().thenAcceptAsync(responder -> {
            // Update to contain the actual summary
            BufferedImage image = visualSummaryGenerator.generateVisualSummary(ongoingSession.getInfos());

            responder.removeAllEmbeds().addEmbed(getCurrentKattisStandings(image)).update().thenAccept(message -> {
                ongoingSession.getSummaryMessageIds().add(Map.entry(message.getId(), message.getChannel().getId()));
                bot.getData().save();
            });
        });
    }


    private void handleSessionStop(SlashCommandInteraction interaction, Session ongoingSession) {
        if (ongoingSession == null) {
            interaction.createImmediateResponder().addEmbed(getNoSessionToStop()).respond();
            return;
        }

        interaction.createImmediateResponder().addEmbed(getSessionStopped()).respond();
        ongoingSession.stopSession(this.bot);
        bot.getData().getCachedData().setSession(null);
        bot.getData().save();
    }

    private void handleSessionStart(SlashCommandInteraction interaction, Session ongoingSession) {
        if (ongoingSession != null) {
            interaction.createImmediateResponder().addEmbed(getSessionAlreadyStarted()).respond();
            return;
        }

        // Start Session
        Session newSession = new Session();
        if (!startSession(interaction, newSession)) return;

        interaction.createImmediateResponder().addEmbed(getSessionStarted()).respond();

        bot.getData().getCachedData().setSession(newSession);
        bot.getData().save();
    }

    public boolean startSession(SlashCommandInteraction interaction, Session newSession) {
        try {
            newSession.startSession(this.dataManager, this.bot, this.visualSummaryGenerator, this::getCurrentKattisStandings);
        } catch (ExecutionException | TimeoutException e) {
            log.log(Level.WARNING, e.getMessage(), e);
            if (interaction != null) interaction.createImmediateResponder().addEmbed(getSomethingWentWrong()).respond();
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.log(Level.WARNING, e.getMessage(), e);
            if (interaction != null) interaction.createImmediateResponder().addEmbed(getSomethingWentWrong()).respond();
            return false;
        }

        return true;
    }


    public EmbedBuilder getCurrentKattisStandings(BufferedImage image) {
        return new EmbedBuilder().setTitle("Current Kattis standings").setImage(image).setFooter("Last updated at " + new SimpleDateFormat("HH:mm:ss").format(Instant.now().toEpochMilli()));
    }


    private EmbedBuilder getSomethingWentWrong() {
        return new EmbedBuilder().setTitle("Something went wrong").setDescription("Something went terribly wrong");
    }

    private EmbedBuilder getSessionStarted() {
        return new EmbedBuilder().setTitle("Session started").setDescription("The session has been started!");
    }

    private EmbedBuilder getSessionStopped() {
        return new EmbedBuilder().setTitle("Session stopped!").setDescription("The session has been stopped!");
    }

    private EmbedBuilder getNoSessionToStop() {
        return new EmbedBuilder().setTitle("No ongoing session").setDescription("There are no ongoing sessions, can't stop the session");
    }

    private EmbedBuilder getSessionAlreadyStarted() {
        return new EmbedBuilder().setTitle("Session already started").setDescription("The session is already started");
    }

    private EmbedBuilder getLoadingMessage() {
        return new EmbedBuilder().setTitle("Loading").setDescription("Loading the summary");
    }
}