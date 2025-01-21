package sparkwinrate;

import datacleaner.Battle;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class SparkWinrate {
    /*
     * Read battles from the master data sets then compute statistics for each decks
     * subdecks can be also computes (1 2 3 4 ... ) cards by passing the argument.
     * each time statistics about the best evolution and best towers card are
     * provided
     */
    public static void main(String[] args) {
        String inputPath = args[0];
        String outputPath = args[1];

        System.out.println("Starting SparkWinrate");

        SparkConf conf = new SparkConf().setAppName("Winrate Calculator");
        JavaSparkContext sc = new JavaSparkContext(conf);
        DataReader dataReader = new DataReader(inputPath);

        // All battles
        JavaRDD<Battle> duelRDD = dataReader
                .getDistinctBattles(sc)
                .cache();

        // Log the number of battles loaded
        System.out.println("Number of battles loaded: " + duelRDD.count());

        // Log first few battles for inspection
        duelRDD.take(5).forEach(duel -> System.out.println("Battle: " + duel));

        // Get wins$
        System.out.println("Computing wins");
        JavaPairRDD<String, Integer> wins = duelRDD
                .mapToPair(duel -> {
                    String winnerDeck = duel.players.get(duel.winner).deck;
                    return new Tuple2<>(winnerDeck, 1);
                })
                .reduceByKey(Integer::sum);

        // Get losses
        System.out.println("Computing losses");
        JavaPairRDD<String, Integer> losses = duelRDD
                .mapToPair(duel -> {
                    String loserDeck = duel.players.get(duel.winner == 0 ? 1 : 0).deck;
                    return new Tuple2<>(loserDeck, 1);
                })
                .reduceByKey(Integer::sum);

        // Get winrates
        System.out.println("Computing winrates as ratio");
        JavaPairRDD<String, Tuple2<Integer, Integer>> winratesStruct = wins
                .fullOuterJoin(losses)
                .mapValues(tuple -> {
                    int winsCount = tuple._1.orElse(0);
                    int lossesCount = tuple._2.orElse(0);
                    return new Tuple2<>(winsCount, lossesCount);
                });

        // Compute winrates
        System.out.println("Computing winrates as double");
        JavaPairRDD<String, Double> winrates = winratesStruct
                .mapValues(tuple -> {
                    int winsCount = tuple._1;
                    int lossesCount = tuple._2;
                    double winrate = lossesCount == 0 ? 1.0 : (double) winsCount / (winsCount + lossesCount);
                    return winrate;
                })
                .cache();

        // Log some final winrates
        winrates.take(20).forEach(entry -> System.out.println("Final Winrate: " + entry._1 + " -> " + entry._2));

        // Write the winrates to the output file
        writeToFile(outputPath, winrates.collect());

        System.out.println("OK !!!!!!!!!!!!");
        sc.close();
    }

    private static void writeToFile(String fileName, List<Tuple2<String, Double>> winrateList) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(Paths.get(fileName)), StandardCharsets.UTF_8))) {
            writer.write("{\n");
            writer.write("  \"winrates\": [\n");

            // Write each deck's winrate as a JSON object
            for (int i = 0; i < winrateList.size(); i++) {
                Tuple2<String, Double> entry = winrateList.get(i);
                writer.write("    {\n");
                writer.write("      \"deck\": \"" + entry._1 + "\",\n");
                writer.write("      \"winrate\": " + entry._2 + "\n");
                writer.write("    }");

                // Add a comma if it's not the last element
                if (i < winrateList.size() - 1) {
                    writer.write(",");
                }
                writer.write("\n");
            }

            writer.write("  ]\n");
            writer.write("}\n");
        } catch (IOException ex) {
            ex.printStackTrace(); // Print the stack trace to identify the issue
        }
    }
}
