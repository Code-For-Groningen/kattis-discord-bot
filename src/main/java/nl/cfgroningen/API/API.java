package nl.cfgroningen.API;

import express.Express;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileNotFoundException;
/*import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;*/
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.Vector;

import com.google.gson.Gson;
//import nl.cfgroningen.kattis.KattisApi;
import nl.cfgroningen.scores.UniversityScoreInformation;
import nl.cfgroningen.scores.UniversityScoreInformation.UniversityUserInformation;
import nl.cfgroningen.database.BotData.CachedData;

public class API {
    /*private static String doApi(String universityUrl) 
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
    }*/

    private static String doApi(String universityUrl) 
        throws InterruptedException, 
               ExecutionException, 
               TimeoutException, 
               CancellationException, 
               FileNotFoundException,
               JsonProcessingException,
               JsonMappingException
    {
        Gson gson = new Gson();
        //KattisApi kapi = new KattisApi();
        Scanner scan = new Scanner(new File("data.json"));
        String data = scan.useDelimiter("\\Z").next();
        ObjectMapper mapper = new ObjectMapper();
        CachedData cache = mapper.readValue(data, CachedData.class);
        List<UniversityScoreInformation> scores = cache.getSession().getInfos();
        UniversityScoreInformation info = scores.get(scores.size()-1);
        University uni = new University();
        uni.universityName = info.getName();
        uni.universityRank = info.getRank();
        uni.universityScore = info.getScore();
        List<UniversityUserInformation> users = info.getStudents();
        Vector<Player> players = new Vector<>();
        ListIterator<UniversityUserInformation> iter = users.listIterator();
        while (iter.hasNext()) {
            var i = iter.next();
            Player cur = new Player();
            cur.currentScore = i.getScore();
            cur.playerName = i.getName();
            
            ListIterator<UniversityScoreInformation> cacheiter = scores.listIterator();
            Vector<PlayerScoreHistory> histories = new Vector<>();
            while (cacheiter.hasNext()) {
                UniversityUserInformation curscore = null;
                var curglob = cacheiter.next();
                for (UniversityUserInformation s: curglob.getStudents()) {
                    if (s.getName() == i.getName()) {
                        curscore = s;
                        break;
                    }
                }
                if (curscore == null) {
                    continue;
                }
                PlayerScoreHistory history = new PlayerScoreHistory();
                history.date = curglob.getUnixMillis();
                history.score = curscore.getScore();
                histories.add(history);
            }
            cur.scoreHistory = histories.toArray(PlayerScoreHistory[]::new);
            players.add(cur);
        }
        uni.players = players.toArray(Player[]::new);
        scan.close();
        return gson.toJson(uni);
    }

    public static void serveAPI(String universityUrl) {
        Express app = new Express();
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
                res.send("failed with " + e + " at " + e.getStackTrace()[0].getFileName() + ":" + e.getStackTrace()[0].getLineNumber() + "\n");
            }
        }).listen();
    }
}
