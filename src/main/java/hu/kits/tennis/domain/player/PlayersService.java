package hu.kits.tennis.domain.player;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.domain.ktr.PlayerWithKTR;
import hu.kits.tennis.domain.ktr.PlayersWithKTR;
import hu.kits.tennis.domain.ktr.KTRService;
import hu.kits.tennis.domain.match.MatchRepository;
import hu.kits.tennis.domain.player.Player.Address;
import hu.kits.tennis.domain.player.Player.Contact;

public class PlayersService {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    
    private final KTRService ktrService;

    public PlayersService(PlayerRepository playerRepository, MatchRepository matchRepository, KTRService ktrService) {
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.ktrService = ktrService;
    }
    
    public PlayersWithKTR loadAllPlayersWithKTR() {
        List<PlayerWithKTR> playersWithKTR = ktrService.calculateKTRRanking();
        return new PlayersWithKTR(playersWithKTR);
    }
    
    public Players loadAllPlayers() {
        return playerRepository.loadAllPlayers();
    }
    
    public Player saveNewPlayer(Player player) {
        Player savedPlayer = playerRepository.saveNewPlayer(player);
        logger.info("Player created: {}", savedPlayer);
        return savedPlayer;
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
        return playerRepository.loadAllPlayers().get(playerId);
    }

    public Optional<Player> saveAddress(String name, String email, Address address) {
        Optional<Player> player = playerRepository.findPlayerByEmail(email);
        logger.info("Saving address data: {}, {} {}", name, email, address);
        if(player.isPresent()) {
            Player p = player.get();
            Contact c = p.contact();
            Contact updatedContact = new Contact(c.email(), c.phone(), address, c.comment());
            Player updatedPlayer = new Player(p.id(), p.name(), updatedContact, p.startingKTR(), p.organisations());
            playerRepository.updatePlayer(updatedPlayer);
            logger.info("Address data saved");
        } else {
            logger.warn("Email address not found: {}", email);
        }
        return player;
    }
    
    public Optional<Player> findPlayerByEmail(String email) {
        return playerRepository.findPlayerByEmail(email);
    }

}
