package hu.kits.tennis.common;

import static java.util.stream.Collectors.toMap;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

public class CollectionsUtil {

    @SafeVarargs
    public static <T> Set<T> union(Set<T> ... sets) {
        Set<T> result = new HashSet<>();
        for(var set : sets) {
            result.addAll(set);
        }
        return Set.copyOf(result);
    }
    
    public static <K, V, W> Map<K, W> mapValues(Map<K, V> map, Function<V, W> mappingFunction) {
        return map.entrySet().stream()
                .collect(toMap(Entry::getKey, e -> mappingFunction.apply(e.getValue())));
    }
    
}
