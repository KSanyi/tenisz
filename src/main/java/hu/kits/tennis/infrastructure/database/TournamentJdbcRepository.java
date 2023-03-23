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
import hu.kits.tennis.domain.tournament.BasicTournamentInfo;
import hu.kits.tennis.domain.tournament.Contestant;
import hu.kits.tennis.domain.tournament.Organization;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentBoard;
import hu.kits.tennis.domain.tournament.TournamentMatches;
import hu.kits.tennis.domain.tournament.TournamentParams;
import hu.kits.tennis.domain.tournament.TournamentRepository;
import hu.kits.tennis.domain.tournament.TournamentSummary;
import hu.kits.tennis.domain.tournament.TournamentParams.Level;
import hu.kits.tennis.domain.tournament.TournamentParams.Status;
import hu.kits.tennis.domain.tournament.TournamentParams.Structure;
import hu.kits.tennis.domain.tournament.TournamentParams.Type;

public class TournamentJdbcRepository implements TournamentRepository {

    private static final String TABLE_TOURNAMENT = "TOURNAMENT";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_ORGANIZATION = "ORGANIZER";
    private static final String COLUMN_DATE = "DATE";
    private static final String COLUMN_NAME = "NAME";
    private static final String COLUMN_VENUE = "VENUE";
    private static final String COLUMN_TYPE = "TYPE";
    private static final String COLUMN_STRUCTURE = "STRUCTURE";
    private static final String COLUMN_LEVEL_FROM = "LEVEL_FROM";
    private static final String COLUMN_LEVEL_TO = "LEVEL_TO";
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
    public Map<String, BasicTournamentInfo> loadBasicTournamentInfosMap() {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public List<TournamentSummary> loadTournamentSummariesList() {
        
        Map<String, Integer> matchCountByTournament = matchRepository.countMatchesByTournament();
        Map<String, Integer> playerCountByTournament = contestantDBTable.countPlayersByTournament();
        Map<String, Integer> winnerIdByTournament = Map.of();//contestantDBTable.findWinnerByTournament();
        Players players = playerRepository.loadAllPlayers();
        Map<String, Player> winnerByTournament = CollectionsUtil.mapValues(winnerIdByTournament, players::get);
        
        String sql = String.format("SELECT * FROM %s", TABLE_TOURNAMENT);
        
        return jdbi.withHandle(handle -> 
            handle.createQuery(sql)
            .map((rs, ctx) -> mapToTournamentSummary(rs, 
                    matchCountByTournament, 
                    playerCountByTournament,
                    winnerByTournament)).list());
    }
    
    private static TournamentSummary mapToTournamentSummary(ResultSet rs, 
            Map<String, Integer> matchCountByTournament,
            Map<String, Integer> playerCountByTournament,
            Map<String, Player> winnerByTournament) throws SQLException {
        
        String tournamentId = rs.getString(COLUMN_ID);
        
        return new TournamentSummary(
                tournamentId,
                Organization.valueOf(rs.getString(COLUMN_ORGANIZATION)),
                Type.valueOf(rs.getString(COLUMN_TYPE)),
                Level.valueOf(rs.getString(COLUMN_LEVEL_FROM)),
                Level.valueOf(rs.getString(COLUMN_LEVEL_TO)),
                rs.getString(COLUMN_NAME),
                rs.getDate(COLUMN_DATE).toLocalDate(),
                Status.valueOf(rs.getString(COLUMN_STATUS)),
                matchCountByTournament.getOrDefault(tournamentId, 0),
                playerCountByTournament.getOrDefault(tournamentId, 0),
                winnerByTournament.get(tournamentId));
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
        
        Structure structure = Structure.valueOf(rs.getString(COLUMN_STRUCTURE));
        
        TournamentMatches tournamentMatches = matchesByTournament.getOrDefault(tournamentId, TournamentMatches.empty());
        
        List<TournamentBoard> boards = new ArrayList<>();
        
        int numberOfRounds = MathUtil.log2(contestants.size());
        boards.add(new TournamentBoard(numberOfRounds, tournamentMatches.matchesInBoard(1)));
        if(structure == Structure.BOARD_AND_CONSOLATION) {
            boards.add(new TournamentBoard(numberOfRounds - 1, tournamentMatches.matchesInBoard(2)));
        }
        
        return new Tournament(
                tournamentId,
                new TournamentParams(
                        Organization.valueOf(rs.getString(COLUMN_ORGANIZATION)),
                        Type.valueOf(rs.getString(COLUMN_TYPE)),
                        Level.valueOf(rs.getString(COLUMN_LEVEL_FROM)),
                        Level.valueOf(rs.getString(COLUMN_LEVEL_TO)),
                        rs.getDate(COLUMN_DATE).toLocalDate(),
                        rs.getString(COLUMN_NAME),
                        rs.getString(COLUMN_VENUE),
                        structure,
                        rs.getInt(COLUMN_BEST_OF_N_SETS)),
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
        
        TournamentParams params = tournament.params();
        
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(COLUMN_ID, tournament.id());
        valuesMap.put(COLUMN_ORGANIZATION, params.organization());
        valuesMap.put(COLUMN_TYPE, params.type());
        valuesMap.put(COLUMN_LEVEL_FROM, params.levelFrom());
        valuesMap.put(COLUMN_LEVEL_TO, params.levelTo());
        valuesMap.put(COLUMN_DATE, params.date());
        valuesMap.put(COLUMN_NAME, params.name());
        valuesMap.put(COLUMN_VENUE, params.venue());
        valuesMap.put(COLUMN_STRUCTURE, params.structure());
        valuesMap.put(COLUMN_BEST_OF_N_SETS, params.bestOfNSets());
        valuesMap.put(COLUMN_STATUS, tournament.status());
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
    public void updateTournamentType(String tournamentId, Structure structure) {
        jdbi.useHandle(handle -> JdbiUtil.executeSimpleUpdate(jdbi, TABLE_TOURNAMENT, COLUMN_TYPE, structure, COLUMN_ID, tournamentId));
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
