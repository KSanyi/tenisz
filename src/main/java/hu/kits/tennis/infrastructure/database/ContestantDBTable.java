package hu.kits.tennis.infrastructure.database;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.jdbi.v3.core.Jdbi;

import hu.kits.tennis.common.Pair;
import hu.kits.tennis.domain.tournament.Contestant;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.domain.utr.Players;

class ContestantDBTable {

    private static final String TABLE_TOURNAMENT_CONTESTANT = "TOURNAMENT_CONTESTANT";
    private static final String COLUMN_TOURNAMENT_ID = "TOURNAMENT_ID";
    private static final String COLUMN_PLAYER_ID = "PLAYER_ID";
    private static final String COLUMN_RANK_NUMBER = "RANK_NUMBER";
    
    private final Jdbi jdbi;
    
    ContestantDBTable(DataSource dataSource) {
        jdbi = Jdbi.create(dataSource);
    }
    
    Map<String, List<Contestant>> loadAllContestantsByTournament(Players players) {
        String sql = String.format("SELECT * FROM %s", TABLE_TOURNAMENT_CONTESTANT);
        
        List<Pair<String, Contestant>> list = jdbi.withHandle(handle -> 
            handle.createQuery(sql)
            .map((rs, ctx) -> mapToTournamentIdContestantPair(rs, players)).list());
        
        return list.stream().collect(groupingBy(Pair::first, mapping(Pair::second, toList())));
    }
    
    List<Contestant> loadAllContestantsForTournament(Players players, String tournamentId) {
        String sql = String.format("SELECT * FROM %s WHERE %s = :tournamentId ORDER BY %s", TABLE_TOURNAMENT_CONTESTANT, COLUMN_TOURNAMENT_ID, COLUMN_RANK_NUMBER);
        
        List<Pair<String, Contestant>> list = jdbi.withHandle(handle -> 
            handle.createQuery(sql)
            .bind("tournamentId", tournamentId)
            .map((rs, ctx) -> mapToTournamentIdContestantPair(rs, players)).list());
        
        return list.stream().map(Pair::second).collect(toList());
    }
    
    private static Pair<String, Contestant> mapToTournamentIdContestantPair(ResultSet rs, Players players) throws SQLException {
        
        Player player = players.get(rs.getInt(COLUMN_PLAYER_ID));
        
        return Pair.of(rs.getString(COLUMN_TOURNAMENT_ID), 
                new Contestant(player, rs.getInt(COLUMN_RANK_NUMBER)));
    }

    void updateContestants(String tournamentId, List<Contestant> contestants) {
        
        deleteContestants(tournamentId);
        saveContestants(tournamentId, contestants);
    }
    
    private void saveContestants(String tournamentId, List<Contestant> contestants) {
        
        var values = contestants.stream().map(contstant -> createMap(tournamentId, contstant)).collect(toList());
        
        jdbi.withHandle(handle -> 
            JdbiUtil.createBatchInsertStatement(handle, TABLE_TOURNAMENT_CONTESTANT, Set.of(COLUMN_TOURNAMENT_ID, COLUMN_PLAYER_ID, COLUMN_RANK_NUMBER), values)
                .execute());
    }
    
    private static Map<String, Object> createMap(String tournamentId, Contestant contestant) {
        
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(COLUMN_TOURNAMENT_ID, tournamentId);
        valuesMap.put(COLUMN_PLAYER_ID, contestant.player().id());
        valuesMap.put(COLUMN_RANK_NUMBER, contestant.rank());
        
        return valuesMap;
    }

    void deleteContestants(String tournamentId) {
        
        jdbi.withHandle(handle -> handle.execute(String.format("DELETE FROM %s WHERE %s = ?", TABLE_TOURNAMENT_CONTESTANT, COLUMN_TOURNAMENT_ID), tournamentId));
    }

}
