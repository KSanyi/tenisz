package hu.kits.tennis.common;

public record Pair<S, T>(S first, T second) {

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
    
    public static <S, T> Pair<S, T> of(S first, T second) {
        return new Pair<>(first, second);
    }
    
}
