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

import datacleaner.Battle;

import scala.Tuple2;

public class DeckGenerator {

	public static ArrayList<ArrayList<Integer>> generateCombinations(int n, int k) {
		ArrayList<Integer> elements = new ArrayList<Integer>();
		for (int x = 0; x < n; ++x)
			elements.add(x);

		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
		generate(new ArrayList<Integer>(), 0, elements, k, result);
		return result;
	}

	private static void generate(ArrayList<Integer> current, int start, ArrayList<Integer> elements, int k,
			ArrayList<ArrayList<Integer>> result) {
		if (current.size() == k) {
			result.add(new ArrayList<>(current));
			return;
		}
		for (int i = start; i < elements.size(); i++) {
			current.add(elements.get(i));
			generate(current, i + 1, elements, k, result);
			current.remove(current.size() - 1);
		}
	}
/*
{"date":"2024-09-26T11:29:09Z","game":"gdc","mode":"CW_Battle_1v1","round":0,"type":"riverRacePvP","winner":1,"players":[{"utag":"#YLUGLG29L","ctag":"#YCCQULJ0","trophies":9000,"ctrophies":5066,"exp":57,"league":8,"bestleague":9,"deck":"000b2f3138565a5e","evo":"0b","tower":"","strength":14.4375,"crown":1,"elixir":1.01,"touch":1,"score":100},{"utag":"#JU0VQRQU","ctag":"#YC8R0RJ0","trophies":9000,"ctrophies":5442,"exp":63,"league":9,"bestleague":9,"deck":"0d25374045596062","evo":"3740","tower":"","strength":15.25,"crown":2,"elixir":1.48,"touch":1,"score":200}],"warclan":{"day":3,"hour_seg":0,"period":"112-3","training":[false,false]}}
{"date":"2024-09-26T11:29:08Z","game":"gdc","mode":"CW_Battle_1v1","round":0,"type":"riverRacePvP","winner":0,"players":[{"utag":"#JU0VQRQU","ctag":"#YC8R0RJ0","trophies":9000,"ctrophies":5442,"exp":63,"league":9,"bestleague":9,"deck":"0d25374045596062","evo":"3740","tower":"","strength":15.25,"crown":2,"elixir":1.48,"touch":1,"score":200},{"utag":"#YLUGLG29L","ctag":"#YCCQULJ0","trophies":9000,"ctrophies":5066,"exp":57,"league":8,"bestleague":9,"deck":"000b2f3138565a5e","evo":"0b","tower":"","strength":14.4375,"crown":1,"elixir":1.01,"touch":1,"score":100}],"warclan":{"day":3,"hour_seg":0,"period":"112-3","training":[false,false]}}
*/
	/*
	 * Read battles from the master data sets then compute statistics for each decks
	 * subdecks can be also computes (1 2 3 4 ... ) cards by passing the argument.
	 * each time statistics about the best evolution and best towers card are
	 * provided
	 */

	public static void treatCombination(Battle x, ArrayList<String> tmp1, ArrayList<String> tmp2,
										 ArrayList<Tuple2<String, Deck>> res, ArrayList<Integer> cmb) {
		StringBuilder c1 = new StringBuilder();
		StringBuilder c2 = new StringBuilder();
		for (int i : cmb) {
			c1.append(tmp1.get(i));
			c2.append(tmp2.get(i));
		}
		Deck d1;
		Deck d2;
	if (cmb.size() == 8) {		
		d1 = new Deck(c1.toString(), x.players.get(0).evo, x.players.get(0).tower, 1, x.winner,
				x.players.get(0).strength - x.players.get(1).strength, x.players.get(0).utag,
				x.players.get(0).league, x.players.get(0).ctrophies);
		d2 = new Deck(c2.toString(), x.players.get(1).evo, x.players.get(1).tower, 1, 1 - x.winner,
				x.players.get(1).strength - x.players.get(0).strength, x.players.get(1).utag,
				x.players.get(1).league, x.players.get(1).ctrophies);
	}
	else {
		d1 = new Deck(c1.toString(), "", "", 1, x.winner,
				x.players.get(0).strength - x.players.get(1).strength, x.players.get(0).utag,
				x.players.get(0).league, x.players.get(0).ctrophies);
		d2 = new Deck(c2.toString(), "", "", 1, 1 - x.winner,
				x.players.get(1).strength - x.players.get(0).strength, x.players.get(1).utag,
				x.players.get(1).league, x.players.get(1).ctrophies);
	}
		res.add(new Tuple2<>(d1.id, d1));
		res.add(new Tuple2<>(d2.id, d2));
	}

}