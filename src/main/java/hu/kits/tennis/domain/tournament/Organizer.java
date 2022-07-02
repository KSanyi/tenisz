package hu.kits.tennis.domain.tournament;

public enum Organizer {
    
    BVSC("BVSC"),
    MATK("MATK"),
    KVTK("KVTK"),
    TENISZPARTNER("Tenisz Partner"),
    NA("NA");
    
    public final String name;

    private Organizer(String name) {
        this.name = name;
    }
    
}
