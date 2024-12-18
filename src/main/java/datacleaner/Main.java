package datacleaner;

import java.io.File;
import java.util.List;
import org.codehaus.jackson.map.ObjectMapper;

public class Main {
    public static void main(String[] args) {
        try {
            // Path to the JSON file
            String filePath = "data_cr_example.json";

            // Create ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();

            // Deserialize the JSON file into a list of Battle objects
            List<Battle> battles = objectMapper.readValue(
                    new File(filePath),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Battle.class)
            );

            // Print out the deserialized objects
            for (Battle battle : battles) {
                System.out.println(battle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}