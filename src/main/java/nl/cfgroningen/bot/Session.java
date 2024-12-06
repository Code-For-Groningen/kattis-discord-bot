package nl.cfgroningen.bot;

import nl.cfgroningen.scores.UniversityScoreInformation;
import org.javacord.api.interaction.SlashCommandInteraction;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;

public class Session {

    public Timer timer = new Timer();

    private KattisDataManager dataManager;
    private KattisBot bot;
    private UniversityScoreInformation oldInfo;
    private UniversityScoreInformation newInfo;

    public void startSession(SlashCommandInteraction interaction){
        CompletableFuture<UniversityScoreInformation> future = dataManager
                .getUniversityStats(bot.getOwningUniversityUrl());

        if (future.isDone()){
            oldInfo = future.join();
        }

        newInfo = deltaInfo(newInfo, oldInfo);

        TimerTask timerTask = new TimerTask() {
            public void run() {
                UniversityScoreInformation info;
                CompletableFuture<UniversityScoreInformation> future = dataManager
                    .getUniversityStats(bot.getOwningUniversityUrl());
                if (future.isDone()){
                    info = future.join();
                    newInfo = deltaInfo(info, oldInfo);
                }
            }
        };
        timer.schedule(timerTask, 0, 5000);
    }

    public void stopSession(){
        timer.cancel();
    }

    public UniversityScoreInformation getInfo(){
        return newInfo;
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
