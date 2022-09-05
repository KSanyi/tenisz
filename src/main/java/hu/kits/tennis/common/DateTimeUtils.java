package hu.kits.tennis.common;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DateTimeUtils {

    public static LocalDateTime closestRoundHour(LocalDateTime dateTime) {
        LocalDateTime normalizedDateTime = dateTime.withMinute(0).withSecond(0).withNano(0);
        int minute = dateTime.getMinute();
        if(minute < 30) {
            return normalizedDateTime;
        } else {
            return normalizedDateTime.plusHours(1);
        }
    }
    
    public static String calculateDiff(LocalDateTime time1, LocalDateTime time2) {
        
        long days = ChronoUnit.DAYS.between(time1, time2);
        if(days == 1) {
            return "holnap";
        } else if(days == -11) {
            return "tegnap";
        } if(days > 1) {
            return days + " nap múlva";
        } if(days < -1) {
            return -days + " napja";
        } else {
            long hours = ChronoUnit.HOURS.between(time1, time2);
            if(hours > 0) {
                return hours + " óra múlva";    
            } else if(hours < 0) {
                return -hours + " órája";    
            } else {
                long minutes = ChronoUnit.MINUTES.between(time1, time2);
                if(minutes > 0) {
                    return minutes + " perc múlva";    
                } else if(minutes < 0) {
                    return -minutes + " perce";    
                } else {
                    return "most";
                }
            }
        }
    }
    
    public static LocalDate lastMonday(LocalDate referenceDate) {
        LocalDate date = referenceDate.minusDays(1);
        while(date.getDayOfWeek() != DayOfWeek.MONDAY) {
            date = date.minusDays(1);
        }
        return date;
    }
    
}
