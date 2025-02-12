package nl.cfgroningen.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import express.Express;
import lombok.AllArgsConstructor;
import lombok.Data;
import nl.cfgroningen.bot.KattisDataManager;
import nl.cfgroningen.bot.Session;
import nl.cfgroningen.scores.UniversityScoreInformation;
import nl.cfgroningen.scores.UniversityScoreInformation.UniversityUserInformation;

import java.util.*;
import java.util.concurrent.CancellationException;

@AllArgsConstructor
public class API {
    private int port;
    private KattisDataManager dataManager;

    @Data
    private static class UniversityDTO {
        private String universityName;
        private int universityRank;
        private double universityScore;
        private List<PlayerDTO> players;
    }

    @Data
    private static class PlayerDTO {
        private String playerName;
        private double currentScore;
        private List<PlayerScoreHistoryDTO> scoreHistory = new ArrayList<>();

        public PlayerDTO(String playerName) {
            this.playerName = playerName;
        }
    }

    @Data
    @AllArgsConstructor
    private static class PlayerScoreHistoryDTO {
        private long date;
        private double score;
    }

    private UniversityDTO doApi() throws CancellationException {
        System.out.println("AAAA");
        Session ongoingSession = dataManager.getCurrentSession();

        System.out.println(ongoingSession);
        if (dataManager.getCurrentSession() == null) {
            return null;
        }

        Map<String, PlayerDTO> players = new HashMap<>();
        List<UniversityScoreInformation> infos = ongoingSession.getInfos();
        for (UniversityScoreInformation info : infos) {
            for (UniversityUserInformation user : info.getStudents()) {
                if (!players.containsKey(user.getName())) {
                    players.put(user.getName(), new PlayerDTO(user.getName()));
                }

                players.get(user.getName()).getScoreHistory().add(new PlayerScoreHistoryDTO(
                        info.getUnixMillis(), user.getScore()));
            }
        }

        UniversityScoreInformation last = infos.get(infos.size() - 1);
        for (UniversityUserInformation user : last.getStudents()) {
            if (!players.containsKey(user.getName())) {
                continue;
            }

            players.get(user.getName()).setCurrentScore(user.getScore());
        }


        List<PlayerDTO> playerList = new ArrayList<>(players.values());
        playerList.sort(Comparator.comparing(PlayerDTO::getCurrentScore).reversed());

        UniversityDTO result = new UniversityDTO();
        result.setUniversityName(infos.get(0).getName());
        result.setUniversityRank(infos.get(0).getRank());
        result.setUniversityScore(infos.get(0).getScore());
        result.setPlayers(playerList);

        return result;
    }

    public void serveAPI() {
        Express app = new Express();
        app.get("/", (req, res) -> {
            try {
                UniversityDTO result = doApi();
                if (result == null) {
                    res.send("{\"error\": \"No ongoing session right now!\"}");
                    return;
                }
                res.send(new ObjectMapper().writeValueAsString(result));
            } catch (Exception e) {
                e.printStackTrace();
                res.send("{\"error\": \"failed with " + e + "\"}");
            }
        }).listen(this.port);
    }
}
