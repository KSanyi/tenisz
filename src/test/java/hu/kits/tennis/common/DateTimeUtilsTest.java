package hu.kits.tennis.common;

import static java.time.LocalDateTime.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DateTimeUtilsTest {

    @Test
    void closestRoundHour() {
        
        assertEquals(of(2021,1,10, 11,0), DateTimeUtils.closestRoundHour(of(2021,1,10, 11,23)));
        assertEquals(of(2021,1,10, 12,0), DateTimeUtils.closestRoundHour(of(2021,1,10, 11,31))); 
    }
    
    @Test
    void calculateDiff() {
        // TODO
    }
    
}
