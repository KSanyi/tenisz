package hu.kits.tennis.domain.utr;

import java.util.Optional;

public interface PlayerRepository {

    Players loadAllPlayers();
    
    Player saveNewPlayer(Player player);
    
    void updatePlayer(Player updatedPlayer);

    void deletePlayer(Player player);

    Optional<Player> findPlayer(int id);

}
