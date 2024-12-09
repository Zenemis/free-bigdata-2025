package crtracker;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonProperty;


class Player implements Serializable{
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
	public String deck;
	@JsonProperty("evo")
	public String evo;
	@JsonProperty("tower")
	public String tower;
	@JsonProperty("strength")
	public double strength;
	@JsonProperty("elixir")
	public double elixir;
	@JsonProperty("touch")
	public int touch;
	@JsonProperty("score")
	public int score;
}