package listgamemodes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.hadoop.io.WritableComparable;

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
public class GameMode implements WritableComparable<GameMode> {
    public String game;
    public String mode;
    public String type;

    @Override
    public int hashCode() {
        return Objects.hash(game, mode, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameMode battle = (GameMode) o;
        if (!Objects.equals(game, battle.game)) return false;
        if (!Objects.equals(type, battle.type)) return false;
        if (!Objects.equals(mode, battle.mode)) return false;

        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(64); // Pre-allocate some capacity for performance
        sb.append("GameMode{")
                .append("game='").append(game).append('\'')
                .append(", mode='").append(mode).append('\'')
                .append(", type='").append(type).append('\'')
                .append('}');
        return sb.toString();
    }


    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(game);
        out.writeUTF(mode);
        out.writeUTF(type);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        game = in.readUTF();
        mode = in.readUTF();
        type = in.readUTF();
    }

    @Override
    public int compareTo(GameMode o) {
        if (o == null) {
            throw new NullPointerException("Cannot compare to null");
        }

        // Compare 'game' fields
        int gameComparison = this.game.compareTo(o.game);
        if (gameComparison != 0) {
            return gameComparison;
        }

        // Compare 'mode' fields if 'game' fields are equal
        int modeComparison = this.mode.compareTo(o.mode);
        if (modeComparison != 0) {
            return modeComparison;
        }

        // Compare 'type' fields if 'game' and 'mode' fields are equal
        return this.type.compareTo(o.type);
    }
}