package hu.kits.tennis.domain.utr;

public record Player(Integer id, String name, UTR startingUTR) {

    public static Player BYE = new Player(0, "Bye", UTR.UNDEFINED);

    public static Player createNew(String name) {
        return new Player(null, name, UTR.UNDEFINED);
    }
    
}
