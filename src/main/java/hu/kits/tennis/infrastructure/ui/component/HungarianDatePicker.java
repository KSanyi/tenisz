package hu.kits.tennis.infrastructure.ui.component;

import java.util.List;
import java.util.Locale;

import com.vaadin.flow.component.datepicker.DatePicker;

public class HungarianDatePicker extends DatePicker  {

    public HungarianDatePicker() {
        this("");
    }
    
    public HungarianDatePicker(String caption) {
        setLabel(caption);
        setLocale(new Locale("HU"));
        
        setI18n(new DatePickerI18n()
                .setWeek("Hét")
                .setCalendar("Kalendár")
                .setToday("Ma")
                .setCancel("Mégsem")
                .setFirstDayOfWeek(1)
                .setMonthNames(List.of("Január", "Február", "Március", "Április", "Május", "Június", "Július", "Augusztus", "Szeptember", "Október", "November", "December"))
                .setWeekdays(List.of("Vasárnap", "Hétfő", "Kedd", "Szerda", "Csütörtök", "Péntek", "Szombat"))
                .setWeekdaysShort(List.of("V", "H", "K", "Sze", "Cs", "P", "Szo")));
    }
    
}
