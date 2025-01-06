package sparkwinrate;

import java.io.Serializable;
import java.util.Comparator;

public class WinrateComparator implements Comparator<Deck>, Serializable {
    @Override
    public int compare(Deck x, Deck y) {
        if (y.count == 0 && x.count != 0)
            return 1;
        if (x.count == 0 && y.count != 0)
            return -1;
        if (x.count == 0 && y.count == 0)
            return 0;
        if ((double) x.win / x.count > (double) y.win / y.count)
            return 1;
        else if ((double) x.win / x.count < (double) y.win / y.count)
            return -1;
        return 0;
    }
}