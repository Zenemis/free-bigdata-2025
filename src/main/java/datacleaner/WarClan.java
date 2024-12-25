package datacleaner;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.apache.hadoop.io.WritableComparable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WarClan implements WritableComparable<WarClan> {
	public int period;
	public int day = 0;
	public int hour_seg = 0;
	public boolean training1 = false;
	public boolean training2 = false;

	public int toInt() {
		return (period * 1000 + day * 100 + hour_seg * 10 +
				(training1 ? 2 : 0) + (training2 ? 1 : 0));
	}

	public static WarClan fromInt(int value) {
		WarClan wc = new WarClan();
		wc.period = value / 1000;
		wc.day = value / 100;
		wc.hour_seg = value / 10;
		switch (value % 4) {
			case 0:
				wc.training1 = false;
				wc.training2 = false;
				break;
			case 1:
				wc.training1 = false;
				wc.training2 = true;
				break;
			case 2:
				wc.training1 = true;
				wc.training2 = false;
				break;
			case 3:
				wc.training1 = true;
				wc.training2 = true;
		}
		return wc;
	}

	@JsonProperty("period")
	public void setPeriod(String period) {
		if (period == null || !period.matches("\\d+-\\d")) {
			throw new IllegalArgumentException("Invalid period format: must match 'xxx-n'");
		}
		String[] parts = period.split("-");
		int xxx = Integer.parseInt(parts[0]); // Partie 'xxx'
		int n = Integer.parseInt(parts[1]);   // Partie 'n'
		this.period = xxx * 10 + n;           // Représentation combinée de 'xxx' et 'n'
	}


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
		return Objects.hash(period, day, hour_seg, training1, training2);
	}

	@Override
	public int compareTo(WarClan o) {
		if (o == null) {
			throw new NullPointerException("Cannot compare with null.");
		}
		if (this == o) return 0;
		int cmp = Integer.compare(period, o.period);
		if (cmp != 0) return cmp;

		cmp = Integer.compare(day, o.day);
		if (cmp != 0) return cmp;

		cmp = Integer.compare(hour_seg, o.hour_seg);
		if (cmp != 0) return cmp;


		cmp = Boolean.compare(training1, o.training1);
		if (cmp != 0) return cmp;

		return Boolean.compare(training2, o.training2);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof WarClan)) return false;
		WarClan warClan = (WarClan) o;
		return compareTo(warClan) == 0;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(day);
		out.writeInt(hour_seg);
		out.writeInt(period);
		out.writeBoolean(training1);
		out.writeBoolean(training2);
	}

	public static void writeEmpty(DataOutput out) throws IOException {
		out.writeInt(0);
		out.writeInt(0);
		out.writeInt(0);
		out.writeBoolean(false);
		out.writeBoolean(false);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		day = in.readInt();
		hour_seg = in.readInt();
		period = in.readInt();
		training1 = in.readBoolean();
		training2 = in.readBoolean();
	}

	public boolean isValid() {
		return day >= 0 && day <= 6 &&
				hour_seg >= 0 && hour_seg <= 23 &&
				period >= 0;
	}
}
