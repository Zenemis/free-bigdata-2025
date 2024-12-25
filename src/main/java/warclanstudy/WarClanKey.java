package warclanstudy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

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

@JsonIgnoreProperties(ignoreUnknown = true)
public class WarClanKey implements WritableComparable<WarClanKey> {
    public int day;
    public int hour_seg;

    public WarClanKey() {}

    public WarClanKey(int day, int hour_seg) {
        this.day = day;
        this.hour_seg = hour_seg;
    }

    @Override
    public int hashCode() { return Objects.hash(day, hour_seg); }

    @Override
    public int compareTo(WarClanKey o) {
        if (o == null) {
            throw new NullPointerException("Cannot compare to null");
        }
        int cmp = day - o.day;
        if (cmp != 0) {
            return cmp;
        }
        return hour_seg - o.hour_seg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WarClanKey that = (WarClanKey) o;
        return this.compareTo(that) == 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("WarClanKey{");
        sb.append("day=").append(day);
        sb.append(", hour_seg=").append(hour_seg);
        sb.append('}');
        return sb.toString();
    }



    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(day);
        out.writeInt(hour_seg);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        day = in.readInt();
        hour_seg = in.readInt();
    }
}