package hu.kits.tennis.domain.utr;

import hu.kits.tennis.common.StringUtil;

public record Player(Integer id, String name, UTR startingUTR) {

    public static Player BYE = new Player(0, "Bye", UTR.UNDEFINED);

    public static Player createNew(String name) {
        return new Player(null, name, UTR.UNDEFINED);
    }
    public boolean matches(String filter) {
        String cleanedFilter = StringUtil.cleanNameString(filter);
        return StringUtil.cleanNameString(name).contains(cleanedFilter) || String.valueOf(id).contains(filter);
    }
    
}
