package hu.kits.tennis;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.cj.jdbc.MysqlDataSource;

import hu.kits.tennis.application.MATKMeccsImporter;
import hu.kits.tennis.application.ResourceFactory;
import hu.kits.tennis.common.Environment;
import hu.kits.tennis.domain.email.EmailSender;
import hu.kits.tennis.infrastructure.email.SendGridEmailSender;
import hu.kits.tennis.infrastructure.web.HttpServer;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    public static ResourceFactory resourceFactory;
    
    public static void main(String[] args) throws Exception {
        
        logger.info("Starting application");
        
        Environment environment = getEnvironment();
        
        int port = getPort();
        URI dbUri = getDatabaseUri();
        
        DataSource dataSource = createDataSource(dbUri);
        EmailSender emailSender = createEmailSender(environment);
        
        resourceFactory = new ResourceFactory(dataSource, emailSender);
        
        //new MATKMeccsImporter(resourceFactory).importMatches();
        //new MATKMeccsImporter(resourceFactory).createTournaments();
        
        //new TeniszPartnerMeccsImporter(resourceFactory).importMatches();
        //new TeniszPartnerMeccsImporter(resourceFactory).createTournaments();
        //new TeniszPartnerMeccsImporter(resourceFactory).cleanupDuplicates();
        
        //new TeniszPartnerMeccsImporter(resourceFactory).importPlayers();
        //new TeniszPartnerMeccsImporter(resourceFactory).importTournaments();
        
        new HttpServer(port).start();
    }
    
    private static Environment getEnvironment() {
        
        String environmentString = loadMandatoryEnvVariable("ENVIRONMENT");
        
        Environment environment;
        try {
            environment = Environment.valueOf(environmentString);
        } catch(Exception ex) {
            throw new IllegalArgumentException("System environment variable ENVIRONMENT is wrong: " + environmentString);
        }
        logger.info("ENVIRONMENT: " + environment);
        return environment;
    }
    
    private static int getPort() {
        String port = loadMandatoryEnvVariable("PORT");

        try {
            int portNumber = Integer.parseInt(port);
            logger.info("PORT: " + port);
            return portNumber;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Illegal system environment variable PORT: " + port);
        }
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
        return dataSource;
    }
    
    private static EmailSender createEmailSender(Environment environment) throws URISyntaxException {
        String sendGridPassword = System.getenv("SENDGRID_PASSWORD");
        
        return new SendGridEmailSender(environment, sendGridPassword);
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
