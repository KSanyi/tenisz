package hu.kits.tennis.infrastructure.database;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.jdbi.v3.core.Jdbi;

import hu.kits.tennis.common.CollectionsUtil;
import hu.kits.tennis.common.Pair;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.Players;
import hu.kits.tennis.domain.tournament.Contestant;
import hu.kits.tennis.domain.tournament.PaymentStatus;

class ContestantDBTable {

    private static final String TABLE_TOURNAMENT_CONTESTANT = "TENISZ_TOURNAMENT_CONTESTANT";
    private static final String COLUMN_TOURNAMENT_ID = "TOURNAMENT_ID";
    private static final String COLUMN_PLAYER_ID = "PLAYER_ID";
    private static final String COLUMN_RANK_NUMBER = "RANK_NUMBER";
    private static final String COLUMN_POSITION = "POSITION";
    private static final String COLUMN_PAYMENT_STATUS = "PAYMENT_STATUS";
    
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
                new Contestant(player, 
                        rs.getInt(COLUMN_RANK_NUMBER),
                        PaymentStatus.valueOf(rs.getString(COLUMN_PAYMENT_STATUS)),
                        rs.getInt(COLUMN_POSITION)));
    }

    void updateContestants(String tournamentId, List<Contestant> contestants) {
        
        String sql = String.format("SELECT * FROM %s WHERE %s = :tournamentId ORDER BY %s", TABLE_TOURNAMENT_CONTESTANT, COLUMN_TOURNAMENT_ID, COLUMN_RANK_NUMBER);
        
        List<ContestantRecord> currentRecords = jdbi.withHandle(handle -> 
            handle.createQuery(sql)
            .bind("tournamentId", tournamentId)
            .map((rs, ctx) -> ContestantRecord.fromResultSet(rs)).list());
        
        
        List<Integer> currentPlayerIds = currentRecords.stream().map(c -> c.playerId()).toList();
        List<Integer> newPlayerIds = contestants.stream().map(c -> c.player().id()).toList();
        
        Set<Integer> playerIdsToDelete = CollectionsUtil.diff(currentPlayerIds, newPlayerIds);
        if(!playerIdsToDelete.isEmpty()) {
            deleteContestants(tournamentId, playerIdsToDelete);
        }
        
        Set<Integer> playerIdsToAdd = CollectionsUtil.diff(newPlayerIds, currentPlayerIds);
        if(!playerIdsToAdd.isEmpty()) {
            List<Contestant> contestantsToAdd = contestants.stream().filter(c -> playerIdsToAdd.contains(c.player().id())).toList();
            saveContestants(tournamentId, contestantsToAdd);
        }
        
        Set<Integer> playerIdsToUpdate = CollectionsUtil.intersection(currentPlayerIds, newPlayerIds);
        
        for(int playerId : playerIdsToUpdate) {
            Contestant updatedContestant = contestants.stream().filter(c -> c.player().id().equals(playerId)).findFirst().get();
            ContestantRecord currentContestant = currentRecords.stream().filter(c -> c.playerId() == playerId).findFirst().get();
            
            JdbiUtil.executeUpdate(jdbi, TABLE_TOURNAMENT_CONTESTANT,
                    createMap(tournamentId, currentContestant),
                    createMap(tournamentId, ContestantRecord.fromContestant(updatedContestant)),
                    Map.of(COLUMN_TOURNAMENT_ID, tournamentId, COLUMN_PLAYER_ID, playerId));
        }
    }
    
    private static record ContestantRecord(int playerId, int rank, Integer position, PaymentStatus paymentStatus) {

        public static ContestantRecord fromResultSet(ResultSet rs) throws SQLException {
            return new ContestantRecord(
                    rs.getInt(COLUMN_PLAYER_ID),
                    rs.getInt(COLUMN_RANK_NUMBER),
                    rs.getInt(COLUMN_POSITION),
                    PaymentStatus.valueOf(rs.getString(COLUMN_PAYMENT_STATUS)));
        }

        public static ContestantRecord fromContestant(Contestant contestant) {
            return new ContestantRecord(contestant.player().id(), contestant.rank(), null, contestant.paymentStatus());
        }
    }
    
    private void saveContestants(String tournamentId, List<Contestant> contestants) {
        
        var values = contestants.stream().map(contestant -> createMap(tournamentId, ContestantRecord.fromContestant(contestant))).collect(toList());
        
        jdbi.withHandle(handle -> 
            JdbiUtil.createBatchInsertStatement(handle, TABLE_TOURNAMENT_CONTESTANT, Set.of(COLUMN_TOURNAMENT_ID, COLUMN_PLAYER_ID, COLUMN_RANK_NUMBER, COLUMN_PAYMENT_STATUS), values)
                .execute());
    }
    
    private static Map<String, Object> createMap(String tournamentId, ContestantRecord contestantRecord) {
        
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(COLUMN_TOURNAMENT_ID, tournamentId);
        valuesMap.put(COLUMN_PLAYER_ID, contestantRecord.playerId());
        valuesMap.put(COLUMN_RANK_NUMBER, contestantRecord.rank());
        valuesMap.put(COLUMN_PAYMENT_STATUS, contestantRecord.paymentStatus().name());
        
        return valuesMap;
    }

    void deleteContestants(String tournamentId) {
        
        jdbi.withHandle(handle -> handle.execute(String.format("DELETE FROM %s WHERE %s = ?", TABLE_TOURNAMENT_CONTESTANT, COLUMN_TOURNAMENT_ID), tournamentId));
    }
    
    private void deleteContestants(String tournamentId, Collection<Integer> playerIds) {
        String sql = String.format("DELETE FROM %s WHERE %s = :tournamentId AND %s IN (<playerIds>)", TABLE_TOURNAMENT_CONTESTANT, COLUMN_TOURNAMENT_ID, COLUMN_PLAYER_ID);
        jdbi.withHandle(handle -> handle.createUpdate(sql)
                .bind("tournamentId", tournamentId)
                .bindList("playerIds", playerIds)
                .execute());
    }

    public Map<String, Integer> countPlayersByTournament() {
        String sql = String.format("SELECT %s, COUNT(*) FROM %s GROUP BY %s", COLUMN_TOURNAMENT_ID, TABLE_TOURNAMENT_CONTESTANT, COLUMN_TOURNAMENT_ID);
        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .map((rs, ctx) -> Pair.of(rs.getString(1), rs.getInt(2))).list()).stream()
                    .collect(toMap(Pair::first, Pair::second));
    }

    public Map<String, Integer> findWinnerByTournament() {
        String sql = String.format("SELECT %s, %s FROM %s WHERE %s = 1", COLUMN_TOURNAMENT_ID, COLUMN_PLAYER_ID, TABLE_TOURNAMENT_CONTESTANT, COLUMN_POSITION);
        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .map((rs, ctx) -> Pair.of(rs.getString(1), rs.getInt(2))).list()).stream()
                    .collect(toMap(Pair::first, Pair::second));
    }

    public void setPosition(String tournamentId, int playerId, int position) {
        Integer oldWinnerId = findWinnerByTournament().get(tournamentId);
        if(oldWinnerId != null) {
            String sql = String.format("UPDATE %s SET %s = NULL WHERE %s = :tournamentId AND %s = :playerId", TABLE_TOURNAMENT_CONTESTANT, COLUMN_POSITION, COLUMN_TOURNAMENT_ID, COLUMN_PLAYER_ID);
            int x = jdbi.withHandle(handle -> handle.createUpdate(sql)
                    .bind("tournamentId", tournamentId)
                    .bind("playerId", oldWinnerId)
                    .execute());
            System.out.println(x);
        }
        JdbiUtil.executeUpdate(jdbi, TABLE_TOURNAMENT_CONTESTANT, Map.of(), Map.of(COLUMN_POSITION, position), Map.of(COLUMN_TOURNAMENT_ID, tournamentId, COLUMN_PLAYER_ID, playerId));
    }

    public void setPaymentStatus(String tournamentId, int playerId, PaymentStatus status) {
        JdbiUtil.executeUpdate(jdbi, TABLE_TOURNAMENT_CONTESTANT, Map.of(), Map.of(COLUMN_PAYMENT_STATUS, status.name()), Map.of(COLUMN_TOURNAMENT_ID, tournamentId, COLUMN_PLAYER_ID, playerId));
    }
    
    List<String> loadTournamentIdsForPlayer(int playerId) {
        String sql = String.format("SELECT %s FROM %s WHERE %s = :playerId", COLUMN_TOURNAMENT_ID, TABLE_TOURNAMENT_CONTESTANT, COLUMN_PLAYER_ID);
        
        return  jdbi.withHandle(handle -> 
            handle.createQuery(sql)
                .bind("playerId", playerId)
                .map((rs, ctx) -> rs.getString(COLUMN_TOURNAMENT_ID)).list());
    }

}
