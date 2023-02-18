package hu.kits.tennis.common;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class Formatters {

    public static final Locale HU_LOCALE = new Locale("HU");
    
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    public static final DateTimeFormatter SHORT_DATE_FORMAT = DateTimeFormatter.ofPattern("yy.MM.dd");
    public static final DateTimeFormatter LONG_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy. MMMM d.", HU_LOCALE);
    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
    private static final DecimalFormat PERCENT_FORMAT;
    
    static {
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.US);
        decimalFormatSymbols.setGroupingSeparator(' ');
        PERCENT_FORMAT = new DecimalFormat("0%", decimalFormatSymbols);
    }
    
    public static String formatDate(LocalDate date) {
        return DATE_FORMAT.format(date);
    }
    
    public static String formatDateShort(LocalDate date) {
        return SHORT_DATE_FORMAT.format(date);
    }
    
    public static String formatDateLong(LocalDate date) {
        return LONG_DATE_FORMAT.format(date);
    }
    
    public static String formatDateVeryLong(LocalDate date) {
        return LONG_DATE_FORMAT.format(date) + " (" + formatDayOfWeek(date) + ")";
    }
    
    public static String formatDateTime(LocalDateTime dateTime) {
        return DATE_TIME_FORMAT.format(dateTime);
    }
    
    public static String formatShortWeekDay(LocalDate day) {
        return (day.getDayOfWeek().getDisplayName(TextStyle.SHORT, HU_LOCALE));
    }

    public static String formatDayOfWeek(LocalDate date) {
        return date.getDayOfWeek().getDisplayName(TextStyle.FULL, HU_LOCALE);
    }
    
    public static String formatPercent(double value) {
        return PERCENT_FORMAT.format(value);
    }
    
}
