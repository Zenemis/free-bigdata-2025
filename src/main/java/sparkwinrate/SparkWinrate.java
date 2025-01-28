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

import datacleaner.Player;
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

        int BATTLES = 80;
        int PLAYERS = 10;
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

        // Triangle de Pascal : 1, 7, 21, 35, 35, 21, 7, 1
        // Génération (fainéante) de tous les RDD
        JavaPairRDD<String, Deck>[] results = new JavaPairRDD[8];
        for (int i = 1; i <= NGRAM_MAX; i++) {
            ArrayList<ArrayList<Integer>> ngrams = DeckGenerator.generateCombinations(NGRAM_MAX, i);
            results[i-1] = processNgram(ngrams, duelRDD, i);
        }

        for (int i = 1; i <= NGRAM_MAX; i++) {
            System.out.println("Processing: " + Integer.toString(i));
            ArrayList<ArrayList<Integer>> ngrams = DeckGenerator.generateCombinations(NGRAM_MAX, i);
            List<Deck> collected = results[i-1]
                    .values()
                    .filter((Deck x) -> x.players.size() >= PLAYERS && x.count >= BATTLES)
                    .collect();
            writeToFile(outputPath, i, ngrams, collected, i == NGRAM_MAX);
        }

        long endTime = System.currentTimeMillis(); // End timer
        System.out.println("Total execution time: " + (endTime - startTime) + " milliseconds");

        System.out.println("OK !!!!!!!!!!!!");
        sc.close();
    }


    private static JavaPairRDD<String, Deck> processNgram(
            List<ArrayList<Integer>> ngrams,
            JavaRDD<Battle> duelRDD,
            int ngramIndex
    ) {

        // Compute winrates as "Deck"
        JavaPairRDD<String, Deck> deckWR = duelRDD
                .flatMapToPair(duel -> {
                    List<Tuple2<String, Deck>> deckPair = new ArrayList<>();
                    for (ArrayList<Integer> ngram : ngrams) {

                        int winIndex = duel.winner;
                        Player winner = duel.players.get(winIndex);
                        String winNgram = DeckGenerator.choiceInDeck(winner.deck, ngram);

                        int loseIndex = duel.winner == 0 ? 1 : 0;
                        Player loser = duel.players.get(loseIndex);
                        String loseNgram = DeckGenerator.choiceInDeck(loser.deck, ngram);

                        double strengthDelta = winner.strength - loser.strength;

                        if (winNgram != null)
                            deckPair.add(new Tuple2<>(winNgram, Deck.fromPlayer(winNgram, winner, strengthDelta, true)));
                        if (loseNgram != null)
                            deckPair.add(new Tuple2<>(winNgram, Deck.fromPlayer(loseNgram, loser, -strengthDelta, false)));
                    }
                    return deckPair.iterator();
                })
                .reduceByKey(Deck::merge);

        return deckWR;
    }



    private static void writeToFile(
            String fileName,
            int ngramIndex,
            List<ArrayList<Integer>> ngrams, // Changed from ArrayList<ArrayList<Integer>>
            List<Deck> winrateList,
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
            writer.write("\""+ Integer.toString(ngramIndex) +"\": {\n");
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
                Deck entry = winrateList.get(i);
                writer.write(entry.toString());
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
