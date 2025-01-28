package sparkwinrate;

import datacleaner.Player;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Deck implements Serializable {
    Set<String> players = new TreeSet<>();
    Map<String, Integer> evos;
    Map<String, Integer> wevos;
    Map<String, Integer> towers;
    Map<String, Integer> wtowers;
    public String id;
    int count;
    int win;
    double strength;
    int league;
    int trophy;

    public static Deck fromPlayer(String ngram, Player player, double strDelta, boolean win) {
        return new Deck(
                ngram,
                (ngram.length() == 16) ? player.evo : "",
                (ngram.length() == 16) ? player.tower : "",
                1,
                win ? 1 : 0,
                strDelta,
                player.utag,
                player.league,
                player.ctrophies
        );
    }

    public Deck(String str, String evo, String tower, int count, int win, double strength, String player, int league,
                int trophy) {
        this.id = str;
        this.count = count;
        this.win = win;
        this.strength = strength;
        this.league = league;
        this.trophy = trophy;
        players.add(player);

        if (str.length() == 16) {
            evos = new TreeMap<>();
            wevos = new TreeMap<>();
            towers = new TreeMap<>();
            wtowers = new TreeMap<>();
            for (int i = 0; i < evo.length() / 2; ++i) {
                String key = evo.substring(i * 2, i * 2 + 2);
                evos.put(key, 1);
                if (win > 0)
                    wevos.put(key, 1);
                else
                    wevos.put(key, 0);
            }
            for (int i = 0; i < tower.length() / 2; ++i) {
                String key = tower.substring(i * 2, i * 2 + 2);
                towers.put(key, 1);
                if (win > 0)
                    wtowers.put(key, 1);
                else
                    wtowers.put(key, 0);
            }
        }
    }

    Deck merge(Deck b) {
        this.count += b.count;
        this.win += b.win;
        this.strength += b.strength;
        this.league = Math.max(b.league, this.league);
        this.trophy = Math.max(b.trophy, this.trophy);

        for (String x : b.players) {
            if (players.size() > 10) break;
            players.add(x);
        }

        if (this.id.length() == 16 && b.id.length() == 16) {
            for (String key : b.evos.keySet()) {
                if (!this.evos.containsKey(key)) {
                    this.evos.put(key, b.evos.get(key));
                    this.wevos.put(key, b.wevos.get(key));
                } else {
                    this.evos.put(key, this.evos.get(key) + b.evos.get(key));
                    this.wevos.put(key, this.wevos.get(key) + b.wevos.get(key));
                }
            }
            for (String key : b.towers.keySet()) {

                if (!this.towers.containsKey(key)) {
                    this.towers.put(key, b.towers.get(key));
                    this.wtowers.put(key, b.wtowers.get(key));
                } else {
                    this.towers.put(key, this.towers.get(key) + b.towers.get(key));
                    this.wtowers.put(key, this.wtowers.get(key) + b.wtowers.get(key));
                }

            }
        }
        return this;
    }

    public String toString() {
        double winrate = 0;
        double stren = 0;
        if (count > 0) {
            winrate = Math.round((float) (win * 1000) / count) / 10.;
            stren = strength / count;
        }

        String sevo = "[";
        sevo = getString(sevo, evos, wevos);

        String stower = "[";
        stower = getString(stower, towers, wtowers);

        return "{'id':'" + id + "', 'evos':" + sevo + ", 'towers':" + stower
                + ", 'count':" + count + ", 'winrate':" + winrate + ", 'nbplayers':" + players.size() + ", 'win':" + win
                + ", 'strength':" + stren
                + ", 'league':" + league
                + ", 'ctrophy':" + trophy + "}";
    }

    private String getString(String sevo, Map<String, Integer> evos, Map<String, Integer> wevos) {
        if (evos == null || wevos == null) return "";
        StringBuilder sevoBuilder = new StringBuilder(sevo);
        boolean first = true;
        for (String key : evos.keySet()) {
            if (!first)
                sevoBuilder.append(",");
            first = false;
            sevoBuilder.append("['").append(key).append("',").append(evos.get(key)).append(",").append(Math.round(wevos.get(key) * 1000. / evos.get(key)) / 10.).append("]");
        }
        sevo = sevoBuilder.toString();
        sevo += ']';
        return sevo;
    }

}