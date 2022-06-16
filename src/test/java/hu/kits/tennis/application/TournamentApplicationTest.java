package hu.kits.tennis.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.testutil.InMemoryDataSourceFactory;
import hu.kits.tennis.testutil.SpyEmailSender;

public class TournamentApplicationTest {

    private static final SpyEmailSender spyEmailSender = new SpyEmailSender();
    
    private static TournamentService tournamentService;
    
    @SuppressWarnings("static-method")
    @BeforeEach
    private void init() throws Exception {
        DataSource dataSource = InMemoryDataSourceFactory.createDataSource(
                "INSERT INTO USER VALUES('ksanyi', 'PWD', 'Kócsó Sanyi', 'ADMIN', '06703699209', 'kocso.sandor.gabor@gmail.com', 'ACTIVE')",
                "INSERT INTO USER VALUES('csányika', 'PWD', 'Csányi Zsolt', 'MEMBER', '', 'csanyika@xxx.hu', 'ACTIVE')");
        
        ResourceFactory resourceFactory = new ResourceFactory(dataSource, spyEmailSender);
        tournamentService = resourceFactory.getTournamentService();
    }
    
    @Test
    void createTournament() {
        List<Tournament> tournaments = tournamentService.loadAllTournaments();
        assertEquals(0, tournaments.size());
        
        tournamentService.createTournament("BVSC Szőnyi út Nyári tour 2022", "BVSC Szőnyi út", LocalDate.of(2022, 6, 1), Tournament.Type.SIMPLE_BOARD, 3);
        
        tournaments = tournamentService.loadAllTournaments();
        assertEquals(1, tournaments.size());
        
        Tournament tournament = tournaments.get(0);
        assertEquals("BVSC Szőnyi út Nyári tour 2022", tournament.name());
        assertEquals("BVSC Szőnyi út", tournament.venue());
        assertEquals(LocalDate.of(2022, 6, 1), tournament.date());
        assertEquals(Tournament.Type.SIMPLE_BOARD, tournament.type());
        assertEquals(List.of(), tournament.contestants());
    }
    
    @Test
    void updateTournament() {
        
        Tournament tournament = tournamentService.createTournament("BVSC Szőnyi út Nyári tour 2022", "BVSC Szőnyi út", LocalDate.of(2022, 6, 1), Tournament.Type.SIMPLE_BOARD, 3);
        
        tournamentService.updateTournamentName(tournament, "BVSC Tatai út Nyári tour 2022");
        tournament = tournamentService.loadAllTournaments().get(0);
        assertEquals("BVSC Tatai út Nyári tour 2022", tournament.name());
        
        tournamentService.updateTournamentDate(tournament, LocalDate.of(2022, 5, 1));
        tournament = tournamentService.loadAllTournaments().get(0);
        assertEquals(LocalDate.of(2022, 5, 1), tournament.date());
        
        tournamentService.updateTournamentVenue(tournament, "BVSC Tatai út");
        tournament = tournamentService.loadAllTournaments().get(0);
        assertEquals("BVSC Tatai út", tournament.venue());
        
        tournamentService.updateTournamentType(tournament, Tournament.Type.BOARD_AND_CONSOLATION);
        tournament = tournamentService.loadAllTournaments().get(0);
        assertEquals(Tournament.Type.BOARD_AND_CONSOLATION, tournament.type());
    }
    
    @Test
    void deleteTournament() {
        
        Tournament tournament = tournamentService.createTournament("BVSC Szőnyi út Nyári tour 2022", "BVSC Szőnyi út", LocalDate.of(2022, 6, 1), Tournament.Type.SIMPLE_BOARD, 3);
        
        tournamentService.deleteTournament(tournament);
        List<Tournament> tournaments = tournamentService.loadAllTournaments();
        assertEquals(0, tournaments.size());
    }
        
    
}
