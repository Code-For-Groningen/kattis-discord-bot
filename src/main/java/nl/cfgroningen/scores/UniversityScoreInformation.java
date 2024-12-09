package nl.cfgroningen.scores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import nl.cfgroningen.database.BotData;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniversityScoreInformation {
    private int rank;
    private double score;
    private int users;

    private String name;
    private String url;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UniversityUserInformation {
        private int rank;
        private String name;
        private String profileUrl;
        private double score;
    }

    private List<UniversityUserInformation> students = new ArrayList<>();

    private long unixMillis;

    private String formatUserInformation(UniversityUserInformation s, Map<String, Long> listOfLinks) {
        if (listOfLinks.containsKey(s.getProfileUrl())) {
            return String.format("%d. [%s](%s) - %.2f (<@%s>)", s.rank, s.name, s.profileUrl, s.score,
                    listOfLinks.get(s.getProfileUrl()) + "");
        } else {
            return String.format("%d. [%s](%s) - %.2f", s.rank, s.name, s.profileUrl, s.score);
        }
    }

    public EmbedBuilder toEmbed(BotData data) {
        Map<String, Long> links = new HashMap<>();
        data.getCachedData().getDiscordIdToKattisProfileUrl().forEach((c, v) -> links.put(v, c));

        return new EmbedBuilder()
                .setTitle("University Information")
                .addField("Rank", String.valueOf(rank), true)
                .addField("Score", String.valueOf(score), true)
                .addField("Users", String.valueOf(users), true)
                .addField("Name", "[" + name + "](" + url + ")", true)
                .addField("Top Students", students.stream()
                        .map(s -> formatUserInformation(s, links))
                        .limit(3)
                        .collect(Collectors.joining("\n")));
    }
}
