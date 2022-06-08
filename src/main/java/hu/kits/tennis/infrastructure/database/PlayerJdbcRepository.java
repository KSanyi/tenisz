package hu.kits.tennis.infrastructure.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.jdbi.v3.core.Jdbi;

import hu.kits.tennis.common.KITSException;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.domain.utr.PlayerRepository;
import hu.kits.tennis.domain.utr.Players;

public class PlayerJdbcRepository implements PlayerRepository {

    private static final String TABLE_PLAYER = "PLAYER";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_NAME = "NAME";
    private static final String COLUMN_UTR_GROUP = "UTR_GROUP";
    
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
                rs.getInt(COLUMN_UTR_GROUP));
    }

    @Override
    public Player saveNewPlayer(Player player) {
        
        Map<String, Object> map = createMap(player);
        int playerId = jdbi.withHandle(handle -> JdbiUtil.createInsertStatement(handle, TABLE_PLAYER, map).executeAndReturnGeneratedKeys(COLUMN_ID).mapTo(Integer.class).one());
        return new Player(playerId, player.name(), player.utrGroup());
    }
    
    private static Map<String, Object> createMap(Player player) {
        
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(COLUMN_ID, player.id());
        valuesMap.put(COLUMN_NAME, player.name());
        valuesMap.put(COLUMN_UTR_GROUP, player.utrGroup());
        
        return valuesMap;
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
    
    private Optional<Player> findPlayer(int playerId) {
        String sql = String.format("SELECT * FROM %s WHERE %s = :id", TABLE_PLAYER, COLUMN_ID);
        
        return jdbi.withHandle(handle -> 
            handle.createQuery(sql)
            .bind("id", playerId)
            .map((rs, ctx) -> mapToPlayer(rs)).findFirst());
    }

    @Override
    public void deletePlayer(Player player) {
        
        jdbi.withHandle(handle -> handle.execute(String.format("DELETE FROM %s WHERE %s = ?", TABLE_PLAYER, COLUMN_ID, player.id())));
    }

}
