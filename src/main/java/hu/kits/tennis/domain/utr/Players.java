package hu.kits.tennis.domain.utr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Players {

    private final Map<Integer, Player> playersMap;
    
    public Players(List<Player> entries) {
        playersMap = entries.stream().collect(Collectors.toMap(Player::id, Function.identity()));
        playersMap.put(0, Player.BYE);
    }
    
    public boolean containsPlayerWithName(String playerName) {
        return playersMap.values().stream().anyMatch(player -> player.name().equals(playerName));
    }
    
    public Optional<Player> findPlayer(String playerName) {
        return playersMap.values().stream().filter(player -> player.name().equals(playerName)).findAny();
    }

    public boolean isEmpty() {
        return playersMap.isEmpty();
    }

    public Players add(Player player) {
        List<Player> playersList = new ArrayList<>(entries());
        playersList.add(player);
        Collections.sort(playersList, Comparator.comparing(Player::name));
        return new Players(playersList);
    }

    public Player get(int playerId) {
        return playersMap.get(playerId);
    }

    public List<Player> entries() {
        return playersMap.values().stream()
                .sorted(Comparator.comparing(Player::name))
                .toList();
    }

    public Player getOne() {
        return playersMap.values().iterator().next();
    }
    
}
