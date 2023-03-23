package hu.kits.tennis.domain.tournament;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.List;

import hu.kits.tennis.common.MathUtil;
import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.tournament.TournamentParams.Status;

public record Tournament(String id,
        TournamentParams params,
        List<Contestant> contestants, 
        Status status, 
        List<TournamentBoard> boards) {
    
    public List<Player> playersLineup() {

        if(contestants.isEmpty()) {
            return List.of();
        }
        
        var playersByRank = contestants.stream().collect(toMap(Contestant::rank, Contestant::player));
        
        List<Player> lineup = new ArrayList<>();
        for(int i=1;i<=MathUtil.pow2(mainBoard().numberOfRounds());i++) {
            Player player = playersByRank.getOrDefault(i, Player.BYE);
            if(player != Player.BYE || status == Status.DRAFT) {
                lineup.add(player);    
            }
        }
        
        return lineup;
    }
    
    public List<Player> simplePlayersLineup() {
        
        return contestants.stream().map(Contestant::player).collect(toList());
    }
    
    @Override
    public String toString() {
        return params.name() + "(" + id + ")";
    }

    public Match getMatch(int boardNumber, int matchNumber) {
        return getBoard(boardNumber).getMatch(matchNumber);
    }

    public int nextRoundMatchNumber(Match match) {
        return boards.get(match.tournamentBoardNumber()-1).nextRoundMatchNumber(match.tournamentMatchNumber());
    }

    public TournamentBoard mainBoard() {
        return boards.get(0);
    }

    public TournamentBoard consolationBoard() {
        return boards.get(1);
    }

    public boolean isBoardFinal(Match match) {
        return boards.get(match.tournamentBoardNumber() - 1).isFinal(match);
    }

    private TournamentBoard getBoard(Integer tournamentBoardNumber) {
        return boards.get(tournamentBoardNumber - 1);
    }

    public Match followUpMatch(Match match) {
        int nextRoundMatchNumber = nextRoundMatchNumber(match);
        return getMatch(match.tournamentBoardNumber(), nextRoundMatchNumber);
    }

    public BasicTournamentInfo tournamentInfo() {
        return new BasicTournamentInfo(id, params.organization(), params.name());
    }

    public List<Match> matches() {
        return boards.stream().flatMap(b -> b.matches().values().stream()).toList();
    }
    
}
