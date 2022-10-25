package hu.kits.tennis.domain.utr;

import java.time.LocalDate;

import hu.kits.tennis.common.StringUtil;
import hu.kits.tennis.domain.tournament.TournamentInfo;

public record MatchInfo(Integer id, 
        TournamentInfo tournamentInfo, 
        LocalDate date,
        Player player1,
        UTR player1UTR,
        Player player2,
        UTR player2UTR,
        MatchResult result,
        UTR matchUTRForPlayer1,
        UTR matchUTRForPlayer2,
        boolean isUpset) {

    public boolean matches(String filterPart) {
        return StringUtil.cleanNameString(player1().name()).contains(filterPart) ||
               StringUtil.cleanNameString(player2().name()).contains(filterPart) ||
               StringUtil.cleanNameString(tournamentInfo.name()).contains(filterPart) ||
               StringUtil.cleanNameString(tournamentInfo.venue()).contains(filterPart);
    }
    
}
