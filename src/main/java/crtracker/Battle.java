package crtracker;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonGetter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
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

public class Battle implements Serializable{
	@JsonProperty("date")
	public String date;
	@JsonProperty("game")
	public String game;
	@JsonProperty("mode")
	public String mode;
	@JsonProperty("round")
	public int round;
	@JsonProperty("type")
	public String type;
	@JsonProperty("winner")
	public int winner;
	@JsonProperty("players")
	@JsonSetter("players")
	private void setPlayers(List<Player> players) {
		if (players.size() == 2) {
			this.player1 = players.get(0);
			this.player2 = players.get(1);
		} else {
			throw new IllegalArgumentException("Expected exactly two players");
		}
	}
	@JsonProperty("player1")
	public Player player1;
	@JsonProperty("player2")
	public Player player2;
	@JsonProperty("warclan")
	WarClan warclan;

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
			Instant thisDate = Instant.parse(this.date);
			Instant otherDate = Instant.parse(battle.date);

			long difference = Math.abs(thisDate.getEpochSecond() - otherDate.getEpochSecond());
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
}