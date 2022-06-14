package hu.kits.tennis.domain.utr;

public record Player(int id, String name, Integer utrGroup) {

    public static Player BYE = new Player(0, "Bye", null);
    
}
