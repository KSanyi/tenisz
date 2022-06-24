package hu.kits.tennis.application;

import static hu.kits.tennis.TestUtil.player1;
import static hu.kits.tennis.TestUtil.player2;
import static hu.kits.tennis.TestUtil.player3;
import static hu.kits.tennis.TestUtil.player4;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hu.kits.tennis.domain.tournament.Contestant;
import hu.kits.tennis.domain.tournament.DrawMode;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.Tournament.Board;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.domain.utr.Match;
import hu.kits.tennis.domain.utr.MatchResult;
import hu.kits.tennis.domain.utr.MatchResultInfo;
import hu.kits.tennis.domain.utr.PlayerRepository;
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
        PlayerRepository playerRepositosy = resourceFactory.getPlayerRepository();
        
        playerRepositosy.saveNewPlayer(player1);
        playerRepositosy.saveNewPlayer(player2);
        playerRepositosy.saveNewPlayer(player3);
        playerRepositosy.saveNewPlayer(player4);
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
        
    @Test
    void createAndPlay2RoundsTournament() {
        
        Tournament tournament = tournamentService.createTournament("BVSC Szőnyi út Nyári tour 2022", "BVSC Szőnyi út", LocalDate.of(2022, 6, 1), Tournament.Type.SIMPLE_BOARD, 2);
        
        tournamentService.updateContestants(tournament, List.of(player1, player2, player3, player4));
        tournamentService.createMatches(tournament.id(), DrawMode.SIMPLE);
        tournament = tournamentService.findTournament(tournament.id()).get();
        
        assertEquals(List.of(
                new Contestant(player1, 1), 
                new Contestant(player2, 2),
                new Contestant(player3, 3),
                new Contestant(player4, 4)), tournament.contestants());
        
        assertEquals(1, tournament.boards().size());
        Board mainBoard = tournament.mainBoard();
        
        var matches = mainBoard.matches();
        assertEquals(2, matches.size());
        
        Match semiFinal1 = matches.get(1);
        assertEquals(player1, semiFinal1.player1());
        assertEquals(player2, semiFinal1.player2());
        assertNull(semiFinal1.result());
        
        Match semiFinal2 = matches.get(2);
        assertEquals(player3, semiFinal2.player1());
        assertEquals(player4, semiFinal2.player2());
        assertNull(semiFinal2.result());
        
        tournamentService.setTournamentMatchResult(new MatchResultInfo(semiFinal1, LocalDate.of(2022,1,1), new MatchResult(6, 4)));
        
        matches = tournamentService.findTournament(tournament.id()).get().mainBoard().matches();
        assertEquals(3, matches.size());
        
        semiFinal1 = matches.get(1);
        assertEquals(new MatchResult(6, 4), semiFinal1.result());
        
        Match theFinal = matches.get(3);
        assertEquals(player1, theFinal.player1());
        assertNull(theFinal.player2());
        assertNull(theFinal.result());
        
        tournamentService.setTournamentMatchResult(new MatchResultInfo(semiFinal2, LocalDate.of(2022,1,1), new MatchResult(3, 6)));
        
        matches = tournamentService.findTournament(tournament.id()).get().mainBoard().matches();
        assertEquals(3, matches.size());
        theFinal = matches.get(3);
        assertEquals(player1, theFinal.player1());
        assertEquals(player4, theFinal.player2());
        assertNull(theFinal.result());
        
        tournamentService.setTournamentMatchResult(new MatchResultInfo(theFinal, LocalDate.of(2022,1,1), new MatchResult(6, 0)));
        matches = tournamentService.findTournament(tournament.id()).get().mainBoard().matches();
        assertEquals(3, matches.size());
    }
    
}
