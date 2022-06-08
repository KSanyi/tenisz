package hu.kits.tennis.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class CollectionsUtilTest {

    @Test
    void cleaning() {
        
        Set<Integer> setA = new HashSet<>(List.of(1, 2));
        Set<Integer> setB = new HashSet<>(List.of(1, 3));
        
        assertEquals(new HashSet<>(List.of(1, 2, 3)), CollectionsUtil.union(setA, setB));
    }

}