package hu.kits.tennis.infrastructure.database;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.jdbi.v3.core.Jdbi;

import hu.kits.tennis.common.KITSException;
import hu.kits.tennis.domain.tournament.Organization;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.domain.utr.Player.Contact;
import hu.kits.tennis.domain.utr.PlayerRepository;
import hu.kits.tennis.domain.utr.Players;
import hu.kits.tennis.domain.utr.UTR;

public class PlayerJdbcRepository implements PlayerRepository {

    private static final String TABLE_PLAYER = "PLAYER";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_NAME = "NAME";
    private static final String COLUMN_EMAIL = "EMAIL";
    private static final String COLUMN_PHONE = "PHONE";
    private static final String COLUMN_COMMENT = "COMMENT";
    private static final String COLUMN_UTR_GROUP = "UTR_GROUP";
    private static final String COLUMN_ORGS = "ORGS";
    
    private final Jdbi jdbi;
    
    public PlayerJdbcRepository(DataSource dataSource) {
        jdbi = Jdbi.create(dataSource);
    }
    
    @Override
    public Players loadAllPlayers() {
        String sql = String.format("SELECT * FROM %s ORDER BY %s", TABLE_PLAYER, COLUMN_NAME);
        
        List<Player> playersList = jdbi.withHandle(handle -> 
            handle.createQuery(sql)
            .map((rs, ctx) -> mapToPlayer(rs)).list());
        
        return new Players(playersList);
    }
    
    private static Player mapToPlayer(ResultSet rs) throws SQLException {
        
        return new Player(
                rs.getInt(COLUMN_ID),
                rs.getString(COLUMN_NAME),
                new Contact(rs.getString(COLUMN_EMAIL), rs.getString(COLUMN_PHONE), rs.getString(COLUMN_COMMENT)),
                JdbiUtil.mapToOptionalDouble(rs, COLUMN_UTR_GROUP).map(UTR::of).orElse(UTR.UNDEFINED),
                mapToOrganisations(rs.getString(COLUMN_ORGS)));
    }

    @Override
    public Player saveNewPlayer(Player player) {
        
        Map<String, Object> map = createMap(player);
        int playerId = jdbi.withHandle(handle -> JdbiUtil.createInsertStatement(handle, TABLE_PLAYER, map).executeAndReturnGeneratedKeys(COLUMN_ID).mapTo(Integer.class).one());
        return new Player(playerId, player.name(), player.contact(), player.startingUTR(), player.organisations());
    }
    
    private static Map<String, Object> createMap(Player player) {
        
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(COLUMN_ID, player.id());
        valuesMap.put(COLUMN_NAME, player.name());
        valuesMap.put(COLUMN_EMAIL, player.contact().email());
        valuesMap.put(COLUMN_PHONE, player.contact().phone());
        valuesMap.put(COLUMN_COMMENT, player.contact().comment());
        valuesMap.put(COLUMN_UTR_GROUP, player.startingUTR().value());
        valuesMap.put(COLUMN_ORGS, mapToOrganisationsString(player.organisations()));
        
        return valuesMap;
    }
    
    private static Set<Organization> mapToOrganisations(String orgsString) {
        if(orgsString == null || orgsString.isBlank()) {
            return Set.of();
        } else {
            return Stream.of(orgsString.split(",")).map(String::trim).map(Organization::valueOf).collect(toSet());
        }
    }
    
    private static String mapToOrganisationsString(Set<Organization> organisations) {
        return organisations.stream().map(Organization::name).collect(joining(", "));
    }

    @Override
    public void updatePlayer(Player updatedPlayer) {
        
        int playerId = updatedPlayer.id();
        Optional<Player> originalPlayer = findPlayer(playerId);
        if(originalPlayer.isPresent()) {
            Map<String, Object> originalMap = createMap(originalPlayer.get());
            Map<String, Object> updatedMap = createMap(updatedPlayer);
            
            JdbiUtil.executeUpdate(jdbi, TABLE_PLAYER, originalMap, updatedMap, COLUMN_ID, String.valueOf(playerId));
        } else {
            throw new KITSException("Player '" + playerId + "' not found");
        }
    }
    
    public Optional<Player> findPlayer(int playerId) {
        String sql = String.format("SELECT * FROM %s WHERE %s = :id", TABLE_PLAYER, COLUMN_ID);
        
        return jdbi.withHandle(handle -> 
            handle.createQuery(sql)
            .bind("id", playerId)
            .map((rs, ctx) -> mapToPlayer(rs)).findFirst());
    }

    @Override
    public void deletePlayer(Player player) {
        
        jdbi.withHandle(handle -> handle.execute(String.format("DELETE FROM %s WHERE %s = ?", TABLE_PLAYER, COLUMN_ID), player.id()));
    }

}
