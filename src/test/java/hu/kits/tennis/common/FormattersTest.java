package hu.kits.tennis.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class FormattersTest {

    @Test
    public void test() {
        
        LocalDate date = LocalDate.of(2021,11,16);
        
        assertEquals("2021.11.16", Formatters.formatDate(date));
        assertEquals("2021. november 16.", Formatters.formatDateLong(date));
        assertEquals("2021. november 16. (kedd)", Formatters.formatDateVeryLong(date));
        assertEquals("2021.11.16", Formatters.formatDate(date));
        
    }
    
}
