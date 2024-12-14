package crtracker;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public class Player implements WritableComparable<Player> {
	public String utag;
	public String ctag;
	public int trophies;
	public int ctrophies = 0;
	public int exp;
	public int league;
	public int bestleague;
	public long deck;
	public String evo;
	public String tower = "6e";
	public double strength;
	public int crown;
	public double elixir;
	public int touch;
	public int score;

	@JsonProperty("deck")
	public void setDeck(String deck) {
		if (deck == null || deck.length() != 16) {
			throw new IllegalArgumentException("Invalid deck: must be non-null and at least 16 characters long.");
		}
		this.deck = Long.parseLong(deck, 16); // Converts the hexadecimal string to a long
	}

	@Override
	public String toString() {
		return "Player [utag=" + utag +"]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(utag, ctag, evo, tower, deck, score, touch, elixir, strength, crown, league, bestleague, exp, trophies, ctrophies);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Player)) return false;
		Player player = (Player) o;
		return trophies == player.trophies
				&& ctrophies == player.ctrophies
				&& exp == player.exp
				&& league == player.league
				&& bestleague == player.bestleague
				&& Double.compare(strength, player.strength) == 0
				&& crown == player.crown
				&& Double.compare(elixir, player.elixir) == 0
				&& touch == player.touch
				&& score == player.score
				&& deck == player.deck
				&& Objects.equals(utag, player.utag)
				&& Objects.equals(ctag, player.ctag)
				&& Objects.equals(evo, player.evo)
				&& Objects.equals(tower, player.tower);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(utag);
		out.writeUTF(ctag == null ? "" : ctag);
		out.writeInt(trophies);
		out.writeInt(ctrophies);
		out.writeInt(exp);
		out.writeInt(league);
		out.writeInt(bestleague);
		out.writeLong(deck);
		out.writeUTF(evo != null ? evo : "");
		out.writeUTF((tower != null && !tower.isEmpty()) ? tower : "6e");
		out.writeDouble(strength);
		out.writeInt(crown);
		out.writeDouble(elixir);
		out.writeInt(touch);
		out.writeInt(score);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		utag = in.readUTF();
		ctag = in.readUTF();
		trophies = in.readInt();
		ctrophies = in.readInt();
		exp = in.readInt();
		league = in.readInt();
		bestleague = in.readInt();
		deck = in.readLong();
		evo = in.readUTF();
		tower = in.readUTF();
		strength = in.readDouble();
		crown = in.readInt();
		elixir = in.readDouble();
		touch = in.readInt();
		score = in.readInt();
	}

	@Override
	public int compareTo(Player o) {
		return this.equals(o) ? 0 : ctag.compareTo(o.ctag);
	}

	public boolean isValid(){
		if (touch == 0) return false;
		if (evo == null || evo.length() > 2*2) return false;
		return true;
	}

}