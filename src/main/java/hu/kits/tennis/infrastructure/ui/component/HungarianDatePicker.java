package hu.kits.tennis.infrastructure.ui.component;

import java.util.List;

import com.vaadin.flow.component.datepicker.DatePicker;

import hu.kits.tennis.common.LocaleUtil;

public class HungarianDatePicker extends DatePicker  {

    public HungarianDatePicker() {
        this("");
    }
    
    public HungarianDatePicker(String caption) {
        setLabel(caption);
        setLocale(LocaleUtil.HUN_LOCALE);
        
        setI18n(new DatePickerI18n()
                .setToday("Ma")
                .setCancel("Mégsem")
                .setFirstDayOfWeek(1)
                .setMonthNames(List.of("Január", "Február", "Március", "Április", "Május", "Június", "Július", "Augusztus", "Szeptember", "Október", "November", "December"))
                .setWeekdays(List.of("Vasárnap", "Hétfő", "Kedd", "Szerda", "Csütörtök", "Péntek", "Szombat"))
                .setWeekdaysShort(List.of("V", "H", "K", "Sze", "Cs", "P", "Szo")));
    }
    
}
