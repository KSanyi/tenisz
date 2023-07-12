package hu.kits.tennis.common;

import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
    
    public static <K, V> Map<K, V> filterByValue(Map<K, V> map, Predicate<V> predicate) {
        return map.entrySet().stream()
                .filter(e -> predicate.test(e.getValue()))
                .collect(toMap(Entry::getKey, Entry::getValue));
    }
    
    public static <K, V> Map<K, V> mapBy(Collection<V> collection, Function<V, K> key) {
        return collection.stream().collect(Collectors.toMap(key, Function.identity()));
    }

    public static <T> Set<T> diff(Collection<T> collection1, Collection<T> collection2) {
        return collection1.stream().filter(e -> !collection2.contains(e)).collect(Collectors.toSet());
    }

    public static <T> Set<T> intersection(Collection<T> collection1, Collection<T> collection2) {
        return collection1.stream().filter(collection2::contains).collect(Collectors.toSet());
    }
    
}
