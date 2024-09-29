package nl.cfgroningen.scores;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public EmbedBuilder toEmbed() {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("University Information")
                .addField("Rank", String.valueOf(rank), true)
                .addField("Score", String.valueOf(score), true)
                .addField("Users", String.valueOf(users), true)
                .addField("Name", "[" + name + "](" + url + ")", true)
                .addField("Top Students", students.stream()
                        .map(s -> String.format("%d. [%s](%s) - %.2f", s.rank, s.name, s.profileUrl, s.score))
                        .limit(3)
                        .collect(Collectors.joining("\n")));

        return builder;
    }
}
