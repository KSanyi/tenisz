package hu.kits.tennis.domain.tournament;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
        List<Match> matches) {

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
    
    public List<Player> players() {
        return contestants.stream()
                .sorted(comparing(Contestant::rank)).map(Contestant::player)
                .collect(toList());
    }
    
    @Override
    public String toString() {
        return name + "(" + id + ")";
    }
    
}
