package hu.kits.tennis.domain.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class TimeRangeTest {

    @Test
    void construction() {
        
        assertThrows(IllegalArgumentException.class, () -> {
            new TimeRange(24, 1);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new TimeRange(10, 15);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new TimeRange(10, -1);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new TimeRange(10, 0);
        });

        new TimeRange(0, 1);
        new TimeRange(22, 2);
    }
    
    @Test
    void basic() {
        
        TimeRange timeRange = new TimeRange(10, 2);
        
        assertTrue(timeRange.contains(10));
        assertTrue(timeRange.contains(11));
        assertFalse(timeRange.contains(12));
        
        assertEquals(10, timeRange.startAt());
        assertEquals(12, timeRange.endAt());
        assertEquals(2, timeRange.hours());
        
        assertEquals("10:00 - 12:00", timeRange.format());
    }
    
    @Test
    void intersection() {
        
        assertTrue(new TimeRange(10, 1).intersectWith(new TimeRange(10, 1)));
        assertTrue(new TimeRange(10, 2).intersectWith(new TimeRange(11, 1)));
        assertTrue(new TimeRange(11, 1).intersectWith(new TimeRange(10, 2)));
        assertTrue(new TimeRange(12, 1).intersectWith(new TimeRange(10, 4)));
        assertTrue(new TimeRange(10, 4).intersectWith(new TimeRange(12, 1)));
        
        assertFalse(new TimeRange(10, 2).intersectWith(new TimeRange(12, 1)));
        assertFalse(new TimeRange(12, 1).intersectWith(new TimeRange(10, 2)));
        assertFalse(new TimeRange(10, 2).intersectWith(new TimeRange(15, 2)));
        assertFalse(new TimeRange(15, 2).intersectWith(new TimeRange(10, 2)));
    }
    
    @Test
    void minus() {
        TimeRange rangeA = new TimeRange(8, 12);
        TimeRange rangeB = new TimeRange(6,2);
        TimeRange rangeC = new TimeRange(10,2);
        TimeRange rangeD = new TimeRange(6,4);
        TimeRange rangeE = new TimeRange(15,1);
        
        assertEquals(List.of(rangeA), rangeA.minus(rangeB));
        assertEquals(List.of(), rangeA.minus(rangeA));
        assertEquals(List.of(new TimeRange(8, 2), new TimeRange(12, 8)), rangeA.minus(rangeC));
        assertEquals(List.of(), rangeC.minus(rangeA));
        assertEquals(List.of(new TimeRange(10, 10)), rangeA.minus(rangeD));
        assertEquals(List.of(new TimeRange(6, 2)), rangeD.minus(rangeA));
        
        
        assertEquals(List.of(new TimeRange(8, 2), new TimeRange(12, 3), new TimeRange(16, 4)), rangeA.minus(List.of(rangeC, rangeE)));
    }
    
    @Test
    void merge() {
        assertEquals(new TimeRange(8, 4), new TimeRange(8, 2).merge(new TimeRange(9, 3)));
        assertEquals(new TimeRange(8, 4), new TimeRange(9, 3).merge(new TimeRange(8, 2)));
        assertEquals(new TimeRange(8, 5), new TimeRange(8, 2).merge(new TimeRange(10, 3)));
        assertEquals(new TimeRange(6, 6), new TimeRange(8, 2).merge(new TimeRange(6, 6)));
    }
    
    @Test
    void union() {
        assertEquals(List.of(new TimeRange(8, 12)), TimeRange.union(List.of(new TimeRange(8, 2), new TimeRange(10, 6), new TimeRange(14, 6))));
        assertEquals(List.of(new TimeRange(8, 2), new TimeRange(14, 3)), TimeRange.union(List.of(new TimeRange(8, 2), new TimeRange(14, 3))));
        assertEquals(List.of(new TimeRange(8, 2), new TimeRange(12, 2)), TimeRange.union(List.of(new TimeRange(8, 1), new TimeRange(9, 1), new TimeRange(12, 1), new TimeRange(13, 1))));
    }
    
}
