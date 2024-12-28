package datacleaner;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * La classe BattleData représente une bataille avec ses détails associés,
 * tels que la date, le gagnant, le jeu, le mode, le round, le type, les joueurs et le clan de guerre.
 * Elle implémente l'interface WritableComparable pour être utilisée dans un contexte Hadoop.
 */
public class Battle implements WritableComparable<Battle> {

	// Attributs représentant les données d'une bataille
	public Instant date; // La date de la bataille, représentée par Instant
	@JsonProperty("date")
	public void setDate(String dateStr) {
		try {
			this.date = Instant.parse(dateStr);
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Invalid date format: " + dateStr, e);
		}
	}
	public int winner;
	public String game;
	public String mode;
	public int round;
	public String type;
	public List<Player> players;
	@JsonProperty("players")
	public void setPlayers(List<Player> players) {
		if (players == null || players.size() != 2) {
			throw new IllegalArgumentException("Invalid players list: must contain exactly 2 players");
		}
		players.sort(Player::compareTo);
		this.players = players;
	}
	public WarClan warclan;

	/**
	 * Retourne une représentation sous forme de chaîne de caractères de l'objet BattleData.
	 *
	 * @return La chaîne de caractères représentant les données de la bataille
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Battle{");
		sb.append("date=").append(date);
		sb.append(", game='").append(game).append('\'');
		sb.append(", mode='").append(mode).append('\'');
		sb.append(", round=").append(round);
		sb.append(", type='").append(type).append('\'');
		sb.append(", winner=").append(winner);
		sb.append(", warclan=").append(warclan);
		sb.append('}');
		return sb.toString();
	}

	/**
	 * Retourne le code de hachage pour l'objet BattleData.
	 *
	 * Hadoop se repose en grande partie sur cette méthode pour calculer la
	 * bataille en tant que "clé", ce qui en fait une méthode de "pré-filtrage" :
	 * Hadoop calcule les hashCode de 2 objets, et s'ils ont un hashCode différent,
	 * ils sont considérés comme différents. Sinon, "eauals" est appelée.
	 *
	 * C'est avec ce mécanisme de comparaison rapide et d'insertion en clés que
	 * Hadoop supprime les doublons.
	 *
	 * @return Le code de hachage calculé
	 */
	@Override
	public int hashCode() {
		return Objects.hash(game, mode, round, type, players, warclan);
	}

	/**
	 * Compare cet objet BattleData à un autre BattleData. La comparaison est à comprendre au sens
	 * de la relation d'ordre.
	 *
	 * @param o L'objet BattleData à comparer
	 * @return Un entier indiquant le résultat de la comparaison
	 */
	@Override
	public int compareTo(Battle o) {
		if (o == null) {
			throw new NullPointerException("Cannot compare with null.");
		}
		// Compare by date
		long difference = Math.abs(date.getEpochSecond() - o.date.getEpochSecond());
		if (difference > 10)
			return date.compareTo(o.date);

		// Compare by game
		int gameComparison = game.compareTo(o.game);
		if (gameComparison != 0) {
			return gameComparison;
		}
		// Compare by mode
		int modeComparison = mode.compareTo(o.mode);
		if (modeComparison != 0) {
			return modeComparison;
		}
		// Compare by round
		int roundComparison = Integer.compare(round, o.round);
		if (roundComparison != 0) {
			return roundComparison;
		}
		// Compare by type
		int typeComparison = type.compareTo(o.type);
		if (typeComparison != 0) {
			return typeComparison;
		}
		// Compare by players
		int playersComparison = 0;
		for (int i=0 ; i<players.size() ; i++) {
			playersComparison = players.get(i).compareTo(o.players.get(i));
			if (playersComparison != 0) return playersComparison;
		}

		// Compare by warclan
		return warclan.compareTo(o.warclan);
	}

	/**
	 * Vérifie si cet objet BattleData est égal à un autre objet.
	 * L'égalité est déterminée en fonction de tous les attributs de la bataille,
	 * à l'exception de la date (comparée à une différence de 10 secondes).
	 *
	 * @param o L'objet à comparer
	 * @return true si les objets sont égaux, false sinon
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Battle battle = (Battle) o;
		if (round != battle.round) return false;
		if (!Objects.equals(mode, battle.mode)) return false;
		if (!Objects.equals(game, battle.game)) return false;
		if (!Objects.equals(type, battle.type)) return false;

		long difference = Math.abs(date.getEpochSecond() - battle.date.getEpochSecond());
		if (difference > 10)
			return false;

		if (!Objects.equals(players.get(winner), battle.players.get(battle.winner))) return false;
		if (!Objects.equals(players.get(battle.winner), battle.players.get(winner))) return false;
		if (!Objects.equals(warclan, battle.warclan)) return false;
		return true;
	}

	/**
	 * Écrit les données de l'objet BattleData dans un flux de sortie.
	 *
	 * @param out Le flux de sortie dans lequel écrire les données
	 * @throws IOException Si une erreur d'écriture se produit
	 */
	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(date.toString());
		out.writeInt(winner);
		out.writeUTF(game);
		out.writeUTF(mode);
		out.writeInt(round);
		out.writeUTF(type);
		out.writeInt(players.size());
		for (Player player : players) {
			player.write(out);
		}
		if (warclan != null)
			warclan.write(out);
		else
			WarClan.writeEmpty(out);
	}

	/**
	 * Lit les données d'un objet BattleData depuis un flux d'entrée.
	 *
	 * @param in Le flux d'entrée à partir duquel lire les données
	 * @throws IOException Si une erreur de lecture se produit
	 */
	@Override
	public void readFields(DataInput in) throws IOException {
		date = Instant.parse(in.readUTF());
		winner = in.readInt();
		game = in.readUTF();
		mode = in.readUTF();
		round = in.readInt();
		type = in.readUTF();
		players = new ArrayList<>();
		int size = in.readInt();
		for (int i = 0; i < size; i++) {
			Player player = new Player();
			player.readFields(in);
			players.add(player);
		}
		warclan = new WarClan();
		warclan.readFields(in); // Assuming WarClanData implements Writable
	}

	/**
	 * Vérifie si les données de la bataille sont valides.
	 * La validité est définie par la présence des informations essentielles et la validité des joueurs et du clan de guerre.
	 *
	 * @return true si les données sont valides, false sinon
	 */
	public boolean isValid() {
		if (date == null || game == null || mode == null || type == null) return false;
		if (players == null || players.size() < 2) return false; // Assuming players has at least 2 entries
		for (Player player : players) {
			if (!player.isValid()) return false;
		}
		if (warclan != null && !warclan.isValid()) return false;
		return true;
	}
}
