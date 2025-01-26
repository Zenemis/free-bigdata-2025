package sparkwinrate;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import datacleaner.Battle;
import scala.Tuple2;

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

        long startTime = System.currentTimeMillis(); // Start timer

        // Group 1: Lightweight n-grams [1, 2, 7, 8]
        processGroupedNgrams(new int[]{2, 7}, duelRDD, GAMES, outputPath);
        
        processGroupedNgrams(new int[]{3, 8}, duelRDD, GAMES, outputPath);

        // Group 2: Moderate n-grams [3, 6]
        processGroupedNgrams(new int[]{1, 6}, duelRDD, GAMES, outputPath);

        // Group 3: Heavyweight n-grams [4]
        processNgramIndividually(4, duelRDD, GAMES, outputPath);

        // Group 4: Heavyweight n-grams [5]
        processNgramIndividually(5, duelRDD, GAMES, outputPath);

        long endTime = System.currentTimeMillis(); // End timer
        System.out.println("Total execution time: " + (endTime - startTime) + " milliseconds");

        System.out.println("OK !!!!!!!!!!!!");
        sc.close();
    }

    private static void processGroupedNgrams(
            int[] ks,
            JavaRDD<Battle> duelRDD,
            int GAMES,
            String outputPath) {

        StringBuilder groupName = new StringBuilder("Ngram_");
        ArrayList<ArrayList<Integer>> ngrams = new ArrayList<>();

        for (int k : ks) {
            groupName.append(k).append("_");
            ngrams.addAll(DeckGenerator.generateCombinations(8, k));
        }

        // Remove the trailing underscore
        groupName.setLength(groupName.length() - 1);

        System.out.println("Processing grouped n-grams: " + groupName);

        processNgrams(ngrams, duelRDD, GAMES, outputPath, groupName.toString());
    }

    private static void processNgramIndividually(
            int k,
            JavaRDD<Battle> duelRDD,
            int GAMES,
            String outputPath) {

        System.out.println("Processing individual n-gram: " + k);

        ArrayList<ArrayList<Integer>> ngrams = DeckGenerator.generateCombinations(8, k);
        processNgrams(ngrams, duelRDD, GAMES, outputPath, "Ngram_" + k);
    }


    private static void processNgrams(
            List<ArrayList<Integer>> ngrams,
            JavaRDD<Battle> duelRDD,
            int GAMES,
            String outputPath,
            String ngramName) {

        System.out.println("Processing: " + ngramName);

        // Compute wins
        JavaPairRDD<String, Integer> wins = duelRDD
                .flatMapToPair(duel -> {
                    List<Tuple2<String, Integer>> winPairs = new ArrayList<>();
                    for (ArrayList<Integer> ngram : ngrams) {
                        int winner = duel.winner;
                        String deck = duel.players.get(winner).deck;
                        String winnerDeck = DeckGenerator.choiceInDeck(deck, ngram);
                        if (winnerDeck != null) winPairs.add(new Tuple2<>(winnerDeck, 1));
                    }
                    return winPairs.iterator();
                })
                .reduceByKey(Integer::sum);

        // Compute losses
        JavaPairRDD<String, Integer> losses = duelRDD
                .flatMapToPair(duel -> {
                    List<Tuple2<String, Integer>> lossPairs = new ArrayList<>();
                    for (ArrayList<Integer> ngram : ngrams) {
                        int loser = duel.winner == 0 ? 1 : 0;
                        String deck = duel.players.get(loser).deck;
                        String loserDeck = DeckGenerator.choiceInDeck(deck, ngram);
                        if (loserDeck != null) lossPairs.add(new Tuple2<>(loserDeck, 1));
                    }
                    return lossPairs.iterator();
                })
                .reduceByKey(Integer::sum);

        // Compute winrates
        JavaPairRDD<String, Tuple2<Integer, Integer>> winratesStruct = wins
                .fullOuterJoin(losses)
                .mapValues(tuple -> {
                    int winsCount = tuple._1.orElse(0);
                    int lossesCount = tuple._2.orElse(0);
                    return new Tuple2<>(winsCount, lossesCount);
                })
                .filter((tuple) -> tuple._2._1 + tuple._2._2 >= GAMES);

        JavaPairRDD<String, Double> winrates = winratesStruct
                .mapValues(tuple -> {
                    int winsCount = tuple._1;
                    int lossesCount = tuple._2;
                    return lossesCount == 0 ? 1.0 : (double) winsCount / (winsCount + lossesCount);
                })
                .cache();

        // Log and write results
        winrates.take(4).forEach(entry -> System.out.println("Winrate: " + entry._1 + " -> " + entry._2));

        List<Tuple2<String, Double>> collected = winrates.collect();

        // Explicit cast to ArrayList<ArrayList<Integer>> for compatibility
        writeToFile(outputPath, ngramName, (ArrayList<ArrayList<Integer>>) ngrams, collected, false);
    }



    private static void writeToFile(
            String fileName,
            String ngramName,
            List<ArrayList<Integer>> ngrams, // Changed from ArrayList<ArrayList<Integer>>
            List<Tuple2<String, Double>> winrateList,
            boolean close) {
        Path path = Paths.get(fileName);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND),
                StandardCharsets.UTF_8))) {
            // Check if the file is empty to handle JSON structure correctly
            boolean isEmpty = Files.size(path) == 0;

            // Opens main object
            if (isEmpty) {
                writer.write("{\n");
            }

            // Write "header" part
            writer.write("\""+ ngramName +"\": {\n");
            writer.write("\"cards\": [");

            for (int i = 0; i < ngrams.size(); i++) {
                ArrayList<Integer> ngram = ngrams.get(i);
                writer.write(ngram.toString());
                if (i < ngrams.size() - 1) {
                    writer.write(",");
                }
            }
            writer.write("],\n");

            // Write start of decks
            writer.write("\"decks\" : [");

            // Write each deck's winrate as a JSON object
            for (int i = 0; i < winrateList.size(); i++) {
                Tuple2<String, Double> entry = winrateList.get(i);
                writer.write("{\"id\": \"" + entry._1 + "\", \"winrate\"" + entry._2 + "}");
                // Add a comma if it's not the last element
                if (i < winrateList.size() - 1) {
                    writer.write(",\n");
                } else {
                    writer.write("\n");
                }
            }
            // Write end of decks
            writer.write("]\n");

            // Write end of Ngram
            writer.write("}\n");

            if (close) {
                writer.write("}\n");
            }

        } catch (IOException ex) {
            ex.printStackTrace(); // Print the stack trace to identify the issue
        }
    }

}
