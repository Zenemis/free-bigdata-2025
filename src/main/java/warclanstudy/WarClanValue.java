package warclanstudy;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WarClanValue implements WritableComparable<WarClanValue> {
    public String period;
    public List<Boolean> training;

    public WarClanValue() {}

    public WarClanValue(String period, List<Boolean> training) {
        this.period = period;
        this.training = training;
    }

    @Override
    public int hashCode() { return Objects.hash(period, training); }

    @Override
    public int compareTo(WarClanValue o) {
        int cmp = period.compareTo(o.period);
        if (cmp != 0) return cmp;
        for (int i = 0; i < training.size(); i++) {
            cmp = training.get(i).compareTo(o.training.get(i));
            if (cmp != 0)  return cmp;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WarClanValue that = (WarClanValue) o;
        return this.compareTo(that) == 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("WarClanValue{");
        sb.append(period);
        sb.append(", ");
        sb.append(training);
        sb.append("}");
        return sb.toString();
    }



    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(period == null ? "NULL" : period);
        if (training != null) {
            out.writeInt(training.size());
            for (Boolean aBoolean : training) {
                out.writeBoolean(aBoolean);
            }
        }
        else {
            out.writeInt(-1);
        }
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        period = in.readUTF();
        int size = in.readInt();
        training = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            training.add(in.readBoolean());
        }
    }
}