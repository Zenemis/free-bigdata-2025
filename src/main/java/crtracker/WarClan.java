package crtracker;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.apache.hadoop.io.WritableComparable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WarClan implements WritableComparable<WarClan> {
	public int day=0;
	public int hour_seg=0;
	public String period;
	public boolean training1 = false;	//????
	public boolean training2 = false;

	@JsonProperty("training")
	public void setTrainings(List<Boolean> training) {
		if (training == null || training.size() != 2) {
			throw new IllegalArgumentException("Invalid training list: must contain exactly 2 trainings");
		}
		this.training1 = training.get(0);
		this.training2 = training.get(1);
	}

	@Override
	public int hashCode() {
		return Objects.hash(day, hour_seg, period, training1, training2);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof WarClan)) return false;
		WarClan warClan = (WarClan) o;
		return day == warClan.day && hour_seg == warClan.hour_seg && Objects.equals(period, warClan.period);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(day);
		out.writeInt(hour_seg);
		out.writeUTF(period != null ? period : ""); // Handle null values for period
		out.writeBoolean(training1);
		out.writeBoolean(training2);
	}

	public static void writeEmpty(DataOutput out) throws IOException {
		out.writeInt(0);
		out.writeInt(0);
		out.writeUTF("");
		out.writeBoolean(false);
		out.writeBoolean(false);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		day = in.readInt();
		hour_seg = in.readInt();
		period = in.readUTF();
		training1 = in.readBoolean();
		training2 = in.readBoolean();
	}

	@Override
	public int compareTo(WarClan o) {
		return this.equals(o) ? 0 : period.compareTo(o.period);
	}

	public boolean isValid(){
		if (day<0 || day>31) return false;
		if (period == null || period.isEmpty()) return false;
		return true;
	}
}