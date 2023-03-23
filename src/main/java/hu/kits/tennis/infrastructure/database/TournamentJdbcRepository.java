package hu.kits.tennis.infrastructure.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.jdbi.v3.core.Jdbi;

import hu.kits.tennis.common.CollectionsUtil;
import hu.kits.tennis.common.MathUtil;
import hu.kits.tennis.domain.match.MatchRepository;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.PlayerRepository;
import hu.kits.tennis.domain.player.Players;
import hu.kits.tennis.domain.tournament.Contestant;
import hu.kits.tennis.domain.tournament.Organization;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.Tournament.Board;
import hu.kits.tennis.domain.tournament.Tournament.Status;
import hu.kits.tennis.domain.tournament.Tournament.Type;
import hu.kits.tennis.domain.tournament.TournamentMatches;
import hu.kits.tennis.domain.tournament.TournamentRepository;

public class TournamentJdbcRepository implements TournamentRepository {

    private static final String TABLE_TOURNAMENT = "TOURNAMENT";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_ORGANIZATION = "ORGANIZER";
    private static final String COLUMN_DATE = "DATE";
    private static final String COLUMN_NAME = "NAME";
    private static final String COLUMN_VENUE = "VENUE";
    private static final String COLUMN_TYPE = "TYPE";
    private static final String COLUMN_BEST_OF_N_SETS = "BEST_OF_N_SETS";
    private static final String COLUMN_STATUS = "STATUS";
    
    private final Jdbi jdbi;
    
    private final ContestantDBTable contestantDBTable;
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    
    public TournamentJdbcRepository(DataSource dataSource, PlayerRepository playerRepository, MatchRepository matchRepository) {
        jdbi = Jdbi.create(dataSource);
        contestantDBTable = new ContestantDBTable(dataSource);
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
    }
    @Override
    public List<Tournament> loadAllTournaments() {
        
        Players players = playerRepository.loadAllPlayers();
        Map<String, List<Contestant>> contestantsByTournament = contestantDBTable.loadAllContestantsByTournament(players);

        //TODO
        String sql = String.format("SELECT * FROM %s", TABLE_TOURNAMENT);
        
        return jdbi.withHandle(handle -> 
            handle.createQuery(sql)
            .map((rs, ctx) -> mapToTournament(rs, contestantsByTournament, Map.of())).list());
    }
    
    @Override
    public List<Tournament> loadAllTournaments(Player player) {
        
        Players players = playerRepository.loadAllPlayers();
        Map<String, List<Contestant>> contestantsByTournament = contestantDBTable.loadAllContestantsByTournament(players);

        Map<String, List<Contestant>> contestantsByTournamentForPlayer = CollectionsUtil.filterByValue(contestantsByTournament, 
                contestants -> contestants.stream().anyMatch(c -> c.player() != null && c.player().id().equals(player.id())));
        
        if(contestantsByTournamentForPlayer.isEmpty()) {
            return List.of();
        }
        
        String sql = String.format("SELECT * FROM %s WHERE %s IN(<tournamentIds>)", TABLE_TOURNAMENT, COLUMN_ID);
        
        return jdbi.withHandle(handle -> 
            handle.createQuery(sql)
            .bindList("tournamentIds", contestantsByTournamentForPlayer.keySet())
            .map((rs, ctx) -> mapToTournament(rs, contestantsByTournamentForPlayer, Map.of())).list());
    }
    
    private static Tournament mapToTournament(ResultSet rs, Map<String, List<Contestant>> contestantsByTournament, Map<String, TournamentMatches> matchesByTournament) throws SQLException {
        
        String tournamentId = rs.getString(COLUMN_ID);
        
        List<Contestant> contestants = contestantsByTournament.getOrDefault(tournamentId, List.of());
        
        Tournament.Type type = Tournament.Type.valueOf(rs.getString(COLUMN_TYPE));
        
        TournamentMatches tournamentMatches = matchesByTournament.getOrDefault(tournamentId, TournamentMatches.empty());
        
        List<Board> boards = new ArrayList<>();
        
        int numberOfRounds = MathUtil.log2(contestants.size());
        boards.add(new Board(numberOfRounds, tournamentMatches.matchesInBoard(1)));
        if(type == Type.BOARD_AND_CONSOLATION) {
            boards.add(new Board(numberOfRounds - 1, tournamentMatches.matchesInBoard(2)));
        }
        
        return new Tournament(
                tournamentId,
                Organization.valueOf(rs.getString(COLUMN_ORGANIZATION)),
                rs.getDate(COLUMN_DATE).toLocalDate(),
                rs.getString(COLUMN_NAME),
                rs.getString(COLUMN_VENUE),
                type,
                rs.getInt(COLUMN_BEST_OF_N_SETS),
                contestants,
                Status.valueOf(rs.getString(COLUMN_STATUS)),
                boards);
    }
    
    @Override
    public void createTournament(Tournament tournament) {
        Map<String, Object> map = createMap(tournament);
        jdbi.withHandle(handle -> JdbiUtil.createInsertStatement(handle, TABLE_TOURNAMENT, map).execute());
    }

    private static Map<String, Object> createMap(Tournament tournament) {
        
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(COLUMN_ID, tournament.id());
        valuesMap.put(COLUMN_DATE, tournament.date());
        valuesMap.put(COLUMN_ORGANIZATION, tournament.organization());
        valuesMap.put(COLUMN_NAME, tournament.name());
        valuesMap.put(COLUMN_VENUE, tournament.venue());
        valuesMap.put(COLUMN_TYPE, tournament.type());
        valuesMap.put(COLUMN_STATUS, tournament.status());
        valuesMap.put(COLUMN_BEST_OF_N_SETS, tournament.bestOfNSets());
        return valuesMap;
    }

    @Override
    public Optional<Tournament> findTournament(String tournamentId) {
        
        Players players = playerRepository.loadAllPlayers();
        List<Contestant> contestants = contestantDBTable.loadAllContestantsForTournament(players, tournamentId);
        TournamentMatches matches = matchRepository.loadMatchesForTournament(tournamentId);
        
        String sql = String.format("SELECT * FROM %s WHERE %s = :id", TABLE_TOURNAMENT, COLUMN_ID);
        
        return jdbi.withHandle(handle -> 
            handle.createQuery(sql)
            .bind("id", tournamentId)
            .map((rs, ctx) -> mapToTournament(rs, Map.of(tournamentId, contestants), Map.of(tournamentId, matches))).findFirst());
    }

    @Override
    public void deleteTournament(String tournamentId) {
        jdbi.withHandle(handle -> handle.execute(String.format("DELETE FROM %s WHERE %s = ?", TABLE_TOURNAMENT, COLUMN_ID), tournamentId));
        contestantDBTable.deleteContestants(tournamentId);
    }
    
    @Override
    public void updateTournamentName(String tournamentId, String updatedName) {
        jdbi.useHandle(handle -> JdbiUtil.executeSimpleUpdate(jdbi, TABLE_TOURNAMENT, COLUMN_NAME, updatedName, COLUMN_ID, tournamentId));
    }
    
    @Override
    public void updateTournamentDate(String tournamentId, LocalDate updatedDate) {
        jdbi.useHandle(handle -> JdbiUtil.executeSimpleUpdate(jdbi, TABLE_TOURNAMENT, COLUMN_DATE, updatedDate, COLUMN_ID, tournamentId));
    }
    
    @Override
    public void updateTournamentVenue(String tournamentId, String venue) {
        jdbi.useHandle(handle -> JdbiUtil.executeSimpleUpdate(jdbi, TABLE_TOURNAMENT, COLUMN_VENUE, venue, COLUMN_ID, tournamentId));
    }
    
    @Override
    public void updateTournamentType(String tournamentId, Type type) {
        jdbi.useHandle(handle -> JdbiUtil.executeSimpleUpdate(jdbi, TABLE_TOURNAMENT, COLUMN_TYPE, type, COLUMN_ID, tournamentId));
    }
    
    @Override
    public void updateTournamentStatus(String tournamentId, Status status) {
        jdbi.useHandle(handle -> JdbiUtil.executeSimpleUpdate(jdbi, TABLE_TOURNAMENT, COLUMN_STATUS, status, COLUMN_ID, tournamentId));
    }
    
    @Override
    public void updateContestants(String id, List<Contestant> contestants) {
        contestantDBTable.updateContestants(id, contestants);
    }

}
