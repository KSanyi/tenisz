package hu.kits.tennis.application.usecase;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import hu.kits.tennis.domain.ktr.KTR;
import hu.kits.tennis.domain.ktr.PlayersWithKTR;
import hu.kits.tennis.domain.match.MatchInfo;
import hu.kits.tennis.domain.match.MatchService;
import hu.kits.tennis.domain.player.PlayersService;

public class AllMatchesUseCase {

    private final PlayersService playersService;
    private final MatchService matchService;

    public AllMatchesUseCase(PlayersService playersService, MatchService matchService) {
        this.playersService = playersService;
        this.matchService = matchService;
    }
    
    public List<MatchInfo> loadAllMatches() {
        
        PlayersWithKTR playersWithKTR = playersService.loadAllPlayersWithKTR();
        List<MatchInfo> allMatches = matchService.loadAllMatches();
        
        Stream<MatchInfo> playedMatches = allMatches.stream()
            .filter(m -> m.result() != null);
        
        Stream<MatchInfo> upcomingMatches = allMatches.stream()
            .filter(m -> m.result() == null)
            .map(m -> updateWithCurrentKTR(m, playersWithKTR));
        
        return Stream.concat(upcomingMatches, playedMatches)
                .sorted(Comparator.comparing(MatchInfo::dateForCompare).thenComparing(MatchInfo::id).reversed())
                .toList();
    }

    private static MatchInfo updateWithCurrentKTR(MatchInfo matchInfo, PlayersWithKTR playersWithKTR) {
        
        return new MatchInfo(matchInfo.id(),
                matchInfo.tournamentInfo(),
                matchInfo.date(),
                matchInfo.player1(),
                playersWithKTR.getKTR(matchInfo.player1().id()),
                matchInfo.player2(),
                playersWithKTR.getKTR(matchInfo.player2().id()),
                null, KTR.UNDEFINED, KTR.UNDEFINED, false);
    }
    
}
