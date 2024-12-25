package playerstudy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerData implements WritableComparable<PlayerData> {
    public String utag;
    public String ctag;
    public String evo;
    public String tower = "6e";
    public String deck;

    @Override
    public int hashCode() {
        return Objects.hash(utag, ctag);
    }

    private long computelongHashCode(String s) {
        if (s == null) {
            return 0; // Valeur par défaut pour une chaîne null
        }

        long hash = 0;
        long prime = 127; // Constante multiplicative pour minimiser les collisions
        for (int i = 0; i < s.length(); i++) {
            hash = hash * prime + s.charAt(i);
        }
        return hash;
    }

    public long longHashCode() {
        return computelongHashCode(utag);
    }

    @Override
    public int compareTo(PlayerData o) {
        if (o == null) {
            throw new NullPointerException("Cannot compare to null");
        }
        int cmp = utag.compareTo(o.utag);
        if (cmp != 0) return cmp;
        return ctag.compareTo(o.ctag);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Player {");
        sb.append("utag=").append(utag);
        sb.append(", ctag=").append(ctag);
        sb.append(", evo=").append(evo);
        sb.append(", tower=").append(tower);
        sb.append(", deck=").append(deck);
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerData that = (PlayerData) o;
        return ctag.equals(that.ctag) && utag.equals(that.utag);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(utag);
        out.writeUTF(ctag == null ? "NULL" : ctag);
        out.writeUTF(evo == null ? "NULL" : evo);
        out.writeUTF(tower == null ? "NULL" : tower);
        out.writeUTF(deck == null ? "NULL" : deck);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        utag = in.readUTF();
        ctag = in.readUTF();
        evo = in.readUTF();
        tower = in.readUTF();
        deck = in.readUTF();
    }
}