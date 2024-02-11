package hu.kits.tennis.domain.player;

import java.util.List;
import java.util.Optional;

import hu.kits.tennis.domain.ktr.KTRUpdate;

public interface PlayerRepository {

    Players loadAllPlayers();
    
    Player saveNewPlayer(Player player);
    
    void updatePlayer(Player updatedPlayer);

    void deletePlayer(Player player);

    Optional<Player> findPlayer(int id);

    Optional<Player> findPlayerByEmail(String email);

    List<KTRUpdate> loadAllKTRUpdates();

}
