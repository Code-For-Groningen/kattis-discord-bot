package nl.cfgroningen.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.cfgroningen.bot.KattisBot;
import nl.cfgroningen.bot.KattisDataManager;
import nl.cfgroningen.scores.UniversityScoreInformation;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.*;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ContributeCommand extends GenericCommand {

    private KattisDataManager dataManager;

    public ContributeCommand(KattisBot bot, KattisDataManager dataManager) {
        super(bot);
        this.dataManager = dataManager;
    }

    @Override
    public String getName() {
        return "contribute";
    }

    @Override
    public SlashCommandBuilder getCommandDefinition() {
        return SlashCommand
                .with("contribute", "Calculate the what points actually mean for the university score!",
                        Arrays.asList(SlashCommandOption.create(SlashCommandOptionType.DECIMAL, "points",
                                "The amount of points you want to calculate for!", true)));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class CalculationResult {
        private double oldScore;
        private double newScore;

        private int userPosition;

        private boolean userInTop50WithNewScore;
        private boolean userInTop50WithOldScore;
    }

    @Override
    public void execute(SlashCommandInteraction interaction) {
        double points = interaction.getArgumentDecimalValueByName("points").orElse(0.0);

        if (points < 0) {
            interaction.createImmediateResponder()
                    .setFlags(MessageFlag.EPHEMERAL).addEmbed(this.error("Invalid points",
                            "The amount of points you provided is invalid! It must be a positive number!"))
                    .respond();
            return;
        }

        // Make sure the user is linked
        String userUrl = bot.getData().getCachedData().getDiscordIdToKattisProfileUrl()
                .get(interaction.getUser().getId());

        if (userUrl == null) {
            interaction.createImmediateResponder()
                    .setFlags(MessageFlag.EPHEMERAL).addEmbed(this.error("Not linked",
                            "You have not linked your Kattis account to your Discord account!"))
                    .respond();
            return;
        }

        CompletableFuture<UniversityScoreInformation> future = dataManager
                .getUniversityStats(bot.getOwningUniversityUrl());

        Function<CalculationResult, EmbedBuilder> embedGenerator = result -> new EmbedBuilder()
                .setTitle("Contribution calculation")
                .addField("Old score", String.format("%.1f", result.getOldScore()), true)
                .addField("New score", String.format("%.1f", result.getNewScore()), true)
                .addField("User position", result.getUserPosition() == -1 ? "Not in top 50"
                        : "#" + (result.getUserPosition() + 1), true)
                .setColor(Color.GREEN)
                .setFooter("Good luck getting the points!");

        if (!future.isDone()) {
            interaction.createImmediateResponder()
                    .addEmbed(this.info("Please wait", "Retrieving university information..."))
                    .respond().thenCombine(future, (msg, info) -> {
                        CalculationResult result = calculateNewScore(info, points, userUrl);

                        msg.removeAllEmbeds().addEmbed(embedGenerator.apply(result)).update();

                        return null;
                    });
        } else {
            UniversityScoreInformation info = future.join();
            CalculationResult result = calculateNewScore(info, points, userUrl);
            interaction.createImmediateResponder()
                    .addEmbed(embedGenerator.apply(result))
                    .respond();
        }
    }

    private CalculationResult calculateNewScore(UniversityScoreInformation info, double points, String userUrl) {
        List<Map.Entry<String, Double>> scores = info.getStudents().stream().map(c -> {
            return Map.entry(c.getProfileUrl(), c.getScore());
        }).collect(Collectors.toList());

        // Remove the user if they exist
        for (int i = 0; i < scores.size(); i++) {
            if (scores.get(i).getKey().equals(userUrl)) {
                scores.remove(i);
                break;
            }
        }

        // Add the user back in
        scores.add(Map.entry(userUrl, points));

        // Sort the scores
        scores.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // If bigger than 50 we need to remove the last one
        boolean bumped = false;
        if (scores.size() > 50) {
            scores.remove(scores.size() - 1);
            bumped = true;
        }

        // Find the current position of the user
        int userPosition = -1;
        for (int i = 0; i < scores.size(); i++) {
            if (scores.get(i).getKey().equals(userUrl)) {
                userPosition = i;
                break;
            }
        }

        double score = 0;
        double f = 5;

        for (int i = 0; i < scores.size(); i++) {
            score += Math.pow((1.0 - 1.0 / f), i) * scores.get(i).getValue();
        }

        return new CalculationResult(info.getScore(), score / f, userPosition,
                userPosition < 50, bumped);
    }

}
