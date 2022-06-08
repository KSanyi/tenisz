package hu.kits.tennis.testutil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;

public class InMemoryDataSourceFactory {

    public static DataSource createDataSource(String ... insertSqls) throws Exception {
        Class.forName("org.h2.Driver");
        JdbcDataSource dataSource = new JdbcDataSource();
        Path tempDirPath = Files.createTempDirectory("opfr");
        dataSource.setURL("jdbc:h2:" + tempDirPath + "/test");
        try (Connection connection = dataSource.getConnection()) {
            dropExistingDb(connection);
            createDb(connection);
            Stream.of(insertSqls).forEach(sql -> execute(connection, sql));
        }
        
        return dataSource;
    }
    
    private static void dropExistingDb(Connection connection) {
        try {
            for(String line : Files.readAllLines(Paths.get("database/drop-database.sql"))) {
                connection.createStatement().executeUpdate(line);
            }
        } catch (Exception ex) {} // TABLE does not exist, no problem
    }
    
    private static void createDb(Connection connection) throws Exception {
        boolean execute = true;
        for (String line : Files.readAllLines(Paths.get("database/create-database.sql"))) {
            if(line.contains("ONLY MYSQL START")) {
                execute = false;
            } else if(line.contains("ONLY MYSQL END")) {
                execute = true;
            } else {
                if(execute) connection.createStatement().executeUpdate(line);
            }
        }
    }
    
    private static void execute(Connection connection, String sqlInsert) {
        try {
            connection.createStatement().executeUpdate(sqlInsert);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
}
