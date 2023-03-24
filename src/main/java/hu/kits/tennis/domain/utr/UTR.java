package hu.kits.tennis.domain.utr;

import java.util.Objects;

public record UTR(Double value) implements Comparable<UTR> {

    public static UTR UNDEFINED = new UTR(null);
    
    public UTR calculateMatchUTR(double score) {
        return isUndefinded() ? UNDEFINED : new UTR(value + 2 * score);
    }
    
    public boolean isUndefinded() {
        return this == UNDEFINED;
    }
    
    public boolean isDefinded() {
        return !isUndefinded();
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
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        UTR other = (UTR) obj;
        if(value == null && other.value != null) return false;
        if(value != null && other.value == null) return false;
        return Math.abs(value - other.value) < 0.01;
    }

    public static UTR of(Double value) {
        return value != null ? new UTR(value) : UTR.UNDEFINED;
    }

    public int utrGroup() {
        return value != null ? value.intValue() : 0;
    }

}
