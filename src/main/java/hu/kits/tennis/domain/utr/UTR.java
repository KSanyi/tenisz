package hu.kits.tennis.domain.utr;

public record UTR(Double value) implements Comparable<UTR> {

    public static UTR UNDEFINED = new UTR(null);
    
    public UTR calculateMatchUTR(double score) {
        return isUndefinded() ? UNDEFINED : new UTR(value + 2 * score);
    }
    
    public boolean isUndefinded() {
        return this == UNDEFINED;
    }
    
    @Override
    public String toString() {
        return this == UNDEFINED ? "-" : String.format("%2.2f", value);
    }

    public boolean comparable(UTR otherUtr) {
        boolean onlyOneIsUndefined = (isUndefinded() && !otherUtr.isUndefinded()) || (!isUndefinded() && otherUtr.isUndefinded());
        return  onlyOneIsUndefined || Math.abs(value - otherUtr.value) <= 2;
    }

    @Override
    public int compareTo(UTR otherUTR) {
        if(this == UNDEFINED) {
            return otherUTR == UNDEFINED ? 0 : -1;
        } else {
            return otherUTR == UNDEFINED ? 1 : Double.compare(value, otherUTR.value());
        }
    }
    
    public static UTR of(double value) {
        return new UTR(value);
    }

}
