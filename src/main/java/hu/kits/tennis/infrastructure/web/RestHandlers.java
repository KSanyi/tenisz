package hu.kits.tennis.infrastructure.web;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.StringUtil;
import hu.kits.tennis.domain.utr.BookedMatch;
import hu.kits.tennis.domain.utr.PlayerWithUTR;
import hu.kits.tennis.domain.utr.UTRService;
import io.javalin.http.ContentType;
import io.javalin.http.Context;

class RestHandlers {

    private final UTRService utrService = Main.applicationContext.getUTRService();
    
    void listAllMatches(Context context) {
        List<BookedMatch> bookedMatches = utrService.loadBookedMatches();
        context.json(bookedMatches);
    }
    
    void listAllPlayersWithUtr(Context context) {
        List<PlayerWithUTR> playersWithUTR = utrService.calculateUTRRanking(true);
        List<PlayerWithUTR> playersWithUTRSortedByName = playersWithUTR.stream()
                .sorted((p1, p2) -> StringUtil.HUN_COLLATOR.compare(p1.player().name(), p2.player().name()))
                .toList();
        context.json(playersWithUTRSortedByName);
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
