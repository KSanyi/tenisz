package hu.kits.tennis.domain.ktr;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import hu.kits.tennis.common.StringUtil;

public class PlayersWithKTR {

    private final Map<Integer, PlayerWithKTR> idToPlayerWithKTR;
    
    public PlayersWithKTR(List<PlayerWithKTR> playersWithKTR) {
        idToPlayerWithKTR = playersWithKTR.stream().collect(Collectors.toMap(p -> p.player().id(), p -> p));
    }

    public List<PlayerWithKTR> entries() {
        return idToPlayerWithKTR.values().stream()
                .filter(p -> p.player().id() != 0)
                .sorted((p1, p2) -> StringUtil.HUN_COLLATOR.compare(p1.player().name(), p2.player().name()))
                .toList();
    }

    public KTR getKTR(Integer id) {
        PlayerWithKTR playerWithKTR = idToPlayerWithKTR.get(id);
        return playerWithKTR != null ? playerWithKTR.ktr() : KTR.UNDEFINED;
    }
    
}
