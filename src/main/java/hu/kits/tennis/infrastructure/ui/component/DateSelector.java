package hu.kits.tennis.infrastructure.ui.component;

import java.time.LocalDate;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;

import hu.kits.tennis.common.Clock;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

public class DateSelector extends CustomField<LocalDate> {

    private final HungarianDatePicker datePicker = new HungarianDatePicker();
    private final Button prevDayButton = UIUtils.createButton(VaadinIcon.ARROW_LEFT, ButtonVariant.LUMO_TERTIARY);
    private final Button nextDayButton = UIUtils.createButton(VaadinIcon.ARROW_RIGHT, ButtonVariant.LUMO_TERTIARY);
    
    public DateSelector() {
        
        prevDayButton.addClickListener(click -> prevDay());
        nextDayButton.addClickListener(click -> nextDay());
        
        datePicker.getElement().setAttribute("theme", "small");
        datePicker.setWidth("120px");
        
        add(new HorizontalLayout(prevDayButton, datePicker, nextDayButton));
    }
    
    private void nextDay() {
        changeDate(1);
    }

    private void prevDay() {
        changeDate(-1);
    }
    
    private void changeDate(int dayChange) {
        LocalDate currentValue = datePicker.getValue();
        if(currentValue != null) {
            datePicker.setValue(currentValue.plusDays(dayChange));
        } else {
            datePicker.setValue(Clock.today());
        }
    }
    
    public Registration addDateValueChangeListener(ValueChangeListener<? super ComponentValueChangeEvent<DatePicker, LocalDate>> listener) {
        return datePicker.addValueChangeListener(listener);
    }
    
    @Override
    protected LocalDate generateModelValue() {
        return datePicker.getValue();
    }

    @Override
    protected void setPresentationValue(LocalDate value) {
        datePicker.setValue(value);
    }

}
