package nl.cfgroningen.database;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nl.cfgroningen.scores.UniversityScoreInformation;

@Getter
public class BotData {
    @Getter(AccessLevel.NONE)
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * A map of all the university names, where the key is
     * the URL of the university and the value is the name of the university.
     */
    private Map<String, String> universityNames = new HashMap<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CachedData {
        private Map<String, UniversityScoreInformation> universityScores = new HashMap<>();
        private Map<String, Long> lastUpdated = new HashMap<>();

        private Map<Long, String> discordIdToKattisProfileUrl = new HashMap<>();
    }

    private CachedData cachedData;

    public static BotData load() {
        // Start by loading up the data from the embedded file universities.json
        InputStream stream = BotData.class.getClassLoader().getResourceAsStream("universities.json");
        if (stream == null) {
            throw new RuntimeException("Could not find universities.json");
        }

        BotData data = new BotData();

        try {
            TypeReference<Map<String, String>> type = new TypeReference<Map<String, String>>() {
            };
            data.universityNames = data.mapper.readValue(stream, type);
        } catch (Exception e) {
            throw new RuntimeException("Could not load universities.json", e);
        }

        // Load CachedData from data.json
        File file = new File("data.json");
        if (!file.exists()) {
            file.getAbsoluteFile().getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (Exception e) {
                throw new RuntimeException("Could not create data.json", e);
            }
        }

        try {
            data.cachedData = data.mapper.readValue(file, CachedData.class);
        } catch (Exception e) {
            data.cachedData = new CachedData();
        }

        return data;
    }

    public void save() {
        try {
            mapper.writeValue(new File("data.json"), cachedData);
        } catch (Exception e) {
            throw new RuntimeException("Could not save data.json", e);
        }
    }
}
