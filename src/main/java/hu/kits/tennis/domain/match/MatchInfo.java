package hu.kits.tennis.domain.match;

import java.time.LocalDate;

import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.common.StringUtil;
import hu.kits.tennis.domain.ktr.KTR;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.tournament.BasicTournamentInfo;

public record MatchInfo(Integer id, 
        BasicTournamentInfo tournamentInfo, 
        LocalDate date,
        Player player1,
        KTR player1KTR,
        Player player2,
        KTR player2KTR,
        MatchResult result,
        KTR matchKTRForPlayer1,
        KTR matchKTRForPlayer2,
        boolean isUpset) {

    public boolean matches(String filterPart) {
        return StringUtil.cleanNameString(player1().name()).contains(filterPart) ||
               StringUtil.cleanNameString(player2().name()).contains(filterPart) ||
               StringUtil.cleanNameString(tournamentInfo.name()).contains(filterPart) ||
               date != null && Formatters.formatDate(date).contains(filterPart);
    }
    
    public LocalDate dateForCompare() {
        return date != null ? date : LocalDate.MAX;
    }
    
}
