package hu.kits.tennis.infrastructure.web.api;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import hu.kits.tennis.common.StringUtil;
import hu.kits.tennis.domain.match.MatchInfo;
import hu.kits.tennis.domain.match.MatchService;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.PlayerRepository;
import hu.kits.tennis.domain.utr.PlayerStats;
import hu.kits.tennis.domain.utr.PlayerWithUTR;
import hu.kits.tennis.domain.utr.UTRService;
import hu.kits.tennis.infrastructure.ApplicationContext;
import io.javalin.http.ContentType;
import io.javalin.http.Context;

class RestHandlers {

    private final PlayerRepository playerRepository;
    private final UTRService utrService;
    private final MatchService matchService;
    
    RestHandlers(ApplicationContext applicationContext) {
        playerRepository = applicationContext.getPlayerRepository();
        utrService = applicationContext.getUTRService();
        matchService = applicationContext.getMatchService();
    }
    
    void listAllMatches(Context context) {
        List<MatchInfo> allMatches = matchService.loadAllMatches();
        context.json(allMatches);
    }
    
    void calculateUTRRanking(Context context) {
        List<PlayerWithUTR> utrRanking = utrService.calculateUTRRanking(true);
        context.json(utrRanking);
    }
    
    void playerStats(Context context) {
        int playerId = Integer.parseInt(context.pathParam("playerId"));
        Player player = playerRepository.findPlayer(playerId).get();
        PlayerStats playerStats = utrService.loadPlayerStats(player);
        context.json(playerStats);
    }
    
    void listAllPlayersWithUtrInCSV(Context context) {
        List<PlayerWithUTR> playersWithUTR = utrService.calculateUTRRanking(true);
        
        String content = playersWithUTR.stream()
                .sorted((p1, p2) -> StringUtil.HUN_COLLATOR.compare(p1.player().name(), p2.player().name()))
                .map(playerWithUtr -> createCsvRow(playerWithUtr))
                .collect(Collectors.joining("\n"));
        
        context.result(content);
        context.contentType(ContentType.TEXT_CSV);
    }
    
    private static String createCsvRow(PlayerWithUTR playerWithUtr) {
        return createCsvRow(
                playerWithUtr.player().name(),
                String.valueOf(playerWithUtr.player().id()),
                playerWithUtr.player().contact().phone(),
                playerWithUtr.player().contact().email(), 
                playerWithUtr.utr().toString().replace(".", ","));
    }
    
    private static String createCsvRow(String ... values) {
        return Arrays.stream(values).collect(Collectors.joining(";"));
    }
    
    void redirectToVaadin(Context context) {
        context.redirect("/ui/");
    }
    
}
