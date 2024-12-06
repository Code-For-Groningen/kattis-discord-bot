package nl.cfgroningen.bot;

import nl.cfgroningen.scores.UniversityScoreInformation;
import org.javacord.api.interaction.SlashCommandInteraction;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

public class Session {

    public TimerTask timerTask;

    private KattisDataManager dataManager;
    private KattisBot bot;
    private UniversityScoreInformation oldInfo;

    public void startSession(SlashCommandInteraction interaction){
        CompletableFuture<UniversityScoreInformation> future = dataManager
                .getUniversityStats(bot.getOwningUniversityUrl());

        if (future.isDone()){
            oldInfo = future.join();
        }
        UniversityScoreInformation newInfo = oldInfo;
        newInfo = deltaInfo(newInfo, oldInfo);

        // TODO: Create embed

        timerTask = new TimerTask() {
            public void run() {
                UniversityScoreInformation info;
                CompletableFuture<UniversityScoreInformation> future = dataManager
                    .getUniversityStats(bot.getOwningUniversityUrl());
                if (future.isDone()){
                    info = future.join();
                    info = deltaInfo(info, oldInfo);

                    // TODO: Edit embed
                }
            }
        };
        timerTask.run();
    }

    public void stopSession(){
        timerTask.cancel();
    }

    private UniversityScoreInformation deltaInfo(UniversityScoreInformation info, UniversityScoreInformation oldinfo){
        // Compare students
        List<UniversityScoreInformation.UniversityUserInformation> deltaStudents = new ArrayList<>();
        for (UniversityScoreInformation.UniversityUserInformation student : info.getStudents()) {
            oldinfo.getStudents().stream()
                    .filter(oldStudent -> oldStudent.getName().equals(student.getName()))
                    .findFirst()
                    .ifPresentOrElse(oldStudent -> {
                        if (!student.equals(oldStudent)) {
                            deltaStudents.add(student);
                        }
                    }, () -> deltaStudents.add(student)); // New student
        }
        info.setStudents(deltaStudents);

        return info;
    }
}
