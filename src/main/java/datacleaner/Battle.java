package datacleaner;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.time.Instant;
import java.util.Objects;

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
	public Player winner;
	public Player loser;
	public int warclan;

	public Battle() {}

	public Battle(BattleJson battleJson) {
		this.date = Instant.parse(battleJson.date);
		this.game = battleJson.game;
		this.mode = battleJson.mode;
		this.round = battleJson.round;
		this.type = battleJson.type;
		if (battleJson.players == null || battleJson.players.size() != 2) {
			throw new IllegalArgumentException("Invalid players list: must contain exactly 2 players");
		}
		this.winner = battleJson.players.get(battleJson.winner);
		this.loser = battleJson.players.get(Math.abs(battleJson.winner-1));
		warclan = battleJson.warclan == null ? 0 : battleJson.warclan.toInt();
	}

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
		sb.append(", loser=").append(loser);
		sb.append(", warclan=").append(warclan);
		sb.append('}');
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(game, mode, round, type, winner, loser, warclan);
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

		if (!Objects.equals(winner, battle.winner)) return false;
		if (!Objects.equals(loser, battle.loser)) return false;

		return true;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(date.toString());
		out.writeUTF(game);
		out.writeUTF(mode);
		out.writeInt(round);
		out.writeUTF(type);
		winner.write(out);
		loser.write(out);
		out.writeInt(warclan);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		date = Instant.parse(in.readUTF());
		game = in.readUTF();
		mode = in.readUTF();
		round = in.readInt();
		type = in.readUTF();
		winner = new Player();
		winner.readFields(in);
		loser = new Player();
		loser.readFields(in);
		warclan = in.readInt();
	}

	@Override
	public int compareTo(Battle o) {
		return this.equals(o) ? 0 : (this.date.compareTo(o.date));
	}

	public boolean isValid() {
		if (date == null || game == null || mode == null || type == null) return false;
		if (winner == null || loser == null) return false;
		if (!winner.isValid() || !loser.isValid()) return false;
		return true;
	}
}
