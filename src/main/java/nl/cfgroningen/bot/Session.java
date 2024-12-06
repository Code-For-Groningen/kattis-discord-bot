package nl.cfgroningen.bot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import nl.cfgroningen.scores.UniversityScoreInformation;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Data
public class Session {
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private transient Timer timer = new Timer();

    private List<UniversityScoreInformation> infos = new ArrayList<>();

    public void startSession(KattisDataManager dataManager, KattisBot bot)
            throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<UniversityScoreInformation> future = dataManager
                .getUniversityStats(bot.getOwningUniversityUrl());

        // To even start the session we need at least the first info
        UniversityScoreInformation info = future.get(10, TimeUnit.SECONDS);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    UniversityScoreInformation info = dataManager
                            .getUniversityStats(bot.getOwningUniversityUrl()).get(10, TimeUnit.SECONDS);
                    infos.add(info);

                } catch (Exception e) {
                    // lol
                }
            }
        };

        timer.schedule(timerTask, 0, 5000);
    }

    public void stopSession() {
        timer.cancel();
    }

}
