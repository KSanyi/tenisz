package hu.kits.tennis.domain.utr;

public record Player(Integer id, String name, Integer utrGroup) {

    public static Player BYE = new Player(0, "Bye", 0);

    public static Player createNew(String name) {
        return new Player(null, name, 0);
    }
    
}
