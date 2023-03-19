package hu.kits.tennis.domain.tournament;

public enum Organization {
    
    KVTK("KVTK"), DEBRECEN("DEBRECEN");
    
    public final String name;

    private Organization(String name) {
        this.name = name;
    }
    
}
