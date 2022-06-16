package hu.kits.tennis.domain.utr;

import java.util.List;

import hu.kits.tennis.domain.tournament.TournamentMatches;

public interface MatchRepository {

    List<BookedMatch> loadAllPlayedMatches(Player player);
    
    List<BookedMatch> loadAllBookedMatches();
    
    BookedMatch save(BookedMatch bookedMatch);
    
    void deleteMatch(int matchId);

    void replaceAllBookedMatches(List<BookedMatch> recalculatedBookedMatches);

    void deleteMatchesForTournament(String id);

    TournamentMatches loadMatchesForTournament(String tournamentId);

    void setResult(MatchResultInfo matchResultInfo);

    void setPlayer1(int matchId, Player player);
    
    void setPlayer2(int matchId, Player player);
}
