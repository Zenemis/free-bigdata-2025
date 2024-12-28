package datacleaner;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WarClan implements WritableComparable<WarClan> {
    public String period;
    public int day = 0;
    public int hour_seg = 0;
    public List<Boolean> training;

    @Override
    public int hashCode() {
        return Objects.hash(period, day, hour_seg, training);
    }

    @Override
    public int compareTo(WarClan o) {
        if (o == null) {
            throw new NullPointerException("Cannot compare with null.");
        }
        if (this == o) return 0;
        int cmp = period.compareTo(o.period);
        if (cmp != 0) return cmp;

        cmp = Integer.compare(day, o.day);
        if (cmp != 0) return cmp;

        cmp = Integer.compare(hour_seg, o.hour_seg);
        if (cmp != 0) return cmp;

        return training.hashCode() - o.training.hashCode();
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
        out.writeUTF(period);
        out.writeInt(training.size());
        for (Boolean b : training) {
            out.writeBoolean(b);
        }
    }

    public static void writeEmpty(DataOutput out) throws IOException {
        out.writeInt(0);
        out.writeInt(0);
        out.writeUTF("");
        out.writeInt(2);
        out.writeBoolean(false);
        out.writeBoolean(false);
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

    public boolean isValid() {
        return day >= 0 && day <= 6 &&
                hour_seg >= 0 && hour_seg <= 23;
    }
}
