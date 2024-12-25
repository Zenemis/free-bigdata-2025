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
	public int gameModeType;
	public int round;
	public Player winner;
	public Player loser;
	public int warclan;

	public Battle() {}

	public Battle(BattleJson battleJson) {
		this.date = Instant.parse(battleJson.date);
		this.gameModeType = Objects.hash(battleJson.game, battleJson.mode, battleJson.type);
		this.round = battleJson.round;
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
		sb.append(", gameModeType='").append(gameModeType).append('\'');
		sb.append(", round=").append(round);
		sb.append(", winner=").append(winner);
		sb.append(", loser=").append(loser);
		sb.append(", warclan=").append(warclan);
		sb.append('}');
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(gameModeType, round, winner, loser, warclan);
	}

	@Override
	public int compareTo(Battle o) {
		if (o == null) {
			throw new NullPointerException("Cannot compare with null.");
		}
		// Compare by date
		long difference = Math.abs(date.getEpochSecond() - o.date.getEpochSecond());
		if (difference > 10)
			return date.compareTo(o.date);

		// Compare by gameModeType
		int gameModeComparison = Integer.compare(gameModeType, o.gameModeType);
		if (gameModeComparison != 0) {
			return gameModeComparison;
		}
		// Compare by round
		int roundComparison = Integer.compare(round, o.round);
		if (roundComparison != 0) {
			return roundComparison;
		}
		// Compare by winner
		int winnerComparison = winner.compareTo(o.winner);
		if (winnerComparison != 0) {
			return winnerComparison;
		}
		// Compare by loser
		int loserComparison = loser.compareTo(o.loser);
		if (loserComparison != 0) {
			return loserComparison;
		}
		// Compare by warclan
		return Integer.compare(warclan, o.warclan);
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Battle battle = (Battle) o;
		if (round != battle.round) return false;
		if (gameModeType != battle.gameModeType) return false;
		if (warclan != battle.warclan) return false;

		// Compare the date with a tolerance of 10 seconds
		long difference = Math.abs(date.getEpochSecond() - battle.date.getEpochSecond());
		if (difference > 10) return false;

		if (!Objects.equals(winner, battle.winner)) return false;
		if (!Objects.equals(loser, battle.loser)) return false;

		return true;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(date.toString());
		out.writeInt(gameModeType);
		out.writeInt(round);
		winner.write(out);
		loser.write(out);
		out.writeInt(warclan);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		date = Instant.parse(in.readUTF());
		gameModeType = in.readInt();
		round = in.readInt();
		winner = new Player();
		winner.readFields(in);
		loser = new Player();
		loser.readFields(in);
		warclan = in.readInt();
	}

	public boolean isValid() {
		if (date == null) return false;
		if (winner == null || loser == null) return false;
		if (!winner.isValid() || !loser.isValid()) return false;
		return true;
	}
}
