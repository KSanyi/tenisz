package hu.kits.tennis.infrastructure;

import javax.sql.DataSource;

import com.github.scribejava.core.oauth.OAuth20Service;

import hu.kits.tennis.application.AddPlayerAddressWorkflow;
import hu.kits.tennis.application.usecase.AllMatchesUseCase;
import hu.kits.tennis.application.usecase.InvoicingUseCase;
import hu.kits.tennis.domain.email.EmailSender;
import hu.kits.tennis.domain.invoice.InvoiceService;
import hu.kits.tennis.domain.ktr.KTRService;
import hu.kits.tennis.domain.match.MatchRepository;
import hu.kits.tennis.domain.match.MatchService;
import hu.kits.tennis.domain.player.PlayerRepository;
import hu.kits.tennis.domain.player.PlayersService;
import hu.kits.tennis.domain.player.registration.RegistrationService;
import hu.kits.tennis.domain.tournament.TournamentRepository;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.domain.tournament.VenueRepository;
import hu.kits.tennis.domain.user.UserRepository;
import hu.kits.tennis.domain.user.UserService;
import hu.kits.tennis.domain.user.password.DummyPasswordHasher;
import hu.kits.tennis.infrastructure.database.MatchJdbcRepository;
import hu.kits.tennis.infrastructure.database.PlayerJdbcRepository;
import hu.kits.tennis.infrastructure.database.RegistrationJdbcRepository;
import hu.kits.tennis.infrastructure.database.TournamentJdbcRepository;
import hu.kits.tennis.infrastructure.database.UserJdbcRepository;
import hu.kits.tennis.infrastructure.database.VenueHardcodedRepository;

public class ApplicationContext {

    private final UserService userService;
    private final PlayerRepository playerRepository;
    private final PlayersService playersService;
    private final MatchRepository matchRepository;
    private final TournamentService tournamentService;
    private final KTRService ktrService;
    private final MatchService matchService;
    private final InvoiceService invoiceService;
    private final RegistrationService registrationService;
    
    private final AddPlayerAddressWorkflow addPlayerAddressWorkflow;
    private final AllMatchesUseCase allMatchesUseCase;
    private final InvoicingUseCase invoicingUseCase;
    
    public ApplicationContext(DataSource dataSource, EmailSender emailSender, OAuth20Service oAuthService, InvoiceService invoiceService) {

        playerRepository = new PlayerJdbcRepository(dataSource);
        UserRepository userRepository = new UserJdbcRepository(dataSource);
        userService = new UserService(userRepository, playerRepository, emailSender, new DummyPasswordHasher(), oAuthService);
        
        matchRepository = new MatchJdbcRepository(dataSource, playerRepository);
        TournamentRepository tournamentRepository = new TournamentJdbcRepository(dataSource, playerRepository, matchRepository);
        
        matchService = new MatchService(matchRepository, tournamentRepository);
        ktrService = new KTRService(matchService, matchRepository, playerRepository, tournamentRepository);
        
        playersService = new PlayersService(playerRepository, matchRepository, ktrService);
        
        VenueRepository venueRepository = new VenueHardcodedRepository();
        tournamentService = new TournamentService(tournamentRepository, matchRepository, venueRepository, ktrService);
        this.invoiceService = invoiceService;
        registrationService = new RegistrationService(playersService, new RegistrationJdbcRepository(dataSource), invoiceService);
        
        addPlayerAddressWorkflow = new AddPlayerAddressWorkflow(playersService, invoiceService);
        allMatchesUseCase = new AllMatchesUseCase(playersService, matchService);
        invoicingUseCase = new InvoicingUseCase(tournamentService, invoiceService);
    }
    
    public UserService getUserService() {
        return userService;
    }

    public PlayerRepository getPlayerRepository() {
        return playerRepository;
    }
    
    public KTRService getKTRService() {
        return ktrService;
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
    
    public InvoiceService getInvoiceService() {
        return invoiceService;
    }
    
    public RegistrationService getRegistrationService() {
        return registrationService;
    }
    
    public AddPlayerAddressWorkflow getAddPlayerAddressWorkflow() {
        return addPlayerAddressWorkflow;
    }
    
    public AllMatchesUseCase getAllMatchesUseCase() {
        return allMatchesUseCase;
    }
    
    public InvoicingUseCase getInvoicingUseCase() {
        return invoicingUseCase;
    }
    
}
