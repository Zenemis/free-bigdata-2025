package playerstudy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GameData {
    public List<PlayerData> players;
    public int winner;

    public boolean existsPlayers(){
        if (players == null || players.size() != 2) return false;
        return (players.get(0) != null && players.get(1) != null);
    }

    public PlayerData getWinner() {
        return players.get(0);
    }

    public PlayerData getLoser() {
        return players.get(1);
    }
}