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
import hu.kits.tennis.common.Pair;
import hu.kits.tennis.domain.user.Role;
import hu.kits.tennis.domain.user.UserData;
import hu.kits.tennis.domain.user.UserData.Status;
import hu.kits.tennis.domain.user.UserRepository;
import hu.kits.tennis.domain.user.Users;

public class UserJdbcRepository implements UserRepository {

    private static final String TABLE_USER = "TENISZ_USER";
    private static final String COLUMN_USERID = "USERID";
    private static final String COLUMN_NAME = "NAME";
    private static final String COLUMN_PASSWORD_HASH = "PASSWORD_HASH";
    private static final String COLUMN_ROLE = "ROLE";
    private static final String COLUMN_PHONE = "PHONE";
    private static final String COLUMN_EMAIL = "EMAIL";
    private static final String COLUMN_STATUS = "STATUS";
    private static final String COLUMN_PLAYER_ID = "PLAYER_ID";
    
    private final Jdbi jdbi;
    
    public UserJdbcRepository(DataSource dataSource) {
        jdbi = Jdbi.create(dataSource);
    }
    
    @Override
    public Users loadAllUsers() {
        String sql = String.format("SELECT * FROM %s", TABLE_USER);
        
        List<UserData> usersList = jdbi.withHandle(handle -> 
            handle.createQuery(sql)
            .map((rs, ctx) -> mapToUser(rs)).list());
        
        return new Users(usersList);
    }
    
    @Override
    public Optional<UserData> findUserByEmail(String email) {
        String sql = String.format("SELECT * FROM %s WHERE %s = :email", TABLE_USER, COLUMN_EMAIL);
        return jdbi.withHandle(handle -> 
            handle.createQuery(sql)
                .bind("email", email)
                .map((rs, ctx) -> mapToUser(rs)).findOne());
    }
    
    @Override
    public Optional<Pair<UserData, String>> findUserWithPasswordHash(String userIdOrEmail) {
        
        String sql = String.format("SELECT * FROM %s WHERE %s = :userIdOrEmail OR %s = :userIdOrEmail", TABLE_USER, COLUMN_USERID, COLUMN_EMAIL);
        
        return jdbi.withHandle(handle -> 
            handle.createQuery(sql)
            .bind("userIdOrEmail", userIdOrEmail)
            .map((rs, ctx) -> Pair.of(mapToUser(rs), rs.getString(COLUMN_PASSWORD_HASH))).findOne());
    }
    
    @Override
    public UserData loadUser(String userId) {
        return findUserWithPasswordHash(userId).map(Pair::first).orElseThrow(() -> new KITSException("Can not find user with id: '" + userId + "'"));
    }
    
    private static UserData mapToUser(ResultSet rs) throws SQLException {
        
        return new UserData(
                rs.getString(COLUMN_USERID),
                rs.getString(COLUMN_NAME),
                Role.valueOf(rs.getString(COLUMN_ROLE)), 
                rs.getString(COLUMN_PHONE),
                rs.getString(COLUMN_EMAIL),
                Status.valueOf(rs.getString(COLUMN_STATUS)),
                rs.getInt(COLUMN_PLAYER_ID));
    }

    @Override
    public void changePassword(String userId, String newPasswordHash) {
        
        JdbiUtil.executeUpdate(jdbi, TABLE_USER, Map.of(COLUMN_PASSWORD_HASH, "*****"), Map.of(COLUMN_PASSWORD_HASH, newPasswordHash), COLUMN_USERID, userId);
    }

    @Override
    public void saveNewUser(UserData userData, String passwordHash) {
        
        Map<String, Object> map = createMap(userData);
        map.put(COLUMN_PASSWORD_HASH, passwordHash);
        jdbi.withHandle(handle -> JdbiUtil.createInsertStatement(handle, TABLE_USER, map).execute());
    }
    
    private static Map<String, Object> createMap(UserData user) {
        
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(COLUMN_USERID, user.userId());
        valuesMap.put(COLUMN_NAME, user.name());
        valuesMap.put(COLUMN_ROLE, user.role().name());
        valuesMap.put(COLUMN_PHONE, user.phone());
        valuesMap.put(COLUMN_EMAIL, user.email());
        valuesMap.put(COLUMN_STATUS, user.status().name());
        
        return valuesMap;
    }

    @Override
    public void updateUser(String userId, UserData updatedUserData) {
        
        Optional<Pair<UserData, String>> userWithPasswordHash = findUserWithPasswordHash(userId);
        if(userWithPasswordHash.isPresent()) {
            UserData originalUser = userWithPasswordHash.get().first();
            
            Map<String, Object> originalMap = createMap(originalUser);
            Map<String, Object> updatedMap = createMap(updatedUserData);
            
            JdbiUtil.executeUpdate(jdbi, TABLE_USER, originalMap, updatedMap, COLUMN_USERID, userId);
        } else {
            throw new KITSException("User '" + userId + "' not found");
        }
    }
    
    @Override
    public void activateUser(String userId) {
        setStatus(userId, Status.ACTIVE);
    }
    
    @Override
    public void inActivateUser(String userId) {
        setStatus(userId, Status.INACTIVE);
    }
    
    private void setStatus(String userId, Status status) {
        UserData user = loadUser(userId);
        JdbiUtil.executeUpdate(jdbi, TABLE_USER, Map.of(COLUMN_STATUS, user.status().name()), Map.of(COLUMN_STATUS, status.name()), COLUMN_USERID, userId);
    }

    @Override
    public void deleteUser(String userId) {
        
        jdbi.withHandle(handle -> handle.createUpdate(String.format("DELETE FROM %s WHERE %s = :userId", TABLE_USER, COLUMN_USERID, userId))
                .bind("userId", userId)
                .execute());
    }

}
