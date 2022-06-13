package hu.kits.tennis.domain.tournament;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import hu.kits.tennis.common.MathUtil;
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
        List<Board> boards) {
    
    public static Tournament createNew(String name, String venue, LocalDate date, Tournament.Type type, int bestOfNSets) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        return new Tournament(id, date, name, venue, type, bestOfNSets, List.of(), Status.DRAFT, List.of());
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
    
    public static record Board(int numberOfRounds, Map<Integer, Match> matches) {
        
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
            int round = roundAndMatchNumberInRound.getFirst();
            int matchNumberInRound = roundAndMatchNumberInRound.getSecond();
            
            return matchNumber(round + 1, (matchNumberInRound + 1) / 2);
        }
        
        public Optional<Match> findPrevMatch(Match match, Player player) {
            var roundAndMatchNumberInRound = MathUtil.roundAndMatchNumberInRound(match.tournamentMatchNumber(), numberOfRounds);
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

        public int roundNumber(Match match) {
            return MathUtil.roundAndMatchNumberInRound(match.tournamentMatchNumber(), numberOfRounds).getFirst();
        }
        
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

    public Match getMatch(int boardNumber, int matchNumber) {
        return boards.get(boardNumber-1).getMatch(matchNumber);
    }

    public int nextRoundMatchNumber(Match match) {
        return boards.get(match.tournamentBoardNumber()-1).nextRoundMatchNumber(match.tournamentMatchNumber());
    }

    public Board mainBoard() {
        return boards.get(0);
    }

    public Board consolationBoard() {
        return boards.get(1);
    }
    
}
