package hu.kits.tennis.domain.tournament;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.function.Function;

import hu.kits.tennis.domain.utr.Match;

public class TournamentMatches {

    private final Map<Integer, Map<Integer, Match>> matches;

    public TournamentMatches(Map<Integer, Map<Integer, Match>> matches) {
        this.matches = matches;
    }

    public static TournamentMatches empty() {
        return new TournamentMatches(Map.of());
    }

    public Map<Integer, Match> matchesInBoard(int boardNumber) {
        return matches.getOrDefault(boardNumber, Map.of()).values().stream()
                .collect(toMap(Match::tournamentMatchNumber, Function.identity()));
    }
    
}
