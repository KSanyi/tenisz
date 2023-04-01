package hu.kits.tennis.infrastructure;

import javax.sql.DataSource;

import hu.kits.tennis.domain.email.EmailSender;
import hu.kits.tennis.domain.match.MatchRepository;
import hu.kits.tennis.domain.match.MatchService;
import hu.kits.tennis.domain.player.PlayerRepository;
import hu.kits.tennis.domain.player.PlayersService;
import hu.kits.tennis.domain.tournament.TournamentRepository;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.domain.tournament.VenueRepository;
import hu.kits.tennis.domain.user.UserRepository;
import hu.kits.tennis.domain.user.UserService;
import hu.kits.tennis.domain.user.password.DummyPasswordHasher;
import hu.kits.tennis.domain.utr.UTRService;
import hu.kits.tennis.infrastructure.database.MatchJdbcRepository;
import hu.kits.tennis.infrastructure.database.PlayerJdbcRepository;
import hu.kits.tennis.infrastructure.database.TournamentJdbcRepository;
import hu.kits.tennis.infrastructure.database.UserJdbcRepository;
import hu.kits.tennis.infrastructure.database.VenueHardcodedRepository;

public class ResourceFactory {

    private final UserService userService;
    private final PlayerRepository playerRepository;
    private final PlayersService playersService;
    private final MatchRepository matchRepository;
    private final TournamentService tournamentService;
    private final UTRService utrService;
    private final MatchService matchService;
    
    public ResourceFactory(DataSource dataSource, EmailSender emailSender) {
        
        UserRepository userRepository = new UserJdbcRepository(dataSource);
        userService = new UserService(userRepository, emailSender, new DummyPasswordHasher());
        
        playerRepository = new PlayerJdbcRepository(dataSource);
        matchRepository = new MatchJdbcRepository(dataSource, playerRepository);
        TournamentRepository tournamentRepository = new TournamentJdbcRepository(dataSource, playerRepository, matchRepository);
        playersService = new PlayersService(playerRepository, matchRepository);
        
        matchService = new MatchService(matchRepository, tournamentRepository);
        utrService = new UTRService(matchService, matchRepository, playerRepository, tournamentRepository);
        VenueRepository venueRepository = new VenueHardcodedRepository();
        tournamentService = new TournamentService(tournamentRepository, matchRepository, venueRepository, utrService);
    }
    
    public UserService getUserService() {
        return userService;
    }

    public PlayerRepository getPlayerRepository() {
        return playerRepository;
    }
    
    public UTRService getUTRService() {
        return utrService;
    }

    public TournamentService getTournamentService() {
        return tournamentService;
    }
    
    public MatchService getMatchService() {
        return matchService;
    }

    public MatchRepository getMatchRepository() {
        return matchRepository;
    }
    
    public PlayersService getPlayersService() {
        return playersService;
    }
    
}
