package hu.kits.tennis.domain.tournament;

import java.util.Map;
import java.util.Optional;

import hu.kits.tennis.common.MathUtil;
import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.player.Player;

public record TournamentBoard(int numberOfRounds, Map<Integer, Match> matches) {
    
    public Match getMatch(int round, int matchNumberInRound) {
        return matches.get(matchNumber(round, matchNumberInRound));
    }
    
    public Match getMatch(int matchNumber) {
        return matches.get(matchNumber);
    }
    
    private int matchNumber(int round, int matchNumberInRound) {
        return MathUtil.pow2(numberOfRounds) - MathUtil.pow2(numberOfRounds - round + 1) + matchNumberInRound;
    }
    
    public int nextRoundMatchNumber(Integer matchNumber) {
        var roundAndMatchNumberInRound = MathUtil.roundAndMatchNumberInRound(matchNumber, numberOfRounds);
        int round = roundAndMatchNumberInRound.first();
        int matchNumberInRound = roundAndMatchNumberInRound.second();
        
        return matchNumber(round + 1, (matchNumberInRound + 1) / 2);
    }
    
    public Optional<Match> findPrevMatch(Match match, Player player) {
        var roundAndMatchNumberInRound = MathUtil.roundAndMatchNumberInRound(match.tournamentMatchNumber(), numberOfRounds);
        int round = roundAndMatchNumberInRound.first();
        int matchNumberInRound = roundAndMatchNumberInRound.second();
        Match match1 = getMatch(round-1, matchNumberInRound * 2 - 1);
        if(match1 != null && match1.hasPlayer(player)) {
            if(match1.player2().equals(player)) {
                match1 = match1.swap();
            }
            return Optional.of(match1);
        }
        Match match2 = getMatch(round-1, matchNumberInRound * 2);
        if(match2 != null && match2.hasPlayer(player)) {
            if(match2.player2().equals(player)) {
                match2 = match2.swap();
            }
            return Optional.of(match2);
        }
        return Optional.empty();
    }

    public int roundNumber(Match match) {
        return MathUtil.roundAndMatchNumberInRound(match.tournamentMatchNumber(), numberOfRounds).first();
    }

    public boolean isFinal(Match match) {
        return roundNumber(match) == numberOfRounds;
    }

    public Match finalMatch() {
        return matches.get(MathUtil.pow2(numberOfRounds)-1);
    }
    
}
