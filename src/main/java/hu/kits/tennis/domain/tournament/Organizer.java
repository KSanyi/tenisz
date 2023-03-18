package hu.kits.tennis.domain.tournament;

public enum Organizer {
    
    KVTK("KVTK");
    
    public final String name;

    private Organizer(String name) {
        this.name = name;
    }
    
}
