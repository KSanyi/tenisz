package hu.kits.tennis.infrastructure.web;

import java.util.List;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.StringUtil;
import hu.kits.tennis.domain.utr.BookedMatch;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.domain.utr.PlayerRepository;
import hu.kits.tennis.domain.utr.PlayerWithUTR;
import hu.kits.tennis.domain.utr.UTRService;
import hu.kits.tennis.infrastructure.web.Requests.PlayerCreationRequest;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;

class RestHandlers {

    private final PlayerRepository playerRepository = Main.resourceFactory.getPlayerRepository();
    private final UTRService utrService = Main.resourceFactory.getUTRService();
    
    void createPlayer(Context context) {
        PlayerCreationRequest playerCreationRequest = RequestParser.parseUserCreationRequest(context.body());
        Player newPlayer = Player.createNew(playerCreationRequest.name());
        Player savedPlayer = playerRepository.saveNewPlayer(newPlayer);
        context.json(savedPlayer);
        context.status(HttpCode.CREATED);
    }
    
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
    
    void redirectToVaadin(Context context) {
        context.redirect("/ui/");
    }
}
