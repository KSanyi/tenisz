package hu.kits.tennis.application;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.cj.jdbc.MysqlDataSource;

import hu.kits.tennis.common.Clock;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.domain.utr.UTRDetails;
import hu.kits.tennis.domain.utr.UTRService;

public class TaskMain {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    public static ResourceFactory resourceFactory;
    
    public static void main(String[] args) throws Exception {
        
        URI dbUri = getDatabaseUri();
        dbUri = new URI("mysql://bace8362c32290:cf1b3d55@eu-cdbr-west-02.cleardb.net/heroku_5a25f1ea8b513bf?useUnicode=yes&characterEncoding=UTF-8&reconnect=true");
        
        DataSource dataSource = createDataSource(dbUri);
        
        resourceFactory = new ResourceFactory(dataSource, null);
        
        //new KVTKMeccsImporter(resourceFactory).importContactData();
        //new KVTKMeccsImporter(resourceFactory).importPlayers();
        new KVTKMeccsImporter(resourceFactory).importMatches();
        new KVTKMeccsImporter(resourceFactory).setupTournaments();
        
        //new TeniszPartnerMeccsImporter(resourceFactory).importMatches();
        //new TeniszPartnerMeccsImporter(resourceFactory).createTournaments();
        //new TeniszPartnerMeccsImporter(resourceFactory).cleanupDuplicates();
        
        //new TeniszPartnerMeccsImporter(resourceFactory).importPlayers();
        //new TeniszPartnerMeccsImporter(resourceFactory).importTournaments();
        
        runUTRFluctationsTask();
    }
    
    private static void runUTRFluctationsTask() {
        UTRService utrService = resourceFactory.getUTRService();
        LocalDateTime time = LocalDateTime.now();
        List<Double> values = new ArrayList<>();
        for(int i=0;i<152;i++) {
            Clock.setStaticTime(time.plusWeeks(i));
            LocalDate date = Clock.today();
            UTRDetails utrDetails = utrService.calculatePlayersUTR(new Player(20112, null, null, null, null));
            System.out.println(date + " - " + utrDetails.utr());
            values.add(utrDetails.utr().value());
        }
        
        double min = values.stream().mapToDouble(v -> v).min().orElse(0);
        double max = values.stream().mapToDouble(v -> v).max().orElse(0);
        System.out.println(min + " - " + max);
    }
    
    private static URI getDatabaseUri() throws URISyntaxException {
        String databaseUrl = loadMandatoryEnvVariable("CLEARDB_DATABASE_URL");
        return new URI(databaseUrl);
    }
    
    private static DataSource createDataSource(URI dbUri) throws URISyntaxException {
        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String jdbcUrl = "jdbc:mysql://" + dbUri.getHost() + dbUri.getPath() + "?" + dbUri.getQuery(); 
        
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(jdbcUrl);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        logger.info("Database connection: " + jdbcUrl);
        return dataSource;
    }
    
    private static String loadMandatoryEnvVariable(String name) {
        String variable = System.getenv(name);
        if (variable == null) {
            throw new IllegalArgumentException("System environment variable " + name + " is missing");
        } else {
            return variable;
        }
    }

}
