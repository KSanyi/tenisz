package hu.kits.tennis.domain.match;

import java.time.LocalDate;

import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.common.StringUtil;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.tournament.BasicTournamentInfo;
import hu.kits.tennis.domain.utr.UTR;

public record MatchInfo(Integer id, 
        BasicTournamentInfo tournamentInfo, 
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
               date != null && Formatters.formatDate(date).contains(filterPart);
    }
    
}
