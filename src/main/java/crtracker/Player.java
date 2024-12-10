package crtracker;

import java.io.Serializable;
import java.util.Objects;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;


public class Player implements Serializable{
	@JsonProperty("utag")
	public String utag;
	@JsonProperty("ctag")
	public String ctag;
	@JsonProperty("trophies")
	public int trophies;
	@JsonProperty("ctrophies")
	public int ctrophies;
	@JsonProperty("exp")
	public int exp;
	@JsonProperty("league")
	public int league;
	@JsonProperty("bestleague")
	public int bestleague;
	@JsonProperty("deck")
	public long deck;
	@JsonSetter("deck")
	public void setDeck(String deck) {
		if (deck == null || deck.length() < 16) {
			throw new IllegalArgumentException("Invalid deck: must be non-null and at least 16 characters long.");
		}
		this.deck = Long.parseLong(deck, 16); // Converts the hexadecimal string to a long
	}
	@JsonProperty("evo")
	public String evo;
	@JsonProperty("tower")
	public String tower = "6e";
	@JsonProperty("strength")
	public double strength;
	@JsonProperty("crown")
	public int crown;
	@JsonProperty("elixir")
	public double elixir;
	@JsonProperty("touch")
	public int touch;
	@JsonProperty("score")
	public int score;

	@Override
	public String toString() {
		return "Player [utag=" + utag +"]";
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

	public boolean isValid(){
		if (touch == 0) return false;
		if (evo == null || evo.length() > 2*2) return false;
		return true;
	}

}