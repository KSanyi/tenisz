package hu.kits.tennis.infrastructure.web.api;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.infrastructure.ApplicationContext;
import hu.kits.tennis.infrastructure.web.VaadinJettyServer;
import io.javalin.Javalin;
import io.javalin.core.util.RouteOverviewPlugin;
import io.javalin.core.validation.JavalinValidation;
import io.javalin.http.Context;

public class HttpServer {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final Javalin javalin;
    private final int port;
    
    public HttpServer(int port, ApplicationContext applicationContext) {
        
        this.port = port;
        
        RestHandlers restHandlers = new RestHandlers(applicationContext);
        ApiDocHandler apiDocHandler = new ApiDocHandler();
        
        javalin = Javalin.create(config -> {
            config.server(() -> new VaadinJettyServer(port));
            config.registerPlugin(new RouteOverviewPlugin("/routes")); 
            config.defaultContentType = "application/json";
            config.enableCorsForAllOrigins();
            config.requestLogger(this::log);
            config.jsonMapper(new TeniszJsonMapper(applicationContext));
        }).routes(() -> {
            path("api/docs", () -> {
                get(apiDocHandler::createTestCasesList);
                get("{testCase}", apiDocHandler::createTestCaseDoc);
            });
            path("api/matches", () -> {
                get(restHandlers::listAllMatches);
            });
            path("api/tournaments/DAILY", () -> {
                get(restHandlers::listAllDailyTournaments);
            });
            path("api/tournaments/TOUR", () -> {
                get(restHandlers::listAllTourTournaments);
            });
            path("api/tournaments/{tournamentId}", () -> {
                get(restHandlers::loadTournamentDetails);
            });
            path("api/ktr-ranking", () -> {
                get(restHandlers::calculateKTRRanking);
            });
            path("api/ktr-csv", () -> {
                get(restHandlers::listAllPlayersWithKTRInCSV);
            });
            path("api/matches-csv", () -> {
                get(restHandlers::listAllMatchesInCSV);
            });
            path("api/player-stats/{playerId}", () -> {
                get(restHandlers::playerStats);
            });
            path("", () -> {
                get(restHandlers::redirectToVaadin);
            });
            path("tournaments", () -> {
                get(restHandlers::redirectToVaadin);
            });
        }).exception(BadRequestException.class, this::handleException);
        
        JavalinValidation.register(LocalDate.class, LocalDate::parse);
        
        logger.info("Server initialized on port {}", port);
    }
    
    public void start() throws Exception {
        javalin.start(port);
        logger.info("Tenisz server started");
    }
    
    public void stop() throws Exception {
        javalin.stop();
        logger.info("Tenisz server stopped");
    }
    
    private void log(Context ctx, Float executionTimeMs) {
        String body = ctx.body().isBlank() ? "" : "body: " + ctx.body().replaceAll("\n", "").replaceAll("\\s+", " ");
        //logger.trace("{} {} {} Status: {} from {} ({}) headers: {} agent: {}", ctx.method(), ctx.path(), body, ctx.status(), ctx.ip(), ctx.host(), ctx.headerMap(), ctx.userAgent());
        logger.info("{} {} {} Status: {} in {} millis", ctx.method(), ctx.path(), body, ctx.status(), executionTimeMs);
    }
    
    public static class BadRequestException extends RuntimeException {
        
        public BadRequestException(String message) {
            super(message);
        }
        
    }
    
    private void handleException(BadRequestException ex, Context context) {
        handleException(context, 400, ex.getMessage());
    }
    
    private static void handleException(Context context, int status, String message) {
        context.status(status);
        if(message != null) {
            context.result(message);            
        }
        logger.error("Status: {}, message: {}", context.status(), message);
    }
    
}