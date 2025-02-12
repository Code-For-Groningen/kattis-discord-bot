package nl.cfgroningen.bot;

import lombok.AllArgsConstructor;
import nl.cfgroningen.database.BotData.CachedData;
import nl.cfgroningen.scores.UniversityScoreInformation;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class KattisDataManager {
    public static final long CACHE_TIME = TimeUnit.DAYS.toSeconds(1);

    private KattisBot bot;

    public Session getCurrentSession() {
        return bot.getData().getCachedData().getSession();
    }

    public CompletableFuture<UniversityScoreInformation> getFreshUniversityStats(String universityUrl) {
        CachedData data = bot.getData().getCachedData();

        return bot.getKattisApi().getUniversityScoreInformation(universityUrl).thenApplyAsync(info -> {
            data.getUniversityScores().put(universityUrl, info);
            data.getLastUpdated().put(universityUrl, Instant.now().getEpochSecond());

            bot.getData().save();

            return info;
        });
    }

    public CompletableFuture<UniversityScoreInformation> getUniversityStats(String universityUrl) {
        CachedData data = bot.getData().getCachedData();

        if (data.getUniversityScores().containsKey(universityUrl)) {
            long lastUpdated = data.getLastUpdated().get(universityUrl);
            long currentTime = Instant.now().getEpochSecond();

            if (currentTime - lastUpdated < CACHE_TIME) {
                return CompletableFuture.completedFuture(data.getUniversityScores().get(universityUrl));
            }
        }

        return getFreshUniversityStats(universityUrl);
    }

}
