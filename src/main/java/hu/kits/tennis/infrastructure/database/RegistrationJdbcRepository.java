package hu.kits.tennis.infrastructure.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.jdbi.v3.core.Jdbi;

import hu.kits.tennis.domain.player.registration.Registration;
import hu.kits.tennis.domain.player.registration.Registration.RegistrationData;
import hu.kits.tennis.domain.player.registration.Registration.RegistrationStatus;
import hu.kits.tennis.domain.player.registration.RegistrationRepository;

public class RegistrationJdbcRepository implements RegistrationRepository {

    private static final String TABLE_REGISTRATION = "REGISTRATION";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_NAME = "NAME";
    private static final String COLUMN_EMAIL = "EMAIL";
    private static final String COLUMN_PHONE = "PHONE";
    private static final String COLUMN_ZIP = "ZIP";
    private static final String COLUMN_TOWN = "TOWN";
    private static final String COLUMN_STREET_ADDRESS = "STREET_ADDRESS";
    private static final String COLUMN_EXPERIENCE = "EXPERIENCE";
    private static final String COLUMN_PLAY_FREQUENCY = "PLAY_FREQUENCY";
    private static final String COLUMN_VENUE = "VENUE";
    private static final String COLUMN_HAS_PLAYED_TOURNAMENT = "HAS_PLAYED_TOURNAMENT";
    private static final String COLUMN_STATUS = "STATUS";
    private static final String COLUMN_TIMESTAMP = "TIMESTAMP";
    
    private final Jdbi jdbi;
    
    public RegistrationJdbcRepository(DataSource dataSource) {
        jdbi = Jdbi.create(dataSource);
    }
    
    @Override
    public void saveNewRegistration(Registration registration) {
        Map<String, Object> map = createMap(registration);
        jdbi.withHandle(handle -> JdbiUtil.createInsertStatement(handle, TABLE_REGISTRATION, map).execute());
    }
    
    private static Map<String, Object> createMap(Registration registration) {
        
        Registration.RegistrationData data = registration.data();
        
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(COLUMN_NAME, data.name());
        valuesMap.put(COLUMN_EMAIL, data.email());
        valuesMap.put(COLUMN_PHONE, data.phone());
        valuesMap.put(COLUMN_ZIP, data.zip());
        valuesMap.put(COLUMN_TOWN, data.town());
        valuesMap.put(COLUMN_STREET_ADDRESS, data.streetAddress());
        valuesMap.put(COLUMN_EXPERIENCE, data.experience());
        valuesMap.put(COLUMN_PLAY_FREQUENCY, data.playFrequency());
        valuesMap.put(COLUMN_VENUE, data.venue());
        valuesMap.put(COLUMN_HAS_PLAYED_TOURNAMENT, data.hasPlayedInTournament());
        valuesMap.put(COLUMN_STATUS, registration.status());
        valuesMap.put(COLUMN_TIMESTAMP, registration.timestamp());
        
        return valuesMap;
    }

    @Override
    public List<Registration> loadAllRegistrations() {
        String sql = String.format("SELECT * FROM %s", TABLE_REGISTRATION);
        return jdbi.withHandle(handle -> 
            handle.createQuery(sql)
            .map((rs, ctx) -> mapToRegistration(rs)).list());
    }
    
    private static Registration mapToRegistration(ResultSet rs) throws SQLException {
        return new Registration(rs.getInt(COLUMN_ID),
                new RegistrationData(
                        rs.getString(COLUMN_NAME),
                        rs.getString(COLUMN_PHONE),
                        rs.getString(COLUMN_EMAIL),
                        rs.getInt(COLUMN_ZIP),
                        rs.getString(COLUMN_TOWN),
                        rs.getString(COLUMN_STREET_ADDRESS),
                        rs.getString(COLUMN_EXPERIENCE),
                        rs.getString(COLUMN_PLAY_FREQUENCY),
                        rs.getString(COLUMN_VENUE),
                        rs.getString(COLUMN_HAS_PLAYED_TOURNAMENT)),
                RegistrationStatus.valueOf(rs.getString(COLUMN_STATUS)),
                rs.getTimestamp(COLUMN_TIMESTAMP).toLocalDateTime());
    }

    @Override
    public void setRegistrationStatus(int id, RegistrationStatus accepted) {
        JdbiUtil.executeSimpleUpdate(jdbi, TABLE_REGISTRATION, COLUMN_STATUS, accepted, COLUMN_ID, id);
    }

}
