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
import hu.kits.tennis.domain.tournament.PaymentStatus;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentBoard;
import hu.kits.tennis.domain.tournament.TournamentMatches;
import hu.kits.tennis.domain.tournament.TournamentParams;
import hu.kits.tennis.domain.tournament.TournamentParams.Level;
import hu.kits.tennis.domain.tournament.TournamentParams.Status;
import hu.kits.tennis.domain.tournament.TournamentParams.Structure;
import hu.kits.tennis.domain.tournament.TournamentParams.Surface;
import hu.kits.tennis.domain.tournament.TournamentParams.Type;
import hu.kits.tennis.domain.tournament.TournamentParams.VenueType;
import hu.kits.tennis.domain.tournament.TournamentRepository;
import hu.kits.tennis.domain.tournament.TournamentSummary;
import hu.kits.tennis.domain.tournament.TournamentSummary.CourtInfo;

public class TournamentJdbcRepository implements TournamentRepository {

    private static final String TABLE_TOURNAMENT = "TENISZ_TOURNAMENT";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_ORGANIZATION = "ORGANIZER";
    private static final String COLUMN_DATE = "DATE";
    private static final String COLUMN_NAME = "NAME";
    private static final String COLUMN_VENUE = "VENUE";
    private static final String COLUMN_VENUE_TYPE = "VENUE_TYPE";
    private static final String COLUMN_NUMBER_OF_COURTS = "NUMBER_OF_COURTS";
    private static final String COLUMN_SURFACE = "SURFACE";
    private static final String COLUMN_TYPE = "TYPE";
    private static final String COLUMN_STRUCTURE = "STRUCTURE";
    private static final String COLUMN_LEVEL_FROM = "LEVEL_FROM";
    private static final String COLUMN_LEVEL_TO = "LEVEL_TO";
    private static final String COLUMN_BEST_OF_N_SETS = "BEST_OF_N_SETS";
    private static final String COLUMN_STATUS = "STATUS";
    private static final String COLUMN_DESCRIPTION = "DESCRIPTION";
    
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
        String sql = String.format("SELECT * FROM %s", TABLE_TOURNAMENT);
        
        List<BasicTournamentInfo> tournamentInfoList = jdbi.withHandle(handle -> handle.createQuery(sql)
            .map((rs, ctx) -> mapToBasicTournamentInfo(rs)).list());
        
        return CollectionsUtil.mapBy(tournamentInfoList, BasicTournamentInfo::id);
    }
    
    private static BasicTournamentInfo mapToBasicTournamentInfo(ResultSet rs) throws SQLException {
        
        return new BasicTournamentInfo(
                rs.getString(COLUMN_ID),
                Organization.valueOf(rs.getString(COLUMN_ORGANIZATION)),
                rs.getString(COLUMN_NAME),
                rs.getDate(COLUMN_DATE).toLocalDate());
    }
    
    @Override
    public List<TournamentSummary> loadTournamentSummariesList() {
        
        Map<String, Integer> matchCountByTournament = matchRepository.countMatchesByTournament();
        Map<String, Integer> playerCountByTournament = contestantDBTable.countPlayersByTournament();
        Map<String, Integer> winnerIdByTournament = contestantDBTable.findWinnerByTournament();
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
    
    public int countTournamentForPlayer(Player player) {
        return contestantDBTable.loadTournamentIdsForPlayer(player.id()).size();
    }
    
    private static TournamentSummary mapToTournamentSummary(ResultSet rs, 
            Map<String, Integer> matchCountByTournament,
            Map<String, Integer> playerCountByTournament,
            Map<String, Player> winnerByTournament) throws SQLException {
        
        String tournamentId = rs.getString(COLUMN_ID);
        
        Type type = Type.valueOf(rs.getString(COLUMN_TYPE));
        CourtInfo courtInfo = type == Type.DAILY ? new CourtInfo(
                rs.getInt(COLUMN_NUMBER_OF_COURTS),
                Surface.valueOf(rs.getString(COLUMN_SURFACE)),
                VenueType.valueOf(rs.getString(COLUMN_VENUE_TYPE))) : null;
        
        return new TournamentSummary(
                tournamentId,
                Organization.valueOf(rs.getString(COLUMN_ORGANIZATION)),
                type,
                Level.valueOf(rs.getString(COLUMN_LEVEL_FROM)),
                Level.valueOf(rs.getString(COLUMN_LEVEL_TO)),
                rs.getString(COLUMN_VENUE),
                courtInfo,
                rs.getString(COLUMN_NAME),
                rs.getDate(COLUMN_DATE).toLocalDate(),
                Status.valueOf(rs.getString(COLUMN_STATUS)),
                matchCountByTournament.getOrDefault(tournamentId, 0),
                playerCountByTournament.getOrDefault(tournamentId, 0),
                winnerByTournament.get(tournamentId),
                rs.getString(COLUMN_DESCRIPTION));
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
        if(params.type() == Type.DAILY) {
            valuesMap.put(COLUMN_VENUE, params.venue());
            valuesMap.put(COLUMN_NUMBER_OF_COURTS, params.courtInfo().numberOfCourts());
            valuesMap.put(COLUMN_SURFACE, params.courtInfo().surface().name());
            valuesMap.put(COLUMN_VENUE_TYPE, params.courtInfo().venueType());    
        }
        valuesMap.put(COLUMN_STRUCTURE, params.structure());
        valuesMap.put(COLUMN_BEST_OF_N_SETS, params.bestOfNSets());
        valuesMap.put(COLUMN_STATUS, tournament.status());
        valuesMap.put(COLUMN_DESCRIPTION, params.description());
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
    
    private static Tournament mapToTournament(ResultSet rs, Map<String, List<Contestant>> contestantsByTournament, Map<String, TournamentMatches> matchesByTournament) throws SQLException {
        
        String tournamentId = rs.getString(COLUMN_ID);
        
        List<Contestant> contestants = contestantsByTournament.getOrDefault(tournamentId, List.of());
        
        Structure structure = Structure.valueOf(rs.getString(COLUMN_STRUCTURE));
        
        TournamentMatches tournamentMatches = matchesByTournament.getOrDefault(tournamentId, TournamentMatches.empty());
        
        List<TournamentBoard> boards = new ArrayList<>();
        
        int numberOfRounds = MathUtil.log2(contestants.stream().mapToInt(c -> c.rank()).max().orElse(0));
        boards.add(new TournamentBoard(numberOfRounds, tournamentMatches.matchesInBoard(1)));
        if(structure == Structure.BOARD_AND_CONSOLATION) {
            boards.add(new TournamentBoard(numberOfRounds - 1, tournamentMatches.matchesInBoard(2)));
        }
        
        Type type = Type.valueOf(rs.getString(COLUMN_TYPE));
        CourtInfo courtInfo = type == Type.DAILY ? new CourtInfo(
                rs.getInt(COLUMN_NUMBER_OF_COURTS),
                Surface.valueOf(rs.getString(COLUMN_SURFACE)),
                VenueType.valueOf(rs.getString(COLUMN_VENUE_TYPE))) : null;
        
        return new Tournament(
                tournamentId,
                new TournamentParams(
                        Organization.valueOf(rs.getString(COLUMN_ORGANIZATION)),
                        type,
                        Level.valueOf(rs.getString(COLUMN_LEVEL_FROM)),
                        Level.valueOf(rs.getString(COLUMN_LEVEL_TO)),
                        rs.getDate(COLUMN_DATE).toLocalDate(),
                        rs.getString(COLUMN_NAME),
                        rs.getString(COLUMN_VENUE),
                        courtInfo,
                        structure,
                        rs.getInt(COLUMN_BEST_OF_N_SETS),
                        rs.getString(COLUMN_DESCRIPTION)),
                contestants,
                Status.valueOf(rs.getString(COLUMN_STATUS)),
                boards);
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

    @Override
    public void setWinner(String tournamentId, int winnerId) {
        contestantDBTable.setPosition(tournamentId, winnerId, 1);
    }
    
    @Override
    public void setPaymentStatus(String tournamentId, int playerId, PaymentStatus status) {
        contestantDBTable.setPaymentStatus(tournamentId, playerId, status);
    }

}
