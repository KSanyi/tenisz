package hu.kits.tennis.infrastructure.database;

import static java.util.stream.Collectors.joining;

import java.lang.invoke.MethodHandles;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.common.CollectionsUtil;

public class JdbiUtil {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    public static Update createInsertStatement(Handle handle, String tableName, Map<String, ?> values) {
        
        Map<String, Object> extendedValues = new HashMap<>(values);
        
        List<String> keys = new ArrayList<>(extendedValues.keySet());
        
        String sql = "INSERT INTO " + tableName + "(" + String.join(", ", keys) + ") VALUES (" + createQuestionMarks(keys.size()) + ")";
        
        Update update = handle.createUpdate(sql);
        for(String key : keys) {
            update = update.bind(keys.indexOf(key), extendedValues.get(key));
        }
        
        return update;
    }
    
    public static PreparedBatch createBatchInsertStatement(Handle handle, String tableName, Set<String> columns, List<Map<String, Object>> valuesList) {
        
        List<String> keys = new ArrayList<>(columns);
        
        PreparedBatch batch = handle.prepareBatch("INSERT INTO " + tableName + "(" + String.join(", ", keys) + ") VALUES (" + createQuestionMarks(keys.size()) + ")");
        
        for (Map<String, Object> values: valuesList) {
            for(String key : keys) {
                batch.bind(keys.indexOf(key), values.get(key));  
            }
            batch.add();
        }
        
        return batch;
    }
    
    public static void executeUpdate(Jdbi jdbi, String tableName, Map<String, Object> originalMap, Map<String, Object> updatedMap, String keyColumn, String keyColumnValue) {
        executeUpdate(jdbi,tableName, originalMap, updatedMap, Map.of(keyColumn, keyColumnValue));
    }
    
    public static void executeUpdate(Jdbi jdbi, String tableName, Map<String, Object> originalMap, Map<String, Object> updatedMap, Map<String, String> keysWithValues) {
        
        Map<String, Object> paramMap = new HashMap<>();
        List<String> updatesLog = new ArrayList<>();
        
        Set<String> keys = CollectionsUtil.union(originalMap.keySet(), updatedMap.keySet());
        for(String columnName : keys) {
            Object originalValue = originalMap.get(columnName);
            Object updatedValue = updatedMap.get(columnName);
            if(!compare(originalValue, updatedValue)) {
                String change = String.format("%s: %s -> %s ", columnName, originalValue, updatedValue);
                updatesLog.add(change);
                paramMap.put(columnName, updatedValue);
            }
        }
        
        if(updatesLog.isEmpty()) {
            log.debug("No changes");
        } else {
            String updateSql = String.format("UPDATE %s SET %s WHERE %s", tableName, 
                    paramMap.keySet().stream().map(column -> column + " = :" + column).collect(joining(", ")), 
                    keysWithValues.keySet().stream().map(key -> key + " = :" + "V_" + key).collect(joining(" AND ")));
            keysWithValues.forEach((key, value) -> paramMap.put("V_" + key, value));
            int updatedRows = jdbi.withHandle(handle -> handle.createUpdate(updateSql).bindMap(paramMap).execute());
            if(updatedRows == 1) {
                log.info("{} {} updated: {}", tableName, keysWithValues.values(), updatesLog.stream().collect(joining(", ")));
            } else {
                log.error("Updated rows: {}", updatedRows);
            }
        }
    }
    
    public static void executeSimpleUpdate(Jdbi jdbi, String tableName, String column, Object value, String keyColumn, Object keyColumnValue) {
        String updateSql = String.format("UPDATE %s SET %s = :value WHERE %s = :keyColumnValue", tableName, column, keyColumn);
        jdbi.withHandle(handle -> handle.createUpdate(updateSql)
                .bind("value", value)
                .bind("keyColumnValue", keyColumnValue)
                .execute());
        log.debug("{} field {} updated to {} in row with {} = {}", tableName, column, value, keyColumn, keyColumnValue);
    }
    
    private static boolean compare(Object a, Object b) {
        if(a == null && b == null) return true;
        if(a != null && b == null || a == null && b != null) return false;
        return a.equals(b);
    }
    
    private static String createQuestionMarks(int n) {
        return IntStream.rangeClosed(1, n).mapToObj(i -> "?").collect(Collectors.joining(", "));
    }
    
    public static Optional<Integer> mapToOptionalInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? Optional.empty() : Optional.of(value);
    }
    
}
