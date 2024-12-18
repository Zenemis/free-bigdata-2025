package warclanstudy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import crtracker.WarClan;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WarClanData implements WritableComparable<WarClanData> {
    public int day;
    public int hour_seg;
    public String period;
    public List<Boolean> training;

    @Override
    public int hashCode() {
        return Objects.hash(day, hour_seg, period, training);
    }

    @Override
    public int compareTo(WarClanData o) {
        if (o == null) {
            throw new NullPointerException("Cannot compare to null");
        }
        int cmp = day - o.day;
        if (cmp != 0) {
            return cmp;
        }
        cmp = hour_seg - o.hour_seg;
        if (cmp != 0) {
            return cmp;
        }
        cmp = period.compareTo(o.period);
        if (cmp != 0) return cmp;
        for (int i = 0; i < training.size(); i++) {
            cmp = training.get(i).compareTo(o.training.get(i));
            if (cmp != 0)  return cmp;
        }
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("WarClanData {");
        sb.append("day=").append(day);
        sb.append(", hour_seg=").append(hour_seg);
        sb.append(", period=").append(period);
        sb.append(", training=").append(training);
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WarClanData that = (WarClanData) o;
        return this.compareTo(that) == 0;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(day);
        out.writeInt(hour_seg);
        out.writeUTF(period);
        out.writeInt(training.size());
        for (Boolean b : training) {
            out.writeBoolean(b);
        }
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        day = in.readInt();
        hour_seg = in.readInt();
        period = in.readUTF();
        int size = in.readInt();
        training = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            training.add(in.readBoolean());
        }
    }
}