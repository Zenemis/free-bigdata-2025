package sparkwinrate;

import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;
import datacleaner.Battle;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;


public class DataReader {
    public static final int WEEKS = 9;

    private final String path;
    private final int weeks;

    public DataReader(String path, int weeks) {
        this.path = path;
        this.weeks = weeks;
    }

    public DataReader(String path) {
        this.path = path;
        this.weeks = 9;
    }

    public JavaRDD<Battle> getDistinctBattles(JavaSparkContext sc) {
        // Lecture des fichiers JSON Hadoop
        JavaRDD<String> rdd = sc.textFile(path).filter((x) -> !x.isEmpty() || x.equals("test"));

        // Transformation des JSON en objets Battle
        ObjectMapper mapper = new ObjectMapper();
        JavaRDD<Battle> rddpair = rdd.flatMap((x) -> {
            try {
                // Tente de convertir la ligne en objet Battle
                Battle battle = mapper.readValue(x, Battle.class);
                return java.util.Collections.singletonList(battle).iterator();
            } catch (Exception e) {
                // En cas d'erreur, log la ligne problématique (optionnel)
                System.err.println("Invalid JSON: " + x);
                return java.util.Collections.emptyIterator();
            }
        }).filter((battle) -> battle.winner == 0 || battle.winner == 1);

        // Filtrage des batailles selon la fenêtre temporelle
        Instant sliding_window = Instant.now().minusSeconds(3600L * 24 * 7 * weeks);
        Instant collect_start = Instant.parse("2024-09-26T09:00:00Z");

        rddpair = rddpair.filter((Battle x) -> {
            Instant inst = x.date;
            return inst.isAfter(sliding_window) && inst.isAfter(collect_start);
        });

        return rddpair;
    }
}
