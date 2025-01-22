package datacleaner;

import org.apache.hadoop.io.WritableComparable;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WarClan implements Serializable {
    @JsonProperty("day")
    public int day=0;
    @JsonProperty("hourd_seg")
    public int hour_seg=0;
    @JsonProperty("period")
    public String period;
    @JsonProperty("training")
    public ArrayList<Boolean> training;
}
