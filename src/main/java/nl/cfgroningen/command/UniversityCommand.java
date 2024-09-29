package nl.cfgroningen.command;

import java.util.concurrent.CompletableFuture;

import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

import nl.cfgroningen.bot.KattisBot;
import nl.cfgroningen.bot.KattisDataManager;
import nl.cfgroningen.scores.UniversityScoreInformation;

public class UniversityCommand extends GenericCommand {

    private KattisDataManager dataManager;

    public UniversityCommand(KattisBot bot, KattisDataManager dataManager) {
        super(bot);
        this.dataManager = dataManager;
    }

    @Override
    public SlashCommandBuilder getCommandDefinition() {
        return SlashCommand.with("university", "Get the status of the university!");
    }

    @Override
    public String getName() {
        return "university";
    }

    @Override
    public void execute(SlashCommandInteraction interaction) {
        CompletableFuture<UniversityScoreInformation> future = dataManager
                .getUniversityStats(bot.getOwningUniversityUrl());

        if (future.isDone()) {
            UniversityScoreInformation info = future.join();
            interaction.createImmediateResponder()
                    .addEmbed(info.toEmbed())
                    .respond();
        } else {
            interaction.createImmediateResponder()
                    .addEmbed(this.info("Please wait", "Retrieving university information..."))
                    .respond().thenCombine(future, (msg, info) -> {
                        msg.removeAllEmbeds().addEmbed(info.toEmbed()).update();
                        return null;
                    });
        }
    }

}
