package nl.cfgroningen.API;

import express.Express;
import lombok.NonNull;

import java.util.Date;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.List;
import java.util.ListIterator; 

import com.google.gson.Gson;
import nl.cfgroningen.kattis.KattisApi;
import nl.cfgroningen.scores.UniversityScoreInformation;
import nl.cfgroningen.scores.UniversityScoreInformation.UniversityUserInformation;

public class API {
    private static String doApi(String universityUrl) 
        throws InterruptedException, ExecutionException, TimeoutException, CancellationException 
    {
        Gson gson = new Gson();
        KattisApi kapi = new KattisApi();
        CompletableFuture<UniversityScoreInformation> futureinfo = kapi.getUniversityScoreInformation(universityUrl);
        UniversityScoreInformation info = futureinfo.get(10, TimeUnit.SECONDS);
        if (info == null) {
            return "info was null";
        }
        University uni = new University();
        uni.universityName = info.getName();
        uni.universityRank = info.getRank();
        uni.universityScore = info.getScore();
        List<UniversityUserInformation> users = info.getStudents();
        uni.players = new Player[users.size()];
        ListIterator<UniversityUserInformation> iter = users.listIterator();
        while (iter.hasNext()) {
            UniversityUserInformation i = iter.next();
            Player cur = new Player();
            cur.currentScore = i.getScore();
            cur.playerName = i.getName();
            PlayerScoreHistory history = new PlayerScoreHistory();
            history.date = new Date().getTime();
            history.score = i.getScore();
            PlayerScoreHistory[] histories = new PlayerScoreHistory[1];
            histories[0] = history;
            cur.scoreHistory = histories;
            uni.players[iter.previousIndex()] = cur;
        }
        return gson.toJson(uni);
    }

    public static void serveAPI(String universityUrl) {
        Express app = new Express();
        System.out.println("troller");
        app.get("/", (req, res) -> {
            /*
             * interface University {
              universityName: string;
              universityRank: number;
              universityScore: number;
              players: Player[];
            }

            interface Player {
              playerName: string;
              currentScore: number;
              scoreHistory: PlayerScoreHistory[];
            }

            interface PlayerScoreHistory {
              date:number; // unix timestamp
              score: number;
            }
             */
            try {
                res.send(doApi(universityUrl));
            } catch (Exception e) {
                res.send("failed with " + e + "\n");
            }
        }).listen();
    }
}
