package hu.kits.tennis.domain.player;

import java.util.Objects;
import java.util.Set;

import hu.kits.tennis.common.StringUtil;
import hu.kits.tennis.domain.tournament.Organization;
import hu.kits.tennis.domain.utr.UTR;

public record Player(Integer id,
        String name,
        Contact contact,
        UTR startingUTR, 
        Set<Organization> organisations) {

    public static Player BYE = new Player(0, "Bye", Contact.EMPTY, UTR.UNDEFINED, Set.of());

    public static Player createNew(String name) {
        return new Player(null, name, Contact.EMPTY, UTR.UNDEFINED, Set.of());
    }
    public boolean matches(String filter) {
        String cleanedFilter = StringUtil.cleanNameString(filter);
        return StringUtil.cleanNameString(name).contains(cleanedFilter) || String.valueOf(id).contains(filter);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Player other = (Player) obj;
        return Objects.equals(id, other.id);
    }

    public static record Contact(String email, String phone, String comment) {
        
        public static Contact EMPTY = new Contact("", "", "");
    }
    
    @Override
    public String toString() {
        return name + "(" + id + ")";
    }
    
}
