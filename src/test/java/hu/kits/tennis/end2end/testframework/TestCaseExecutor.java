package hu.kits.tennis.end2end.testframework;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import javax.json.JsonStructure;
import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.common.Clock;
import hu.kits.tennis.common.IdGenerator;
import hu.kits.tennis.common.JsonUtil;
import hu.kits.tennis.common.UseCaseFileParser;
import hu.kits.tennis.common.UseCaseFileParser.TestCall;
import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.match.MatchResult;
import hu.kits.tennis.domain.match.MatchService;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.Player.Address;
import hu.kits.tennis.domain.player.Player.Contact;
import hu.kits.tennis.domain.player.PlayerRepository;
import hu.kits.tennis.domain.tournament.Organization;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentParams;
import hu.kits.tennis.domain.tournament.TournamentParams.Level;
import hu.kits.tennis.domain.tournament.TournamentParams.Structure;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.domain.utr.UTR;
import hu.kits.tennis.infrastructure.ApplicationContext;
import hu.kits.tennis.infrastructure.web.api.HttpServer;
import hu.kits.tennis.testutil.FakeInvoiceService;
import hu.kits.tennis.testutil.InMemoryDataSourceFactory;
import hu.kits.tennis.testutil.SpyEmailSender;
import hu.kits.tennis.testutil.TestUtil;

public class TestCaseExecutor {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private int port;
    private HttpServer httpServer;
    
    @BeforeEach
    private void init() throws Exception {
        DataSource dataSource = InMemoryDataSourceFactory.createDataSource();
        
        ApplicationContext applicationContext = new ApplicationContext(dataSource, new SpyEmailSender(), null, new FakeInvoiceService());
        
        setup(applicationContext);
        
        IdGenerator.useFakeGenerator();
        
        port = TestUtil.findFreePort();
        httpServer = new HttpServer(port, applicationContext);
        httpServer.start();
    }
    
    private static void setup(ApplicationContext applicationContext) {
        PlayerRepository playerRepository = applicationContext.getPlayerRepository();
        Player player1 = playerRepository.saveNewPlayer(new Player(null, "Nagy Róbert", new Contact("nagy.robert@gmail.com", "", Address.EMPTY, ""), UTR.of(8.), Set.of(Organization.KVTK)));
        Player player2 = playerRepository.saveNewPlayer(new Player(null, "Kiss István", new Contact("istvan.kiss@gmail.com", "", Address.EMPTY, ""), UTR.of(7.), Set.of(Organization.KVTK)));
        Player player3 = playerRepository.saveNewPlayer(new Player(null, "Tóth Gábor", new Contact("toth.gabor@gmail.com", "", Address.EMPTY, ""), UTR.of(7.5), Set.of(Organization.KVTK)));
        
        TournamentService tournamentService = applicationContext.getTournamentService();
        Tournament tournament = tournamentService.createTournament(new TournamentParams(Organization.KVTK, TournamentParams.Type.DAILY, Level.L500, Level.L500, LocalDate.of(2023,3,15), "Napi 500 verseny", "Mini Garros", Structure.SIMPLE_BOARD, 1));
        
        MatchService matchService = applicationContext.getMatchService();
        matchService.saveMatch(new Match(null, tournament.id(), 1, 1, LocalDate.of(2023,3,15), player1, player2, new MatchResult(6, 0)));
        matchService.saveMatch(new Match(null, tournament.id(), 1, 1, LocalDate.of(2023,3,15), player1, player3, new MatchResult(6, 2)));
        matchService.saveMatch(new Match(null, tournament.id(), 1, 1, LocalDate.of(2023,3,15), player2, player3, new MatchResult(7, 5)));
        
        applicationContext.getUTRService().recalculateAllUTRs();
        
        Clock.setStaticTime(LocalDateTime.of(2023,4,1, 10,0));
    }

    @AfterEach
    private void stop() {
       // httpServer.stop();
    }
    
    @ParameterizedTest
    @TestCaseDirSource("test/test-cases")
    void executeTestCase(File testCaseFile) throws IOException, URISyntaxException, InterruptedException {
        
        logger.info("Executing test case: {}", testCaseFile.getName());
        
        List<TestCall> testCalls = UseCaseFileParser.parseUseCaseFile(testCaseFile, false);
        
        for(TestCall testCall : testCalls) {
            
            logger.info("-------------------------- {} --------------------------", testCall.name());
            
            if(testCall.staticTime() != null) {
                Clock.setStaticTime(testCall.staticTime());
            }
            
            String url = testCall.urlTemplate().replaceAll("<url-base>", "http://localhost:" + port);
            
            HttpClient client = HttpClient.newBuilder()
                    .version(Version.HTTP_1_1)
                    .build();
            
            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder()
                    .uri(new URI(url));
            
            HttpRequest httpRequest;
            logger.info("Calling {} {}", testCall.httpMethod(), url);
            switch (testCall.httpMethod()) {
                case GET: 
                    httpRequest = httpRequestBuilder.GET().build();
                    break;
                case POST: 
                    httpRequest = httpRequestBuilder.POST(HttpRequest.BodyPublishers.ofString(testCall.requestJson())).build();
                    break;
                //case DELETE:
                //    httpResponse = Unirest.delete(url).asString();
                //    break;
                case PUT:
                default: throw new IllegalArgumentException("HTTP method not supported: " + testCall.httpMethod());
            }
            
            HttpResponse<String> httpResponse = client.send(httpRequest, BodyHandlers.ofString());
            
            logger.info("Response status: {}", httpResponse.statusCode());
            
            assertEquals(testCall.responseStatus(), httpResponse.statusCode());
            
            if(!testCall.responseJson().isBlank()) {
                assertEquals(normalize(testCall.responseJson()), normalize(httpResponse.body()));
            }
            
            logger.info("Response: {}", httpResponse.body());
        }
    }
    
    private static String normalize(String responseJson) {
        String commentsRemoved = responseJson.replaceAll("//.*\n", "\n");
        JsonStructure json = JsonUtil.readJson(commentsRemoved);
        
        return JsonUtil.printJson(json);
    }

}
