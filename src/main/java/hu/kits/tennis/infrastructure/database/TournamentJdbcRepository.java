package hu.kits.tennis.infrastructure.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.jdbi.v3.core.Jdbi;

import hu.kits.tennis.domain.tournament.Contestant;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.Tournament.Status;
import hu.kits.tennis.domain.tournament.Tournament.Type;
import hu.kits.tennis.domain.tournament.TournamentRepository;
import hu.kits.tennis.domain.utr.Match;
import hu.kits.tennis.domain.utr.MatchRepository;
import hu.kits.tennis.domain.utr.PlayerRepository;
import hu.kits.tennis.domain.utr.Players;

public class TournamentJdbcRepository implements TournamentRepository {

    private static final String TABLE_TOURNAMENT = "TOURNAMENT";
    private static final String COLUMN_ID = "ID";
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
        
        String sql = String.format("SELECT * FROM %s", TABLE_TOURNAMENT);
        
        return jdbi.withHandle(handle -> 
            handle.createQuery(sql)
            .map((rs, ctx) -> mapToTournament(rs, contestantsByTournament, Map.of())).list());
    }
    
    private static Tournament mapToTournament(ResultSet rs, Map<String, List<Contestant>> contestantsByTournament, Map<String, List<Match>> matchesByTournament) throws SQLException {
        
        String tournamentId = rs.getString(COLUMN_ID);
        
        return new Tournament(
                tournamentId,
                rs.getDate(COLUMN_DATE).toLocalDate(),
                rs.getString(COLUMN_NAME),
                rs.getString(COLUMN_VENUE),
                Tournament.Type.valueOf(rs.getString(COLUMN_TYPE)),
                rs.getInt(COLUMN_BEST_OF_N_SETS),
                contestantsByTournament.getOrDefault(tournamentId, List.of()),
                Status.valueOf(rs.getString(COLUMN_STATUS)),
                matchesByTournament.getOrDefault(tournamentId, List.of()));
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
        List<Match> matches = matchRepository.loadMatchesForTournament(tournamentId);
        
        String sql = String.format("SELECT * FROM %s WHERE %s = :id", TABLE_TOURNAMENT, COLUMN_ID);
        
        return jdbi.withHandle(handle -> 
            handle.createQuery(sql)
            .bind("id", tournamentId)
            .map((rs, ctx) -> mapToTournament(rs, Map.of(tournamentId, contestants), Map.of(tournamentId, matches))).findFirst());
    }

    @Override
    public void deleteTournament(String tournamentId) {
        jdbi.withHandle(handle -> handle.execute(String.format("DELETE FROM %s WHERE %s = ?", TABLE_TOURNAMENT, COLUMN_ID), tournamentId));
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
