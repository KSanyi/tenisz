package hu.kits.tennis.domain.tournament;

import static java.util.stream.Collectors.toMap;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import hu.kits.tennis.common.MathUtil;
import hu.kits.tennis.domain.utr.Match;
import hu.kits.tennis.domain.utr.Player;

public record Tournament(String id,
        Organizer organizer,
        LocalDate date, 
        String name,
        String venue,
        Type type,
        int bestOfNSets,
        List<Contestant> contestants, 
        Status status, 
        List<Board> boards) {
    
    public static Tournament createNew(Organizer organizer, String name, String venue, LocalDate date, Tournament.Type type, int bestOfNSets) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        return new Tournament(id, organizer, date, name, venue, type, bestOfNSets, List.of(), Status.DRAFT, List.of());
    }
    
    public static enum Type {
        SIMPLE_BOARD,
        BOARD_AND_CONSOLATION,
        FULL_BOARD,
        NA;
    }
    
    public static enum Status {
        LIVE,
        DRAFT,
        COMPLETED;
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
    
    public List<Player> playersLineup() {
        
        var playersByRank = contestants.stream().collect(toMap(Contestant::rank, Contestant::player));
        
        List<Player> lineup = new ArrayList<>();
        for(int i=1;i<=MathUtil.pow2(mainBoard().numberOfRounds);i++) {
            Player player = playersByRank.getOrDefault(i, Player.BYE);
            if(player != Player.BYE || status == Tournament.Status.DRAFT) {
                lineup.add(player);    
            }
        }
        
        return lineup;
    }
    
    @Override
    public String toString() {
        return name + "(" + id + ")";
    }

    public Match getMatch(int boardNumber, int matchNumber) {
        return getBoard(boardNumber).getMatch(matchNumber);
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

    public boolean isBoardFinal(Match match) {
        return boards.get(match.tournamentBoardNumber() - 1).isFinal(match);
    }

    private Board getBoard(Integer tournamentBoardNumber) {
        return boards.get(tournamentBoardNumber - 1);
    }

    public Match followUpMatch(Match match) {
        int nextRoundMatchNumber = nextRoundMatchNumber(match);
        return getMatch(match.tournamentBoardNumber(), nextRoundMatchNumber);
    }

    public TournamentInfo tournamentInfo() {
        return new TournamentInfo(id, organizer, date, name, venue, bestOfNSets, contestants.size());
    }

    public List<Match> matches() {
        return boards.stream().flatMap(b -> b.matches().values().stream()).toList();
    }
    
}
