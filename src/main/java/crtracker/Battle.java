package crtracker;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.hadoop.io.WritableComparable;

/*
{"date":"2024-09-13T07:27:05Z","game":"gdc","mode":"Rage_Ladder","round":0,"type":"riverRacePvP",
"winner":0,
"players":[{"utag":"#C82LPR8J","ctag":"#QQ9QGJYJ","trophies":9000,"ctrophies":1100,"exp":67,"league":6,
"bestleague":10,
"deck":"0a18323d4a4c616b",
"evo":"0a18",
"tower":"",
"strength":15.375,
"crown":2,
"elixir":1.56,
"touch":1,
"score":200},
{"utag":"#9UC2GUJVP","ctag":"#QQJCR9CP","trophies":7160,"ctrophies":1012,"exp":46,"league":1,"bestleague":4,"deck":"05070c14171f445e","evo":"","tower":"","strength":13,"crown":0,"elixir":6.14,"touch":1,"score":100}],"warclan":{"day":3,"hour_seg":3,"period":"112-1","training":[false,false]}}
*/

public class Battle implements WritableComparable<Battle> {
	public Instant date;
	public String game;
	public String mode;
	public int round;
	public String type;
	public int winner;
	public Player player1;
	public Player player2;
	public WarClan warclan;

	@JsonProperty("date")
	public void setDate(String dateStr) {
		try {
			this.date = Instant.parse(dateStr);
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Invalid date format: " + dateStr, e);
		}
	}

	@JsonProperty("players")
	public void setPlayers(List<Player> players) {
		if (players == null || players.size() != 2) {
			throw new IllegalArgumentException("Invalid players list: must contain exactly 2 players");
		}
		this.player1 = players.get(0);
		this.player2 = players.get(1);
	}


	@Override
	public int hashCode() {
		return Objects.hash(date, game, mode, round, type, winner, player1, player2, warclan);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Battle battle = (Battle) o;
		if (round != battle.round) return false;
		if (!Objects.equals(game, battle.game)) return false;
		if (!Objects.equals(type, battle.type)) return false;
		if (!Objects.equals(mode, battle.mode)) return false;
		if (!Objects.equals(warclan, battle.warclan)) return false;

		// Compare the date with a tolerance of 10 seconds
		try {
			long difference = Math.abs(date.getEpochSecond() - battle.date.getEpochSecond());
			if (difference > 10) return false;
		} catch (Exception e) {
			// Handle parsing errors (if any)
			e.printStackTrace();
			return false;
		}

		if (!Objects.equals(player1, battle.player2)) return false;
		if (!Objects.equals(player2, battle.player1)) return false;

		return true;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(date.toString());
		out.writeUTF(game);
		out.writeUTF(mode);
		out.writeInt(round);
		out.writeUTF(type);
		out.writeInt(winner);
		player1.write(out);
		player2.write(out);
		if (warclan != null)
			warclan.write(out);
		else
			WarClan.writeEmpty(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		date = Instant.parse(in.readUTF());
		game = in.readUTF();
		mode = in.readUTF();
		round = in.readInt();
		type = in.readUTF();
		winner = in.readInt();
		player1 = new Player();
		player1.readFields(in);
		player2 = new Player();
		player2.readFields(in);
		warclan = new WarClan();
		warclan.readFields(in);
	}

	@Override
	public int compareTo(Battle o) {
		return this.equals(o) ? 0 : (this.date.compareTo(o.date));
	}

	public boolean isValid() {
		if (date == null || game == null || mode == null || type == null) return false;
		if (player1 == null || player2 == null) return false;
		if (!player1.isValid() || !player2.isValid()) return false;
		if (warclan != null && !warclan.isValid()) return false;
		return true;
	}
}
