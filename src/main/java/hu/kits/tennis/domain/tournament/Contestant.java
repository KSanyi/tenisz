package hu.kits.tennis.domain.tournament;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import hu.kits.tennis.domain.player.Player;

public record Contestant(Player player,
        int rank,
        PaymentStatus paymentStatus,
        int position) {

    public Contestant(Player player, int rank) {
        this(player, rank, PaymentStatus.NOT_PAID, 0);
    }
    
    public Contestant withRank(int rank) {
        return new Contestant(player, rank, paymentStatus, position);
    }

    public static List<Contestant> of(Player ... players) {
        return of(Arrays.asList(players));
    }

    public static List<Contestant> of(List<Player> players) {
        return IntStream.range(0, players.size())
                .mapToObj(index -> new Contestant(players.get(index), index+1))
                .collect(toList());
    }
    
}