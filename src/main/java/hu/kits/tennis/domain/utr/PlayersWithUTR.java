package hu.kits.tennis.domain.utr;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import hu.kits.tennis.common.StringUtil;

public class PlayersWithUTR {

    private final Map<Integer, PlayerWithUTR> idToPlayerWithUTR;
    
    public PlayersWithUTR(List<PlayerWithUTR> playersWithUTR) {
        idToPlayerWithUTR = playersWithUTR.stream().collect(Collectors.toMap(p -> p.player().id(), p -> p));
    }

    public List<PlayerWithUTR> entries() {
        return idToPlayerWithUTR.values().stream()
                .filter(p -> p.player().id() != 0)
                .sorted((p1, p2) -> StringUtil.HUN_COLLATOR.compare(p1.player().name(), p2.player().name()))
                .toList();
    }

    public UTR getUTR(Integer id) {
        return idToPlayerWithUTR.get(id).utr();
    }
    
}
