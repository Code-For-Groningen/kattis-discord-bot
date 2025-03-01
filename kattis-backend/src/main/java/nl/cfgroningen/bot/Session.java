package nl.cfgroningen.bot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.extern.java.Log;
import nl.cfgroningen.scores.UniversityScoreInformation;
import nl.cfgroningen.scores.UniversityScoreInformation.UniversityUserInformation;

import org.javacord.api.entity.Deletable;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

@Log
@Data
public class Session {
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Timer timer = new Timer();

    private List<UniversityScoreInformation> infos = new ArrayList<>();
    private Map<String, UniversityUserInformation> init;

    private Set<Map.Entry<Long, Long>> summaryMessageIds = new HashSet<>();

    @AllArgsConstructor
    private class InformationFetcher extends TimerTask {
        private KattisDataManager dataManager;
        private KattisBot bot;

        @Override
        public void run() {
            // Update the session with the latest information
            dataManager.getFreshUniversityStats(bot.getOwningUniversityUrl()).whenComplete((i, e) -> {
                if (e == null) {
                    if (infos.size() > 10) {
                        infos.remove(0);
                    }
                    for (UniversityUserInformation student : i.getStudents()) {
                        student.setScore(
                            student.getScore() - init.get(student.getName()).getScore()
                        );
                    }
                    infos.add(i);
                } else {
                    log.warning("Failed to fetch university stats: " + e.getMessage());
                }
            });
        }
    }

    @AllArgsConstructor
    private class SummaryUpdater extends TimerTask {
        private KattisBot bot;
        private KattisVisualSummaryGenerator visualSummaryGenerator;
        private Function<BufferedImage, EmbedBuilder> embedBuilderFunction;

        @Override
        public void run() {
            // Generate the visual summary
            BufferedImage image = visualSummaryGenerator.generateVisualSummary(infos);

            for (Map.Entry<Long, Long> messageUid : summaryMessageIds) {
                long messageId = messageUid.getKey();
                long channelId = messageUid.getValue();


                BufferedImage stupidDiscordFixer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

                bot.getClient().getTextChannelById(channelId)
                        .ifPresent(channel -> channel.getMessageById(messageId).thenAcceptAsync(
                                message -> message.createUpdater()
                                        .removeAllEmbeds()
                                        .removeExistingAttachments()
                                        .addAttachment(stupidDiscordFixer, "_.png")
                                        .addEmbed(embedBuilderFunction.apply(image))
                                        .applyChanges()));
            }
        }
    }

    public void startSession(
            KattisDataManager dataManager,
            KattisBot bot,
            KattisVisualSummaryGenerator summaryGenerator,
            Function<BufferedImage, EmbedBuilder> embedBuilderFunction
    ) throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<UniversityScoreInformation> future = dataManager
                .getFreshUniversityStats(bot.getOwningUniversityUrl());

        // To even start the session we need at least the first info
        UniversityScoreInformation info = future.get(10, TimeUnit.SECONDS);
        init = new HashMap<String, UniversityUserInformation>();
        for (UniversityUserInformation student : info.getStudents()) {
            init.put(student.getName(), student);
            student.setScore(0); //set delta
        }

        timer.schedule(new InformationFetcher(dataManager, bot), 0, 5000);
        timer.schedule(new SummaryUpdater(bot, summaryGenerator, embedBuilderFunction), 0, 10000);
    }

    public void stopSession(KattisBot bot) {
        timer.cancel();

        // Go through the messages and delete them
        for (Map.Entry<Long, Long> messageUid : summaryMessageIds) {
            long messageId = messageUid.getKey();
            long channelId = messageUid.getValue();

            bot.getClient().getTextChannelById(channelId)
                    .ifPresent(channel -> channel.getMessageById(messageId).thenAcceptAsync(Deletable::delete));
        }
    }
}
