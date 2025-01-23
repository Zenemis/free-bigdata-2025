//mvn package;~/spark-3.5.0-bin-hadoop3/bin/spark-submit --master local --deploy-mode client target/TPSpark-0.0.1.jar

/*
 * result
 * [Deck 08355b count: 1311.0 winrate: 64% nbplayers : 101 strengthW: 0.233451536643026, 
 *  Deck 082935 count: 1342.0 winrate: 64% nbplayers : 101 strengthW: 0.2571531791907514, 
 *  Deck 0c2137 count: 1441.0 winrate: 64% nbplayers : 101 strengthW: -0.008802816901408451, 
 *  Deck 2f444f count: 1111.0 winrate: 64% nbplayers : 101 strengthW: -0.008438818565400843, 
 *  Deck 2f4448 count: 1340.0 winrate: 63% nbplayers : 101 strengthW: 0.026848591549295774, 
 *  Deck 292f4f count: 1297.0 winrate: 63% nbplayers : 101 strengthW: 0.009784587378640778, Deck 2f484f count: 1105.0 winrate: 63% nbplayers : 101 strengthW: 0.00641025641025641, Deck 3d5a6d count: 1449.0 winrate: 63% nbplayers : 101 strengthW: 0.1800789760348584, Deck 08646b count: 1394.0 winrate: 63% nbplayers : 101 strengthW: 0.25736126840317103, Deck 0c2940 count: 1633.0 winrate: 63% nbplayers : 101 strengthW: 0.30680164888457806, Deck 022f6d count: 1147.0 winrate: 63% nbplayers : 101 strengthW: 0.271667817679558, Deck 08355e count: 1865.0 winrate: 63% nbplayers : 101 strengthW: 0.21659940526762958, Deck 122f6d count: 1075.0 winrate: 62% nbplayers : 101 strengthW: 0.19534711964549484, Deck 071637 count: 1589.0 winrate: 62% nbplayers : 101 strengthW: 0.2124248496993988, Deck 406265 count: 2575.0 winrate: 62% nbplayers : 101 strengthW: 0.3887600494743352, Deck 0b0c37 count: 7277.0 winrate: 62% nbplayers : 101 strengthW: 0.3228983998246383, Deck 0c3750 count: 2637.0 winrate: 62% nbplayers : 101 strengthW: 0.2405852994555354, Deck 0c2a2b count: 2301.0 winrate: 62% nbplayers : 101 strengthW: 0.17747395833333332, Deck 2a2b37 count: 1948.0 winrate: 62% nbplayers : 101 strengthW: 0.09336483155299918, Deck 071137 count: 4246.0 winrate: 62% nbplayers : 101 strengthW: 0.2523331447963801]
 */

package sparkwinrate;

import java.util.ArrayList;
import java.util.List;

import datacleaner.Battle;

import scala.Tuple2;

public class DeckGenerator {

	public static String choiceInDeck(String deck, List<Integer> elements) {
		if (deck.length() != 16) return null;
		if (elements.size() == 8) return deck;
		StringBuilder result = new StringBuilder();
		for (int index : elements) {
			result.append(deck.charAt(2*index));
			result.append(deck.charAt(2*index+1));
		}
		return result.toString();
	}

	public static ArrayList<ArrayList<Integer>> generateCombinations(int n, int k) {
	    ArrayList<ArrayList<Integer>> result = new ArrayList<>();
	    int[] combination = new int[k];
	    for (int i = 0; i < k; i++) {
	        combination[i] = i;
	    }
	    while (combination[k - 1] < n) {
	        ArrayList<Integer> currentCombination = new ArrayList<>();
	        for (int i : combination) {
	            currentCombination.add(i);
	        }
	        result.add(currentCombination);
	        int t = k - 1;
	        while (t != 0 && combination[t] == n - k + t) {
	            t--;
	        }
	        combination[t]++;
	        for (int i = t + 1; i < k; i++) {
	            combination[i] = combination[i - 1] + 1;
	        }
	    }
	    return result;
	}
}