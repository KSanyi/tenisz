package hu.kits.tennis.domain.player;

import java.util.Objects;
import java.util.Set;

import hu.kits.tennis.common.StringUtil;
import hu.kits.tennis.domain.ktr.KTR;
import hu.kits.tennis.domain.tournament.Organization;

public record Player(Integer id,
        String name,
        Contact contact,
        KTR startingKTR, 
        Set<Organization> organisations) {

    public static Player BYE = new Player(0, "Bye", Contact.EMPTY, KTR.UNDEFINED, Set.of());

    public static Player createNew(String name) {
        return new Player(null, name, Contact.EMPTY, KTR.UNDEFINED, Set.of());
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

    public static record Contact(String email, String phone, Address address, String comment) {
        
        public static Contact EMPTY = new Contact("", "", Address.EMPTY, "");
    }
    
    public record Address(int zip, String town, String streetAddress) {
        
        public static Address EMPTY = new Address(0, "", "");

        public boolean isEmpty() {
            return this.equals(EMPTY);
        }
        
        public String toString() {
            if(isEmpty()) {
                return "";
            } else {
                return zip + " " + town + " " + streetAddress;
            }
        }
    }
    
    @Override
    public String toString() {
        return name + "(" + id + ")";
    }
    
}
