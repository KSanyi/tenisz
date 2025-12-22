package hu.kits.tennis.domain.ktr;

import java.util.Objects;

public record KTR(Double value) implements Comparable<KTR> {

    public static KTR UNDEFINED = new KTR(null);
    
    public KTR calculateMatchKTR(double score) {
        return isUndefinded() ? UNDEFINED : new KTR(value + 2 * score);
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

    public boolean comparable(KTR otherKRT) {
        boolean onlyOneIsUndefined = (isUndefinded() && !otherKRT.isUndefinded()) || (!isUndefinded() && otherKRT.isUndefinded());
        return  onlyOneIsUndefined || Math.abs(value - otherKRT.value) <= 1.5;
    }

    @Override
    public int compareTo(KTR otherKTR) {
        if(this == UNDEFINED) {
            return otherKTR == UNDEFINED ? 0 : -1;
        } else {
            return otherKTR == UNDEFINED ? 1 : Double.compare(value, otherKTR.value());
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
        KTR other = (KTR) obj;
        if(value == null && other.value != null) return false;
        if(value != null && other.value == null) return false;
        return Math.abs(value - other.value) < 0.01;
    }

    public static KTR of(Double value) {
        return value != null ? new KTR(value) : KTR.UNDEFINED;
    }

    public int ktrGroup() {
        return value != null ? value.intValue() : 0;
    }

}
