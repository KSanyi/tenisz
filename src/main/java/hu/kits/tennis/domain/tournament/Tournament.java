package hu.kits.tennis.domain.tournament;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import hu.kits.tennis.common.Pair;
import hu.kits.tennis.domain.utr.Match;
import hu.kits.tennis.domain.utr.Player;

public record Tournament(String id, 
        LocalDate date, 
        String name,
        String venue,
        Type type,
        int bestOfNSets,
        List<Contestant> contestants, 
        Status status, 
        Map<Integer, Match> matches) {
    
    public static Tournament createNew(String name, String venue, LocalDate date, Tournament.Type type, int bestOfNSets) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        return new Tournament(id, date, name, venue, type, bestOfNSets, List.of(), Status.DRAFT, Map.of());
    }
    
    public static enum Type {
        SIMPLE_BOARD,
        BOARD_AND_CONSOLATION,
        FULL_BOARD;
    }
    
    public static enum Status {
        DRAFT,
        LIVE;
    }
    
    public List<Player> players() {
        return contestants.stream()
                .sorted(comparing(Contestant::rank)).map(Contestant::player)
                .collect(toList());
    }
    
    @Override
    public String toString() {
        return name + "(" + id + ")";
    }

    public int numberOfRounds() {
        int numberOfContestants = contestants.size();
        for(int i=1;i<twoPows.length;i++) {
            if(numberOfContestants <= twoPows[i]) {
                return i;
            }
        }
        throw new IllegalArgumentException();
    }
    
    public Match getMatch(int round, int matchNumberInRound) {
        return matches.get(matchNumber(round, matchNumberInRound));
    }
    
    private int matchNumber(int round, int matchNumberInRound) {
        int numberOfRounds = numberOfRounds();
        return pow2(numberOfRounds) - pow2(numberOfRounds - round + 1) + matchNumberInRound;
    }
    
    public Pair<Integer, Integer> roundAndMatchNumberInRound(int matchNumber) {
        int numberOfRounds = numberOfRounds();
        int counter = 0;
        int round = 1;
        for(round=1;round<twoPows.length;round++) {
            if(matchNumber > counter) {
                counter += twoPows[numberOfRounds - round];
            } else {
                break;
            }
        }
        round--;
        int matchNumberInRound = matchNumber - (pow2(numberOfRounds) - pow2(numberOfRounds - round + 1));
        
        return new Pair<>(round, matchNumberInRound);
    }
    
    public int nextRoundMatchNumber(Integer matchNumber) {
        var roundAndMatchNumberInRound = roundAndMatchNumberInRound(matchNumber);
        int round = roundAndMatchNumberInRound.getFirst();
        int matchNumberInRound = roundAndMatchNumberInRound.getSecond();
        
        return matchNumber(round + 1, (matchNumberInRound + 1) / 2);
    }
    
    private static final int[] twoPows = new int[] {1, 2, 4, 8, 16, 32, 64, 128, 256};

    private static int pow2(int x) {
        return twoPows[x];
    }

    public Optional<Match> findPrevMatch(Match match, Player player) {
        var roundAndMatchNumberInRound = roundAndMatchNumberInRound(match.tournamentMatchNumber());
        int round = roundAndMatchNumberInRound.getFirst();
        int matchNumberInRound = roundAndMatchNumberInRound.getSecond();
        Match match1 = getMatch(round-1, matchNumberInRound * 2 - 1);
        if(match1 != null && match1.hasPlayed(player)) {
            if(match1.player2().equals(player)) {
                match1 = match1.swap();
            }
            return Optional.of(match1);
        }
        Match match2 = getMatch(round-1, matchNumberInRound * 2);
        if(match2 != null && match2.hasPlayed(player)) {
            if(match2.player2().equals(player)) {
                match2 = match2.swap();
            }
            return Optional.of(match2);
        }
        return Optional.empty();
    }
    
}
