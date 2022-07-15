package hu.kits.tennis.domain.utr;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayersService {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;

    public PlayersService(PlayerRepository playerRepository, MatchRepository matchRepository) {
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
    }
    
    public Players loadAllPlayers() {
        return playerRepository.loadAllPlayers();
    }
    
    public Player saveNewPlayer(Player player) {
        return playerRepository.saveNewPlayer(player);
    }
    
    public void updatePlayer(Player updatedPlayer) {
        playerRepository.updatePlayer(updatedPlayer);
    }

    public boolean deletePlayer(Player player) {
        if(matchRepository.loadAllPlayedMatches(player).isEmpty()) {
            playerRepository.deletePlayer(player);
            logger.info("Player deleted: {}", player);
            return true;
        } else {
            return false;
        }
    }

    public Player findPlayer(int playerId) {
        return loadAllPlayers().get(playerId);
    }

}
