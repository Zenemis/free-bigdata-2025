package crtracker;

import java.io.Serializable;
import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonProperty;

class WarClan implements Serializable{
	@JsonProperty("day")
	public int day=0;
	@JsonProperty("hourd_seg")
	public int hour_seg=0;
	@JsonProperty("period")
	public String period;
	@JsonProperty("training")
	ArrayList<Boolean> training;	
}