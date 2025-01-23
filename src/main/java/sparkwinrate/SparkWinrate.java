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
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
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

        int GAMES = 80;
        int NGRAM_MAX = 8;

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

        for (int k = 1; k <= NGRAM_MAX; k++) {

            ArrayList<ArrayList<Integer>> ngrams = DeckGenerator.generateCombinations(k, NGRAM_MAX);

            for (ArrayList<Integer> ngram : ngrams) {

                // Get wins
                System.out.println("Computing wins");
                JavaPairRDD<String, Integer> wins = duelRDD
                        .mapToPair(duel -> {
                            String winnerDeck = DeckGenerator.choiceInDeck(duel.players.get(duel.winner).deck, ngram);
                            return new Tuple2<>(winnerDeck, 1);
                        })
                        .reduceByKey(Integer::sum);

                // Get losses
                System.out.println("Computing losses");
                JavaPairRDD<String, Integer> losses = duelRDD
                        .mapToPair(duel -> {
                            String loserDeck = DeckGenerator.choiceInDeck(duel.players.get(duel.winner == 0 ? 1 : 0).deck, ngram);
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
                        })
                        .filter((tuple) -> {
                            Tuple2<Integer, Integer> winrate = tuple._2;
                            return winrate._1 + winrate._2 >= GAMES;
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
                winrates.take(4).forEach(entry -> System.out.println("Final Winrate: " + entry._1 + " -> " + entry._2));

            }
            writeToFile(outputPath, k, winrates.collect());

        }

        System.out.println("OK !!!!!!!!!!!!");
        sc.close();
    }

    private static void writeToFile(String fileName, String key, List<Tuple2<String, Double>> winrateList) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(Paths.get(fileName), StandardOpenOption.APPEND),
                StandardCharsets.UTF_8))) {
            // Check if the file is empty to handle JSON structure correctly
            boolean isEmpty = Files.size(Paths.get(fileName)) == 0;

            if (isEmpty) {
                writer.write("{\n");
                writer.write("  \""+ key +"\": [\n");
            } else {
                // Remove the closing brackets from the existing JSON structure
                writer.write(",\n");
            }

            // Write each deck's winrate as a JSON object
            for (int i = 0; i < winrateList.size(); i++) {
                Tuple2<String, Double> entry = winrateList.get(i);
                writer.write("    {\n");
                writer.write("      \"deck\": \"" + entry._1 + "\",\n");
                writer.write("      \"winrate\": " + entry._2 + "\n");
                writer.write("    }");

                // Add a comma if it's not the last element
                if (i < winrateList.size() - 1) {
                    writer.write(",\n");
                } else {
                    writer.write("\n");
                }
            }

            writer.write("  ]\n");
            writer.write("}\n");

        } catch (IOException ex) {
            ex.printStackTrace(); // Print the stack trace to identify the issue
        }
    }

}
