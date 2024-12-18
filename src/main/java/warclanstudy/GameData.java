package warclanstudy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GameData {
    @JsonProperty("warclan")
    public WarClanData warclan;

    public boolean existsWarclan(){
        return warclan != null;
    }

    public WarClanKey getWarClanKey() {
        return new WarClanKey(this.warclan.day, this.warclan.hour_seg);
    }

    public WarClanValue getWarClanValue() {
        return new WarClanValue(this.warclan.period, this.warclan.training);
    }
}