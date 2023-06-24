package hu.kits.tennis.application.usecase;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import hu.kits.tennis.domain.match.MatchInfo;
import hu.kits.tennis.domain.match.MatchService;
import hu.kits.tennis.domain.player.PlayersService;
import hu.kits.tennis.domain.utr.PlayersWithUTR;
import hu.kits.tennis.domain.utr.UTR;

public class AllMatchesUseCase {

    private final PlayersService playersService;
    private final MatchService matchService;

    public AllMatchesUseCase(PlayersService playersService, MatchService matchService) {
        this.playersService = playersService;
        this.matchService = matchService;
    }
    
    public List<MatchInfo> loadAllMatches() {
        
        PlayersWithUTR playersWithUTR = playersService.loadAllPlayersWithUTR();
        List<MatchInfo> allMatches = matchService.loadAllMatches();
        
        Stream<MatchInfo> playedMatches = allMatches.stream()
            .filter(m -> m.result() != null);
        
        Stream<MatchInfo> upcomingMatches = allMatches.stream()
            .filter(m -> m.result() == null)
            .map(m -> updateWithCurrentUTR(m, playersWithUTR));
        
        return Stream.concat(upcomingMatches, playedMatches)
                .sorted(Comparator.comparing(MatchInfo::dateForCompare).thenComparing(MatchInfo::id).reversed())
                .toList();
    }

    private static MatchInfo updateWithCurrentUTR(MatchInfo matchInfo, PlayersWithUTR playersWithUTR) {
        
        return new MatchInfo(matchInfo.id(),
                matchInfo.tournamentInfo(),
                matchInfo.date(),
                matchInfo.player1(),
                playersWithUTR.getUTR(matchInfo.player1().id()),
                matchInfo.player2(),
                playersWithUTR.getUTR(matchInfo.player2().id()),
                null, UTR.UNDEFINED, UTR.UNDEFINED, false);
    }
    
}
