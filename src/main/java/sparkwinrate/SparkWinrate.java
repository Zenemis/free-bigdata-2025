package sparkwinrate;

import datacleaner.Battle;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

import static sparkwinrate.DeckGenerator.generateCombinations;
import static sparkwinrate.DeckGenerator.treatCombination;

public class SparkWinrate {
    /*
     * Read battles from the master data sets then compute statistics for each deck.
     * Subdecks can also be computed (1 2 3 4 ... ) cards by passing the argument.
     * Each time statistics about the best evolution and best towers card are
     * provided.
     */
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis(); // Start timing

        final int[] CARDSGRAMS = { 4, 6, 7, 8 };
        final int[] CARDSCOMBI = { 8, 28, 56, 70, 56, 28, 8, 1 };

        String inputPath = args[0];
        String outputPath = args[1];

        ArrayList<ArrayList<ArrayList<Integer>>> combs = new ArrayList<>();

        for (int k : CARDSGRAMS) {
            combs.add(generateCombinations(8, k));
        }

        SparkConf conf = new SparkConf().setAppName("Winrate Calculator");
        JavaSparkContext sc = new JavaSparkContext(conf);
        DataReader dataReader = new DataReader(inputPath);

        JavaRDD<Battle> clean = dataReader.getDistinctBattles(sc);

        JavaPairRDD<String, Deck> rdddecks = clean.flatMapToPair((x) -> {
            if (x.players.get(0).deck.length() != 16 || x.players.get(1).deck.length() != 16
                    || (Math.abs(x.players.get(0).strength - x.players.get(1).strength)) > 0.75
                    || x.players.get(0).touch != 1 || x.players.get(1).touch != 1
            )
                return Collections.emptyIterator();

            if ((x.players.get(0).bestleague < 6 || x.players.get(1).bestleague < 6))
                return Collections.emptyIterator();

            ArrayList<String> tmp1 = new ArrayList<>();
            for (int i = 0; i < 8; ++i)
                tmp1.add(x.players.get(0).deck.substring(i * 2, i * 2 + 2));
            ArrayList<String> tmp2 = new ArrayList<>();
            for (int i = 0; i < 8; ++i)
                tmp2.add(x.players.get(1).deck.substring(i * 2, i * 2 + 2));
            ArrayList<Tuple2<String, Deck>> res = new ArrayList<>();
            for (ArrayList<ArrayList<Integer>> aa : combs)
                for (ArrayList<Integer> cmb : aa)
                    treatCombination(x, tmp1, tmp2, res, cmb);
            return res.iterator();
        }).cache();

        System.out.println("rdd decks generated : " + rdddecks.count());

        final int PLAYERS = 10;
        final int BATTLES = 80;

        JavaRDD<Deck> stats = rdddecks.reduceByKey(Deck::merge).values()
                .filter((Deck x) -> x.players.size() >= PLAYERS && x.count >= BATTLES);

        System.out.println("rdd decks reduced : " + stats.count());

        ArrayList<JavaRDD<Deck>> statistics = new ArrayList<>();
        for (int cn : CARDSGRAMS) {
            statistics.add(stats.filter((Deck x) -> x.id.length() / 2 == cn));
        }

        final int NB_DECKS = 100000;

        try (
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputPath), StandardCharsets.UTF_8)
        ) {
            writer.write("{\n");

            boolean firsta = true;

            for (int i = 0; i < CARDSGRAMS.length; ++i) {
                System.out.println("kgram " + i + " > " + statistics.get(i).count());
            }

            for (int i = 0; i < CARDSGRAMS.length; ++i) {
                if (!firsta)
                    writer.write(",\n");
                firsta = false;
                writer.write("\"" + CARDSGRAMS[i] + "\": {\n");
                writer.write("\"cards\":" + combs.get(i) + ",\n");
                writer.write("\"decks\":[\n");
                boolean first = true;
                for (Deck d : statistics.get(i).top(NB_DECKS, new WinrateComparator())) {
                    if (!first)
                        writer.write(",\n");
                    first = false;
                    writer.write((d.toString()).replace("'", "\""));
                }
                writer.write("]\n}");
            }
            writer.write("}\n");
            System.out.println("Job has finished with no issues");
        } catch (IOException ex) {
            System.err.println("ERROR: " + ex.getMessage());
        }

        File outputFile = new File(outputPath);
        if (outputFile.exists() && !outputFile.isDirectory()) {
            System.out.println("Fichier de sortie créé avec succès : " + outputFile.getAbsolutePath());
        } else {
            System.err.println("Échec de la création du fichier de sortie.");
        }

        sc.close();

        long endTime = System.currentTimeMillis(); // End timing
        System.out.println("Durée d'exécution : " + (endTime - startTime) + " ms");
    }
}
