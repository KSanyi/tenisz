package hu.kits.tennis.application;

import javax.sql.DataSource;

import hu.kits.tennis.domain.email.EmailSender;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.domain.user.UserRepository;
import hu.kits.tennis.domain.user.UserService;
import hu.kits.tennis.domain.user.password.DummyPasswordHasher;
import hu.kits.tennis.domain.utr.MatchRepository;
import hu.kits.tennis.domain.utr.PlayerRepository;
import hu.kits.tennis.domain.utr.UTRService;
import hu.kits.tennis.infrastructure.database.MatchJdbcRepository;
import hu.kits.tennis.infrastructure.database.PlayerJdbcRepository;
import hu.kits.tennis.infrastructure.database.TournamentJdbcRepository;
import hu.kits.tennis.infrastructure.database.UserJdbcRepository;

public class ResourceFactory {

    private final UserService userService;
    private final PlayerRepository playerRepository;
    private final TournamentService tournamentService;
    private final UTRService utrService;
    
    public ResourceFactory(DataSource dataSource, EmailSender emailSender) {
        
        UserRepository userRepository = new UserJdbcRepository(dataSource);
        userService = new UserService(userRepository, emailSender, new DummyPasswordHasher());
        
        playerRepository = new PlayerJdbcRepository(dataSource);
        
        MatchRepository matchRepository = new MatchJdbcRepository(dataSource, playerRepository);
        tournamentService = new TournamentService(new TournamentJdbcRepository(dataSource, playerRepository, matchRepository), matchRepository);
        
        utrService = new UTRService(matchRepository, playerRepository);
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

}
