package hu.kits.tennis.infrastructure.web;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.StringUtil;
import hu.kits.tennis.domain.utr.BookedMatch;
import hu.kits.tennis.domain.utr.PlayerWithUTR;
import hu.kits.tennis.domain.utr.UTRService;
import io.javalin.http.ContentType;
import io.javalin.http.Context;

class RestHandlers {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final UTRService utrService = Main.applicationContext.getUTRService();
    
    void listAllMatches(Context context) {
        List<BookedMatch> bookedMatches = utrService.loadBookedMatches();
        context.json(bookedMatches);
    }
    
    void listAllPlayersWithUtr(Context context) {
        List<PlayerWithUTR> playersWithUTR = utrService.calculateUTRRanking();
        List<PlayerWithUTR> playersWithUTRSortedByName = playersWithUTR.stream()
                .sorted((p1, p2) -> StringUtil.HUN_COLLATOR.compare(p1.player().name(), p2.player().name()))
                .toList();
        context.json(playersWithUTRSortedByName);
    }
    
    void listAllPlayersWithUtrInCSV(Context context) {
        List<PlayerWithUTR> playersWithUTR = utrService.calculateUTRRanking();
        
        String content = playersWithUTR.stream()
                .sorted((p1, p2) -> StringUtil.HUN_COLLATOR.compare(p1.player().name(), p2.player().name()))
                .map(playerWithUtr -> createCsvRow(playerWithUtr))
                .collect(Collectors.joining("\n"));
        
        context.result(content);
        context.contentType(ContentType.TEXT_CSV);
        
        if(content.length() < 50) {
            logger.info("UTR CSV: {}", content);    
        } else {
            logger.info("UTR CSV: {}", content.substring(0, 50) + " ... " + content.substring(content.length() - 10, content.length()));
        }
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
