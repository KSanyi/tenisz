package hu.kits.tennis.common;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Clock class for being able to test date dependent classes. 
 * By default returns the current date, but can be set to return a static date too. 
 *
 */
public class Clock {

    private Clock(){}
    
    private static LocalDateTime staticTime; 
    
    public static void setStaticTime(LocalDateTime time) {
        staticTime = time;
    }
    
    public static LocalDateTime now() {
        if(staticTime == null) {
            return LocalDateTime.now();
        } else {
            return staticTime;
        }
    }
    
    public static LocalDate today() {
        return now().toLocalDate();
    }
    
}