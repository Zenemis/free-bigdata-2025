package datacleaner;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

import org.apache.hadoop.io.WritableComparable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Player implements WritableComparable<Player> {
	static private final short EMPTY_EVO = Short.MIN_VALUE;

	public String utag;
	public String ctag;
	public int trophies;
	public int ctrophies = 0;
	public int exp;
	public int league;
	public int bestleague;
	public long deck;
	public short evo = EMPTY_EVO;
	public byte tower = 0x6e;
	public double strength;
	public int crown;
	public double elixir;
	public int touch;
	public int score;

	@JsonProperty("deck")
	public void setDeck(String deck) {
		if (deck == null || deck.length() != 16) {
			throw new IllegalArgumentException("Invalid deck: must be non-null and 16 characters long.");
		}
		this.deck = Long.parseLong(deck, 16); // Converts the hexadecimal string to a long
	}

	@JsonProperty("evo")
	public void setEvo(String evo) {
		if (evo == null || evo.length() > 4) {
			throw new IllegalArgumentException("Invalid evo: must be non-null and at most 4 characters long.");
		}
		this.evo = (short) (Short.parseShort(evo, 16) & EMPTY_EVO);
	}

	@JsonProperty
	public void setTower(String tower) {
		if (tower == null || (!tower.isEmpty() && tower.length() != 2)) {
			throw new IllegalArgumentException("Invalid tower: must be non-null and at most 2 characters long.");
		}
		this.tower = Byte.parseByte(tower, 16);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Player{");
		sb.append("utag='").append(utag).append('\'');
		sb.append(", ctag='").append(ctag).append('\'');
		sb.append(", trophies=").append(trophies);
		sb.append(", ctrophies=").append(ctrophies);
		sb.append(", exp=").append(exp);
		sb.append(", league=").append(league);
		sb.append(", bestleague=").append(bestleague);
		sb.append(", deck=").append(deck);
		sb.append(", evo='").append(evo).append('\'');
		sb.append(", tower='").append(tower).append('\'');
		sb.append(", strength=").append(strength);
		sb.append(", crown=").append(crown);
		sb.append(", elixir=").append(elixir);
		sb.append(", touch=").append(touch);
		sb.append(", score=").append(score);
		sb.append('}');
		return sb.toString();
	}

	@Override
	public int hashCode() {
		// Bien calculer le hashcode
		return Objects.hash(utag, ctag, evo, tower, deck, score, touch, elixir, strength, crown, league, bestleague, exp, trophies, ctrophies);
	}

	@Override
	public int compareTo(Player o) {
		if (o == null) {
			throw new NullPointerException("Cannot compare with null.");
		}
		if (this == o) return 0;

		// Compare by unique tag (utag, lexicographically)
		int cmp = this.utag.compareTo(o.utag);
		if (cmp != 0) return cmp;

		// Compare by clan tag (ctag, lexicographically)
		cmp = this.ctag.compareTo(o.ctag);
		if (cmp != 0) return cmp;

		// Compare trophies (higher trophies come first)
		cmp = Integer.compare(o.trophies, this.trophies);
		if (cmp != 0) return cmp;

		// Compare experience points (higher experience comes first)
		cmp = Integer.compare(o.exp, this.exp);
		if (cmp != 0) return cmp;

		// Compare league (higher league comes first)
		cmp = Integer.compare(o.league, this.league);
		if (cmp != 0) return cmp;

		// Compare crown count (higher crown count comes first)
		cmp = Integer.compare(o.crown, this.crown);
		if (cmp != 0) return cmp;

		// Compare elixir usage (lower elixir usage comes first)
		cmp = Double.compare(this.elixir, o.elixir);
		return cmp;
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
		out.writeShort(evo);
		out.writeByte(tower);
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
		evo = in.readShort();
		tower = in.readByte();
		strength = in.readDouble();
		crown = in.readInt();
		elixir = in.readDouble();
		touch = in.readInt();
		score = in.readInt();
	}

	public boolean isValid(){
		return (touch != 0);
	}

}