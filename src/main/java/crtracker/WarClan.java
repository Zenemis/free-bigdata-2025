package crtracker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

import org.codehaus.jackson.annotate.JsonProperty;

class WarClan implements Serializable{
	@JsonProperty("day")
	public int day=0;
	@JsonProperty("hour_seg")
	public int hour_seg=0;
	@JsonProperty("period")
	public String period;
	@JsonProperty("training")
	ArrayList<Boolean> training;	//????

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof WarClan)) return false;
		WarClan warClan = (WarClan) o;
		return day == warClan.day && hour_seg == warClan.hour_seg && Objects.equals(period, warClan.period);
	}

}