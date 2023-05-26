package hu.kits.tennis;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.scribejava.apis.GoogleApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.mysql.cj.jdbc.MysqlDataSource;

import hu.kits.tennis.common.Environment;
import hu.kits.tennis.domain.email.EmailSender;
import hu.kits.tennis.domain.invoice.InvoiceService;
import hu.kits.tennis.infrastructure.ApplicationContext;
import hu.kits.tennis.infrastructure.email.SendGridEmailSender;
import hu.kits.tennis.infrastructure.invoice.BillingoInvoiceService;
import hu.kits.tennis.infrastructure.web.HttpServer;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    // Vaadin UI classes access applicationContext via this static reference as I didn't find a nice way of passing it 
    public static ApplicationContext applicationContext;
    
    public static void main(String[] args) throws Exception {
        
        logger.info("Starting application");
        
        Environment environment = getEnvironment();
        
        int port = getPort();
        URI dbUri = getDatabaseUri();
        
        DataSource dataSource = createDataSource(dbUri);
        
        EmailSender emailSender = createEmailSender(environment);
        
        OAuth20Service oAuthService = createOAuthService();
        
        InvoiceService invoiceService = createInvoiceService(environment);
        
        applicationContext = new ApplicationContext(dataSource, emailSender, oAuthService, invoiceService);
        
        new HttpServer(port, applicationContext).start();
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
        logger.info("Database connection: " + jdbcUrl);
        return dataSource;
    }
    
    private static EmailSender createEmailSender(Environment environment) throws URISyntaxException {
        String sendGridPassword = System.getenv("SENDGRID_PASSWORD");
        
        return new SendGridEmailSender(environment, sendGridPassword);
    }
    
    private static InvoiceService createInvoiceService(Environment environment) throws URISyntaxException {
        String apiKey = loadMandatoryEnvVariable("BILLINGO_API_KEY");
        
        return new BillingoInvoiceService(environment, apiKey);
    }
    
    private static String loadMandatoryEnvVariable(String name) {
        String variable = System.getenv(name);
        if (variable == null) {
            throw new IllegalArgumentException("System environment variable " + name + " is missing");
        } else {
            return variable;
        }
    }
    
    private static OAuth20Service createOAuthService() {
        
        String googleClientId = loadMandatoryEnvVariable("GOOGLE_CLIENT_ID");
        String googleClientSecret = loadMandatoryEnvVariable("GOOGLE_CLIENT_SECRET");
        String callbackUrl = loadMandatoryEnvVariable("OAUTH_CALLBACK_URL");
        
        return new ServiceBuilder(googleClientId)
                .apiSecret(googleClientSecret)
                .defaultScope("profile email") // replace with desired scope
                .callback(callbackUrl)
                .build(GoogleApi20.instance());
    }

}
