package hu.kits.tennis.application;

import static hu.kits.tennis.testutil.TestUtil.player1;
import static hu.kits.tennis.testutil.TestUtil.player2;
import static hu.kits.tennis.testutil.TestUtil.player3;
import static hu.kits.tennis.testutil.TestUtil.player4;
import static hu.kits.tennis.testutil.TestUtil.player5;
import static hu.kits.tennis.testutil.TestUtil.player6;
import static hu.kits.tennis.testutil.TestUtil.player7;
import static hu.kits.tennis.testutil.TestUtil.player8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.match.MatchResult;
import hu.kits.tennis.domain.match.MatchResultInfo;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.PlayerRepository;
import hu.kits.tennis.domain.tournament.Contestant;
import hu.kits.tennis.domain.tournament.Organization;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentBoard;
import hu.kits.tennis.domain.tournament.TournamentParams;
import hu.kits.tennis.domain.tournament.TournamentParams.Level;
import hu.kits.tennis.domain.tournament.TournamentParams.Status;
import hu.kits.tennis.domain.tournament.TournamentParams.Structure;
import hu.kits.tennis.domain.tournament.TournamentParams.Type;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.domain.tournament.TournamentSummary;
import hu.kits.tennis.infrastructure.ApplicationContext;
import hu.kits.tennis.testutil.InMemoryDataSourceFactory;
import hu.kits.tennis.testutil.SpyEmailSender;

public class TournamentApplicationTest {

    private static final SpyEmailSender spyEmailSender = new SpyEmailSender();
    
    private static TournamentService tournamentService;
    
    private final TournamentParams DEFAULT_PARAMS = new TournamentParams(Organization.KVTK, Type.DAILY, Level.L250, Level.L250, LocalDate.of(2022, 1, 1), "Masters 500", "Mini Garros", Structure.SIMPLE_BOARD, 3);
    
    @SuppressWarnings("static-method")
    @BeforeEach
    private void init() throws Exception {
        DataSource dataSource = InMemoryDataSourceFactory.createDataSource(
                "INSERT INTO USER VALUES('ksanyi', 'PWD', 'Kócsó Sanyi', 'ADMIN', '06703699209', 'kocso.sandor.gabor@gmail.com', 'ACTIVE', 0)",
                "INSERT INTO USER VALUES('csányika', 'PWD', 'Csányi Zsolt', 'MEMBER', '', 'csanyika@xxx.hu', 'ACTIVE', 0)");
        
        ApplicationContext resourceFactory = new ApplicationContext(dataSource, spyEmailSender, null, null);
        tournamentService = resourceFactory.getTournamentService();
        PlayerRepository playerRepositosy = resourceFactory.getPlayerRepository();
        
        playerRepositosy.saveNewPlayer(player1);
        playerRepositosy.saveNewPlayer(player2);
        playerRepositosy.saveNewPlayer(player3);
        playerRepositosy.saveNewPlayer(player4);
        playerRepositosy.saveNewPlayer(player5);
        playerRepositosy.saveNewPlayer(player6);
        playerRepositosy.saveNewPlayer(player7);
        playerRepositosy.saveNewPlayer(player8);
    }
    
    @Test
    void createTournament() {
        List<TournamentSummary> tournamentSummaries = tournamentService.loadDailyTournamentSummariesList();
        assertEquals(0, tournamentSummaries.size());
        
        tournamentService.createTournament(DEFAULT_PARAMS);
        
        tournamentSummaries = tournamentService.loadDailyTournamentSummariesList();
        assertEquals(1, tournamentSummaries.size());
        
        TournamentSummary tournamentSummary = tournamentSummaries.get(0);
        assertEquals(Organization.KVTK, tournamentSummary.organiser());
        assertEquals(Type.DAILY, tournamentSummary.type());
        assertEquals(Level.L250, tournamentSummary.levelFrom());
        assertEquals(Level.L250, tournamentSummary.levelTo());
        assertEquals(Status.DRAFT, tournamentSummary.status());
        assertEquals("Masters 500", tournamentSummary.name());
        assertEquals(LocalDate.of(2022, 1, 1), tournamentSummary.date());
    }
    
    //@Test
    void updateTournament() {
        
        Tournament tournament = tournamentService.createTournament(DEFAULT_PARAMS);
        // TODO implement
        /*
        tournamentService.updateTournamentName(tournament, "BVSC Tatai út Nyári tour 2022");
        tournament = tournamentService.loadAllTournaments().get(0);
        assertEquals("BVSC Tatai út Nyári tour 2022", tournament.params().name());
        
        tournamentService.updateTournamentDate(tournament, LocalDate.of(2022, 5, 1));
        tournament = tournamentService.loadAllTournaments().get(0);
        assertEquals(LocalDate.of(2022, 5, 1), tournament.params().date());
        
        tournamentService.updateTournamentVenue(tournament, "BVSC Tatai út");
        tournament = tournamentService.loadAllTournaments().get(0);
        assertEquals("BVSC Tatai út", tournament.params().venue());
        
        tournamentService.updateTournamentType(tournament, Structure.BOARD_AND_CONSOLATION);
        tournament = tournamentService.loadAllTournaments().get(0);
        assertEquals(Structure.BOARD_AND_CONSOLATION, tournament.params().structure());
        */
    }
    
    @Test
    void deleteTournament() {
        
        Tournament tournament = tournamentService.createTournament(DEFAULT_PARAMS);
        
        tournamentService.deleteTournament(tournament);
        List<TournamentSummary> tournamentSummaries = tournamentService.loadDailyTournamentSummariesList();
        assertEquals(0, tournamentSummaries.size());
    }
        
    @Test
    void createAndPlay2RoundsTournament() {
        
        // ************** SETUP TOURNAMENT **************
        
        Tournament tournament = tournamentService.createTournament(DEFAULT_PARAMS);
        
        tournamentService.updateContestants(tournament, Contestant.of(player1, player2, player3, player4));
        tournamentService.createMatches(tournament.id());
        tournament = tournamentService.findTournament(tournament.id()).get();
        
        assertEquals(List.of(
                new Contestant(player1, 1), 
                new Contestant(player2, 2),
                new Contestant(player3, 3),
                new Contestant(player4, 4)), tournament.contestants());
        
        assertEquals(1, tournament.boards().size());
        TournamentBoard mainBoard = tournament.mainBoard();
        
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
        
        // ************** PLAY MATCHES **************
        
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
        
        TournamentSummary tournamentSummary = tournamentService.loadDailyTournamentSummariesList().get(0);
        
        assertEquals(Status.COMPLETED, tournamentSummary.status());
        assertEquals(player1, tournamentSummary.winner());
    }
    
    @Test
    void tournamentWith6PlayersAnd3RoundsGenerateSemifinals() {
        
        Tournament tournament = tournamentService.createTournament(DEFAULT_PARAMS);
        
        tournamentService.updateContestants(tournament, Contestant.of(player1, Player.BYE, player2, player3, player4, player5, Player.BYE, player6));
        tournamentService.createMatches(tournament.id());
        tournament = tournamentService.findTournament(tournament.id()).get();
        
        assertEquals(List.of(
                new Contestant(player1, 1),
                new Contestant(player2, 3),
                new Contestant(player3, 4),
                new Contestant(player4, 5),
                new Contestant(player5, 6),
                new Contestant(player6, 8)), tournament.contestants());
        
        var matches = tournament.mainBoard().matches();
        assertEquals(6, matches.size());
        
        assertEquals(Player.BYE, matches.get(1).player2());
        assertEquals(Player.BYE, matches.get(4).player1());
        
        Match semiFinal1 = matches.get(5);
        assertEquals(player1, semiFinal1.player1());
        assertNull(semiFinal1.player2());

        Match semiFinal2 = matches.get(6);
        assertNull(semiFinal2.player1());
        assertEquals(player6, semiFinal2.player2());
    }
    
    @Test
    void tournamentWith3RoundsAndConsolation() {
        
        TournamentParams params = new TournamentParams(Organization.KVTK, Type.DAILY, Level.L250, Level.L250, LocalDate.of(2022, 1, 1), "Napi", "Mini Garros", Structure.BOARD_AND_CONSOLATION, 3);
        Tournament tournament = tournamentService.createTournament(params);
        
        tournamentService.updateContestants(tournament, Contestant.of(player1, player2, player3, player4, player5, player6, player7, player8));
        tournamentService.createMatches(tournament.id());
        var mainBoardMatches = tournamentService.findTournament(tournament.id()).get().mainBoard().matches();
        
        Match quarterFinal1 = mainBoardMatches.get(1);
        tournamentService.setTournamentMatchResult(new MatchResultInfo(quarterFinal1, LocalDate.of(2022,1,1), new MatchResult(6, 4)));
        
        var consolationMatches = tournamentService.findTournament(tournament.id()).get().consolationBoard().matches();
        assertEquals(1, consolationMatches.size());
        
        assertEquals(player2, consolationMatches.get(1).player1());
        assertNull(consolationMatches.get(1).player2());
        
        Match quarterFinal2 = mainBoardMatches.get(2);
        tournamentService.setTournamentMatchResult(new MatchResultInfo(quarterFinal2, LocalDate.of(2022,1,1), new MatchResult(0, 6)));
        
        consolationMatches = tournamentService.findTournament(tournament.id()).get().consolationBoard().matches();
        assertEquals(1, consolationMatches.size());
        
        assertEquals(player2, consolationMatches.get(1).player1());
        assertEquals(player3, consolationMatches.get(1).player2());
        
    }
    
}
