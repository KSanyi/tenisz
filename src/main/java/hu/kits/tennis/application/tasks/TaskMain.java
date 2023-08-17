package hu.kits.tennis.application.tasks;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.cj.jdbc.MysqlDataSource;

import hu.kits.tennis.common.Clock;
import hu.kits.tennis.common.Environment;
import hu.kits.tennis.domain.invoice.InvoiceService;
import hu.kits.tennis.domain.ktr.KTRDetails;
import hu.kits.tennis.domain.ktr.KTRService;
import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.PlayerRepository;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentParams.Status;
import hu.kits.tennis.domain.tournament.TournamentParams.Type;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.domain.tournament.TournamentSummary;
import hu.kits.tennis.infrastructure.ApplicationContext;
import hu.kits.tennis.infrastructure.invoice.BillingoInvoiceService;

@SuppressWarnings("unused")
public class TaskMain {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    public static ApplicationContext applicationContext;
    
    public static void main(String[] args) throws Exception {
        
        URI dbUri = getDatabaseUri();
        dbUri = new URI("mysql://bace8362c32290:cf1b3d55@eu-cdbr-west-02.cleardb.net/heroku_5a25f1ea8b513bf?useUnicode=yes&characterEncoding=UTF-8&reconnect=true");
        
        DataSource dataSource = createDataSource(dbUri);
        
        InvoiceService invoiceService = createInvoiceService(Environment.LIVE);
        
        applicationContext = new ApplicationContext(dataSource, null, null, invoiceService);
        
        //new KVTKMeccsImporter(applicationContext).importContactData();
        //new KVTKMeccsImporter(applicationContext).importPlayers();
        //new KVTKMeccsImporter(applicationContext).importMatches();
        //new KVTKMeccsImporter(applicationContext).setupTournaments();
        
        //new TeniszPartnerMeccsImporter(applicationContext).importMatches();
        //new TeniszPartnerMeccsImporter(applicationContext).createTournaments();
        //new TeniszPartnerMeccsImporter(applicationContext).cleanupDuplicates();
        
        //new TeniszPartnerMeccsImporter(applicationContext).importPlayers();
        //new TeniszPartnerMeccsImporter(applicationContext).importTournaments();
        
        //setTournamentWinners();
        
        //new KTRChangeAnalyzer(resourceFactory).analyse();
        
        new BillingoClient(applicationContext).createInvoice();
    }
    
    private static void setTournamentWinners() {
        TournamentService tournamentService = applicationContext.getTournamentService();
        for(TournamentSummary summary : tournamentService.loadDailyTournamentSummariesList()) {
            if(summary.status() == Status.COMPLETED && summary.winner() == null) {
                Player winner;
                Tournament tournament = tournamentService.findTournament(summary.id()).get();
                if(summary.type() == Type.TOUR) {
                    Match lastMatch = tournament.matches().stream().filter(Match::isPlayed).max(Comparator.comparing(Match::date)).get();
                    winner = lastMatch.winner();
                } else {
                    winner = findPlayerWhoWonAllHisMatches(tournament.matches());
                }
                if(winner != null) {
                    tournamentService.setWinner(tournament.id(), winner);
                    System.out.println(summary.name() + " winner: " + winner);
                }
            }
        }
    }

    private static Player findPlayerWhoWonAllHisMatches(List<Match> matches) {
        Set<Player> players = matches.stream().flatMap(Match::players).collect(Collectors.toSet());
        for(var match : matches) {
            players.remove(match.loser());
        }
        return players.isEmpty() ? null : players.iterator().next();
    }

    private static void runKTRFluctationsTask() {
        KTRService ktrService = applicationContext.getKTRService();
        LocalDateTime time = LocalDateTime.now();
        List<Double> values = new ArrayList<>();
        for(int i=0;i<152;i++) {
            Clock.setStaticTime(time.plusWeeks(i));
            LocalDate date = Clock.today();
            KTRDetails ktrDetails = ktrService.calculatePlayersKTR(new Player(20112, null, null, null, null));
            System.out.println(date + " - " + ktrDetails.ktr());
            values.add(ktrDetails.ktr().value());
        }
        
        double min = values.stream().mapToDouble(v -> v).min().orElse(0);
        double max = values.stream().mapToDouble(v -> v).max().orElse(0);
        System.out.println(min + " - " + max);
    }
    
    private static InvoiceService createInvoiceService(Environment environment) throws URISyntaxException {
        String apiKey = loadMandatoryEnvVariable("BILLINGO_API_KEY");
        
        return new BillingoInvoiceService(environment, apiKey);
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
