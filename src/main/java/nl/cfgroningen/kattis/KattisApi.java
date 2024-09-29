package nl.cfgroningen.kattis;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import nl.cfgroningen.scores.UniversityScoreInformation;
import nl.cfgroningen.scores.UniversityScoreInformation.UniversityUserInformation;

public class KattisApi {
    public CompletableFuture<UniversityScoreInformation> getUniversityScoreInformation(String universityUrl) {
        return CompletableFuture.supplyAsync(() -> {
            Document doc = null;
            try {
                doc = Jsoup.connect(universityUrl).get();
            } catch (Exception e) {
                return null;
            }

            // "divider_list-flex"
            Element statsList = doc.getElementsByClass("divider_list-flex").get(0);

            Map<String, String> stats = statsList.getElementsByClass("divider_list-item").stream()
                    .filter(e -> e.getElementsByTag("span").size() == 2)
                    .collect(Collectors.toMap(e -> e.getElementsByTag("span").get(0).text(),
                            e -> e.getElementsByTag("span").get(1).text()));

            int rank = -1;
            try {
                rank = Integer.parseInt(stats.getOrDefault("Rank", "-1"));
            } catch (NumberFormatException e1) {
                // Ignore
            }
            double score = -1;
            try {
                score = Double.parseDouble(stats.getOrDefault("Score", "-1"));
            } catch (NumberFormatException e1) {
                // Ignore
            }
            int users = -1;
            try {
                users = Integer.parseInt(stats.getOrDefault("Users", "-1"));
            } catch (NumberFormatException e1) {
                // Ignore
            }

            String universityName = doc.getElementsByClass("image_info-text-main-header").get(0).text();

            // Start parsing the user information
            List<UniversityUserInformation> students = doc.getElementById("top_users").getElementsByTag("tbody").get(0)
                    .getElementsByTag("tr")
                    .stream().map(e -> {
                        // UniversityUserInformation

                        Element rankElement = e.getElementsByTag("td").get(0);
                        Element nameElement = e.getElementsByTag("td").get(1);
                        Element scoreElement = e.getElementsByTag("td").get(4);

                        int userRank = -1;
                        try {
                            userRank = Integer.parseInt(rankElement.text());
                        } catch (NumberFormatException e1) {
                            // Ignore
                        }

                        String userName = nameElement.text();
                        String profileUrl = "https://open.kattis.com"
                                + nameElement.getElementsByTag("a").get(0).attr("href");

                        double userScore = -1;
                        try {
                            userScore = Double.parseDouble(scoreElement.text());
                        } catch (NumberFormatException e1) {
                            // Ignore
                        }

                        return new UniversityScoreInformation.UniversityUserInformation(userRank, userName, profileUrl,
                                userScore);
                    }).collect(Collectors.toList());

            return new UniversityScoreInformation(rank, score, users, universityName, universityUrl, students);
        });
    }
}
