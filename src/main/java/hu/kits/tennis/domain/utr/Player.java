package hu.kits.tennis.domain.utr;

import java.util.Set;

import hu.kits.tennis.common.StringUtil;
import hu.kits.tennis.domain.tournament.Organizer;

public record Player(Integer id, String name, UTR startingUTR, Set<Organizer> organisations) {

    public static Player BYE = new Player(0, "Bye", UTR.UNDEFINED, Set.of());

    public static Player createNew(String name) {
        return new Player(null, name, UTR.UNDEFINED, Set.of());
    }
    public boolean matches(String filter) {
        String cleanedFilter = StringUtil.cleanNameString(filter);
        return StringUtil.cleanNameString(name).contains(cleanedFilter) || String.valueOf(id).contains(filter);
    }
    
}
