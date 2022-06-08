package hu.kits.tennis.common;

import java.util.HashSet;
import java.util.Set;

public class CollectionsUtil {

    @SafeVarargs
    public static <T> Set<T> union(Set<T> ... sets) {
        Set<T> result = new HashSet<>();
        for(var set : sets) {
            result.addAll(set);
        }
        return Set.copyOf(result);
    }
    
}
