package hu.kits.tennis.infrastructure.database;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.jdbi.v3.core.Jdbi;

import hu.kits.tennis.common.CollectionsUtil;
import hu.kits.tennis.domain.tournament.TournamentMatches;
import hu.kits.tennis.domain.utr.BookedMatch;
import hu.kits.tennis.domain.utr.Match;
import hu.kits.tennis.domain.utr.MatchRepository;
import hu.kits.tennis.domain.utr.MatchResult;
import hu.kits.tennis.domain.utr.MatchResultInfo;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.domain.utr.PlayerRepository;
import hu.kits.tennis.domain.utr.Players;
import hu.kits.tennis.domain.utr.UTR;

public class MatchJdbcRepository implements MatchRepository  {

    private static final String TABLE_TENNIS_MATCH = "TENNIS_MATCH";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_TOURNAMENT_ID = "TOURNAMENT_ID";
    private static final String COLUMN_TOURNAMENT_BOARD_NUMBER = "TOURNAMENT_BOARD_NUMBER";
    private static final String COLUMN_TOURNAMENT_MATCH_NUMBER = "TOURNAMENT_MATCH_NUMBER";
    private static final String COLUMN_DATETIME = "DATETIME";
    private static final String COLUMN_PLAYER1_ID = "PLAYER1_ID";
    private static final String COLUMN_PLAYER2_ID = "PLAYER2_ID";
    private static final String COLUMN_RESULT = "RESULT";
    private static final String COLUMN_PLAYER1_UTR = "PLAYER1_UTR";
    private static final String COLUMN_PLAYER2_UTR = "PLAYER2_UTR";
    private static final String COLUMN_MATCH_UTR_FOR_PLAYER1 = "MATCH_UTR_FOR_PLAYER1";
    private static final String COLUMN_MATCH_UTR_FOR_PLAYER2 = "MATCH_UTR_FOR_PLAYER2";
    
    private final Jdbi jdbi;
    private final PlayerRepository playerRepository;
    
    public MatchJdbcRepository(DataSource dataSource, PlayerRepository playerRepository) {
        jdbi = Jdbi.create(dataSource);
        this.playerRepository = playerRepository;
    }

    @Override
    public List<BookedMatch> loadAllPlayedMatches(Player player) {
        String sql = String.format("SELECT * FROM %s WHERE (%s = :playerId OR %s = :playerId) AND %s IS NOT NULL ORDER BY %s", TABLE_TENNIS_MATCH, COLUMN_PLAYER1_ID, COLUMN_PLAYER2_ID, COLUMN_RESULT, COLUMN_DATETIME);
        
        Players players = playerRepository.loadAllPlayers();
        
        return jdbi.withHandle(handle -> 
            handle.createQuery(sql)
            .bind("playerId", player.id())
            .map((rs, ctx) -> mapToBookedMatch(rs, players)).list());
    }

    @Override
    public List<BookedMatch> loadAllBookedMatches() {
        String sql = String.format("SELECT * FROM %s WHERE %s IS NOT NULL ORDER BY %s", TABLE_TENNIS_MATCH, COLUMN_RESULT, COLUMN_DATETIME);
        
        Players players = playerRepository.loadAllPlayers();
        
        return jdbi.withHandle(handle -> 
            handle.createQuery(sql)
            .map((rs, ctx) -> mapToBookedMatch(rs,players)).list());
    }
    
    @Override
    public List<BookedMatch> loadAllBookedMatchesForTournament(String tournamentId) {
        String sql = String.format("SELECT * FROM %s WHERE %s = :tournamentId AND %s IS NOT NULL ORDER BY %s", TABLE_TENNIS_MATCH, COLUMN_TOURNAMENT_ID, COLUMN_RESULT, COLUMN_DATETIME);
        
        Players players = playerRepository.loadAllPlayers();
        
        return jdbi.withHandle(handle -> 
            handle.createQuery(sql)
            .bind("tournamentId", tournamentId)
            .map((rs, ctx) -> mapToBookedMatch(rs, players)).list());
    }
    
    @Override
    public TournamentMatches loadMatchesForTournament(String tournamentId) {
        String sql = String.format("SELECT * FROM %s WHERE %s = :tournamentId ORDER BY %s", TABLE_TENNIS_MATCH, COLUMN_TOURNAMENT_ID, COLUMN_TOURNAMENT_MATCH_NUMBER);
        
        Players players = playerRepository.loadAllPlayers();
        
        List<Match> matchesList = jdbi.withHandle(handle -> 
            handle.createQuery(sql)
            .bind("tournamentId", tournamentId)
            .map((rs, ctx) -> mapToMatch(rs,players)).list());
        
        Map<Integer, List<Match>> matchesByBoard = matchesList.stream().collect(groupingBy(Match::tournamentBoardNumber));
        
        Map<Integer, Map<Integer, Match>> matchesByBoardAndNumber = CollectionsUtil.mapValues(matchesByBoard, matches -> matches.stream().collect(toMap(Match::tournamentMatchNumber, Function.identity())));
        
        return new TournamentMatches(matchesByBoardAndNumber);
    }
    
    private static Match mapToMatch(ResultSet rs, Players players) throws SQLException {
        
        Player player1 = JdbiUtil.mapToOptionalInt(rs, COLUMN_PLAYER1_ID).map(players::get).orElse(null);
        Player player2 = JdbiUtil.mapToOptionalInt(rs, COLUMN_PLAYER2_ID).map(players::get).orElse(null);
        
        return new Match(
                rs.getInt(COLUMN_ID),
                rs.getString(COLUMN_TOURNAMENT_ID),
                JdbiUtil.mapToOptionalInt(rs, COLUMN_TOURNAMENT_BOARD_NUMBER).orElse(null),
                JdbiUtil.mapToOptionalInt(rs, COLUMN_TOURNAMENT_MATCH_NUMBER).orElse(null),
                Optional.ofNullable(rs.getDate(COLUMN_DATETIME)).map(Date::toLocalDate).orElse(null),
                player1, 
                player2, 
                MatchResult.parse(rs.getString(COLUMN_RESULT)));
    }

    private static BookedMatch mapToBookedMatch(ResultSet rs, Players players) throws SQLException {
        return new BookedMatch(
                new Match(
                    rs.getInt(COLUMN_ID),
                    rs.getString(COLUMN_TOURNAMENT_ID),
                    JdbiUtil.mapToOptionalInt(rs, COLUMN_TOURNAMENT_BOARD_NUMBER).orElse(null),
                    JdbiUtil.mapToOptionalInt(rs, COLUMN_TOURNAMENT_MATCH_NUMBER).orElse(null),
                    rs.getDate(COLUMN_DATETIME).toLocalDate(),
                    players.get(rs.getInt(COLUMN_PLAYER1_ID)), 
                    players.get(rs.getInt(COLUMN_PLAYER2_ID)), 
                    MatchResult.parse(rs.getString(COLUMN_RESULT))),
                readUTR(rs, COLUMN_PLAYER1_UTR), 
                readUTR(rs, COLUMN_PLAYER2_UTR),
                readUTR(rs, COLUMN_MATCH_UTR_FOR_PLAYER1),
                readUTR(rs, COLUMN_MATCH_UTR_FOR_PLAYER2));
    }
    
    private static UTR readUTR(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        if(rs.wasNull()) {
            return UTR.UNDEFINED;
        } else {
            return new UTR(value);
        }
    }

    @Override
    public BookedMatch save(BookedMatch bookedMatch) {
        
        if(bookedMatch.playedMatch().id() != null) {
            deleteMatch(bookedMatch.playedMatch().id());
        }
        
        Map<String, Object> map = createMap(bookedMatch);
        int matchId = jdbi.withHandle(handle -> JdbiUtil.createInsertStatement(handle, TABLE_TENNIS_MATCH, map)
                .executeAndReturnGeneratedKeys(COLUMN_ID)
                .mapTo(Integer.class)
                .one());
        return updateWithId(bookedMatch, matchId);
    }
    
    private static BookedMatch updateWithId(BookedMatch bookedMatch, int matchId) {
        return new BookedMatch(
                new Match(matchId, bookedMatch.playedMatch().tournamentId(),
                        bookedMatch.playedMatch().tournamentBoardNumber(),
                        bookedMatch.playedMatch().tournamentMatchNumber(),
                        bookedMatch.playedMatch().date(),
                        bookedMatch.playedMatch().player1(), bookedMatch.playedMatch().player2(), 
                        bookedMatch.playedMatch().result()),
                bookedMatch.player1UTR(), bookedMatch.player2UTR(), bookedMatch.matchUTRForPlayer1(), bookedMatch.matchUTRForPlayer2());
    }
    
    private static Map<String, Object> createMap(BookedMatch bookedMatch) {
        
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(COLUMN_ID, bookedMatch.playedMatch().id());
        valuesMap.put(COLUMN_TOURNAMENT_ID, bookedMatch.playedMatch().tournamentId());
        valuesMap.put(COLUMN_TOURNAMENT_BOARD_NUMBER, bookedMatch.playedMatch().tournamentBoardNumber());
        valuesMap.put(COLUMN_TOURNAMENT_MATCH_NUMBER, bookedMatch.playedMatch().tournamentMatchNumber());
        valuesMap.put(COLUMN_DATETIME, bookedMatch.playedMatch().date());
        valuesMap.put(COLUMN_PLAYER1_ID, bookedMatch.playedMatch().player1() != null ? bookedMatch.playedMatch().player1().id() : null);
        valuesMap.put(COLUMN_PLAYER2_ID, bookedMatch.playedMatch().player2() != null ? bookedMatch.playedMatch().player2().id() : null);
        valuesMap.put(COLUMN_RESULT, bookedMatch.playedMatch().result() != null ? bookedMatch.playedMatch().result().serialize() : null);
        
        if(bookedMatch.player1UTR() != null && bookedMatch.player2UTR() != null && bookedMatch.matchUTRForPlayer1() != null && bookedMatch.matchUTRForPlayer2() != null) {
            valuesMap.put(COLUMN_PLAYER1_UTR, bookedMatch.player1UTR().value());
            valuesMap.put(COLUMN_PLAYER2_UTR, bookedMatch.player2UTR().value());
            valuesMap.put(COLUMN_MATCH_UTR_FOR_PLAYER1, bookedMatch.matchUTRForPlayer1().value());
            valuesMap.put(COLUMN_MATCH_UTR_FOR_PLAYER2, bookedMatch.matchUTRForPlayer2().value());
        }
        
        return valuesMap;
    }
    
    private static Map<String, Object> createMap(Match playedMatch) {
        
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(COLUMN_ID, playedMatch.id());
        valuesMap.put(COLUMN_TOURNAMENT_ID, playedMatch.tournamentId());
        valuesMap.put(COLUMN_TOURNAMENT_BOARD_NUMBER, playedMatch.tournamentBoardNumber());
        valuesMap.put(COLUMN_TOURNAMENT_MATCH_NUMBER, playedMatch.tournamentMatchNumber());
        valuesMap.put(COLUMN_DATETIME, playedMatch.date());
        valuesMap.put(COLUMN_PLAYER1_ID, playedMatch.player1() != null ? playedMatch.player1().id() : null);
        valuesMap.put(COLUMN_PLAYER2_ID, playedMatch.player2() != null ? playedMatch.player2().id() : null);
        valuesMap.put(COLUMN_RESULT, playedMatch.result() != null ? playedMatch.result().serialize() : null);
        return valuesMap;
    }
    
    @Override
    public void setResult(MatchResultInfo matchResultInfo) {
        int matchId = matchResultInfo.match().id();
        MatchResult matchResult = matchResultInfo.matchResult();
        LocalDate date = matchResultInfo.date();
        jdbi.useHandle(handle -> JdbiUtil.executeSimpleUpdate(jdbi, TABLE_TENNIS_MATCH, COLUMN_RESULT, matchResult != null ? matchResult.serialize() : null, COLUMN_ID, matchId));
        jdbi.useHandle(handle -> JdbiUtil.executeSimpleUpdate(jdbi, TABLE_TENNIS_MATCH, COLUMN_DATETIME, date, COLUMN_ID, matchId));
    }
    
    @Override
    public void setPlayer1(int matchId, Player player) {
        jdbi.useHandle(handle -> JdbiUtil.executeSimpleUpdate(jdbi, TABLE_TENNIS_MATCH, COLUMN_PLAYER1_ID, player.id(), COLUMN_ID, matchId));
    }

    @Override
    public void setPlayer2(int matchId, Player player) {
        jdbi.useHandle(handle -> JdbiUtil.executeSimpleUpdate(jdbi, TABLE_TENNIS_MATCH, COLUMN_PLAYER2_ID, player != null ? player.id() : null, COLUMN_ID, matchId));
    }
    
    @Override
    public void deleteMatch(int matchId) {
        
        jdbi.withHandle(handle -> handle.execute(String.format("DELETE FROM %s WHERE %s = ?", TABLE_TENNIS_MATCH, COLUMN_ID), matchId));
    }

    @Override
    public void replaceAllBookedMatches(List<BookedMatch> recalculatedBookedMatches) {
        List<Integer> matchIds = recalculatedBookedMatches.stream().map(m -> m.playedMatch().id()).collect(toList());
        // TODO do it wit updates
        deleteMatches(matchIds);
        
        if(! recalculatedBookedMatches.isEmpty()) {
            List<Map<String, Object>> values = recalculatedBookedMatches.stream()
                    .map(bookedMatch -> createMap(bookedMatch))
                    .collect(Collectors.toList());
            
            Set<String> columns = values.get(0).keySet();
            jdbi.withHandle(handle -> JdbiUtil.createBatchInsertStatement(handle, TABLE_TENNIS_MATCH, columns, values).execute());
        }
    }

    private void deleteMatches(List<Integer> matchIds) {
        jdbi.withHandle(handle -> handle.createUpdate(String.format("DELETE FROM %s WHERE %s IN (<matchIds>)", TABLE_TENNIS_MATCH, COLUMN_ID))
                .bindList("matchIds", matchIds)
                .execute());
    }

    @Override
    public void deleteMatchesForTournament(String tournamentId) {
        jdbi.withHandle(handle -> handle.execute(String.format("DELETE FROM %s WHERE %s = ?", TABLE_TENNIS_MATCH, COLUMN_TOURNAMENT_ID), tournamentId));
    }

    @Override
    public void updateTournament(int matchId, String tournamentId, int boardNumber, int tournamentMatchNumber) {
        jdbi.useHandle(handle -> JdbiUtil.executeUpdate(jdbi, TABLE_TENNIS_MATCH, Map.of(),
                Map.of(COLUMN_TOURNAMENT_ID, tournamentId,
                       COLUMN_TOURNAMENT_BOARD_NUMBER, boardNumber,
                       COLUMN_TOURNAMENT_MATCH_NUMBER, tournamentMatchNumber), COLUMN_ID, String.valueOf(matchId)));
    }

    @Override
    public void save(List<Match> playedMatches) {
        List<Map<String, Object>> values = playedMatches.stream()
                .map(bookedMatch -> createMap(bookedMatch))
                .collect(Collectors.toList());
        
        Set<String> columns = values.get(0).keySet();
        jdbi.withHandle(handle -> JdbiUtil.createBatchInsertStatement(handle, TABLE_TENNIS_MATCH, columns, values).execute());
    }

}
