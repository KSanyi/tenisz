package hu.kits.tennis.domain.utr;

public interface PlayerRepository {

    Players loadAllPlayers();
    
    Player saveNewPlayer(Player player);
    
    void updatePlayer(Player updatedPlayer);

    void deletePlayer(Player player);

}
