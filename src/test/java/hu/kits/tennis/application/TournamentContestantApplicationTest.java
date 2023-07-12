package hu.kits.tennis.application;

import static hu.kits.tennis.testutil.TestUtil.player1;
import static hu.kits.tennis.testutil.TestUtil.player2;
import static hu.kits.tennis.testutil.TestUtil.player3;
import static hu.kits.tennis.testutil.TestUtil.player4;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hu.kits.tennis.domain.player.PlayerRepository;
import hu.kits.tennis.domain.tournament.Contestant;
import hu.kits.tennis.domain.tournament.Organization;
import hu.kits.tennis.domain.tournament.PaymentStatus;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentParams;
import hu.kits.tennis.domain.tournament.TournamentParams.Level;
import hu.kits.tennis.domain.tournament.TournamentParams.Structure;
import hu.kits.tennis.domain.tournament.TournamentParams.Type;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.infrastructure.ApplicationContext;
import hu.kits.tennis.testutil.InMemoryDataSourceFactory;
import hu.kits.tennis.testutil.SpyEmailSender;

public class TournamentContestantApplicationTest {

    private static TournamentService tournamentService;
    private String tournamentId;
    
    @BeforeEach
    private void init() throws Exception {
        DataSource dataSource = InMemoryDataSourceFactory.createDataSource();
        
        ApplicationContext resourceFactory = new ApplicationContext(dataSource, new SpyEmailSender(), null, null);
        tournamentService = resourceFactory.getTournamentService();
        PlayerRepository playerRepositosy = resourceFactory.getPlayerRepository();
        
        playerRepositosy.saveNewPlayer(player1);
        playerRepositosy.saveNewPlayer(player2);
        playerRepositosy.saveNewPlayer(player3);
        playerRepositosy.saveNewPlayer(player4);
        
        Tournament tournament = tournamentService.createTournament(new TournamentParams(Organization.KVTK, Type.DAILY, Level.L250, Level.L250, LocalDate.of(2022, 1, 1), "Masters 500", "Mini Garros", Structure.SIMPLE_BOARD, 3));
        tournamentId = tournament.id();
    }
    
    @Test
    void contestantUpdateTest() {
        
        Tournament tournament = loadTournament();
        assertEquals(List.of(), tournament.contestants());
        
        // add 2 contestants
        tournamentService.updateContestants(tournament, Contestant.of(player1, player2));
        
        tournament = loadTournament();
        assertEquals(List.of(new Contestant(player1, 1), new Contestant(player2, 2)), tournament.contestants());
        
        // swap contestants
        tournamentService.updateContestants(tournament, Contestant.of(player2, player1));
        
        tournament = loadTournament();
        assertEquals(List.of(new Contestant(player2, 1), new Contestant(player1, 2)), tournament.contestants());
        
        // replace contestant
        tournamentService.updateContestants(tournament, Contestant.of(player3, player1));
        
        tournament = loadTournament();
        assertEquals(List.of(new Contestant(player3, 1), new Contestant(player1, 2)), tournament.contestants());
        
        // add contestant
        tournamentService.updateContestants(tournament, Contestant.of(player3, player1, player4));
        
        tournament = loadTournament();
        assertEquals(List.of(new Contestant(player3, 1), new Contestant(player1, 2), new Contestant(player4, 3)), tournament.contestants());
    }
    
    @Test
    void contestantPaymentStatusUpdateTest() {
        
        Tournament tournament = loadTournament();
        assertEquals(List.of(), tournament.contestants());
        
        // add 2 contestants
        tournamentService.updateContestants(tournament, Contestant.of(player1, player2));
        
        // player1 paid
        tournamentService.setPaymentStatus(tournament, player1, PaymentStatus.PAID);
        
        tournament = loadTournament();
        assertEquals(List.of(new Contestant(player1, 1, PaymentStatus.PAID), new Contestant(player2, 2)), tournament.contestants());
        
        // player1 invoice sent
        tournamentService.setPaymentStatus(tournament, player1,PaymentStatus.INVOICE_SENT);
        tournament = loadTournament();
        assertEquals(List.of(new Contestant(player1, 1, PaymentStatus.INVOICE_SENT), new Contestant(player2, 2)), tournament.contestants());
    }
    
    @Test
    void contestantPaymentStatusAndRankUpdateTest() {
        
        Tournament tournament = loadTournament();
        // add 2 contestants
        tournamentService.updateContestants(tournament, Contestant.of(player1, player2));
        // player1 paid
        tournamentService.setPaymentStatus(tournament, player1, PaymentStatus.PAID);
        // swap contestants
        List<Contestant> contestants = loadTournament().contestants();
        tournamentService.updateContestants(tournament, List.of(contestants.get(1), contestants.get(0)));
        
        assertEquals(List.of(new Contestant(player2, 1), new Contestant(player1, 2, PaymentStatus.PAID)), loadTournament().contestants());
    }
    
    private Tournament loadTournament() {
        return tournamentService.findTournament(tournamentId).get();
    }
    
}
