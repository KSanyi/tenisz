package hu.kits.tennis.domain.user;

import java.util.Set;

public enum Role {

    ADMIN("Admin"), 
    MEMBER("Klubtag"),
    VISITOR("Látogató"),
    ANONYMUS("ANONYMUS");
    
    private final String label;

    private Role(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
    
    public static Set<Role> all() {
        return Set.of(ADMIN, MEMBER, VISITOR);
    }

}
