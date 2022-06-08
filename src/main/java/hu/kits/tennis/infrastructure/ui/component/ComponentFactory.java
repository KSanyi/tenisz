package hu.kits.tennis.infrastructure.ui.component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextAreaVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;

public class ComponentFactory {

    public static TextField createTextField(String caption) {
        TextField textField = new TextField(caption);
        textField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        textField.setAutoselect(true);
        return textField;
    }
    
    public static TextArea createTextArea(String caption) {
        TextArea textArea = new TextArea(caption);
        textArea.addThemeVariants(TextAreaVariant.LUMO_SMALL);
        return textArea;
    }
    
    public static IntegerField createIntField(String label, int width, String suffix) {
        IntegerField field = new IntegerField();
        field.setLabel(label);
        //field.setClearButtonVisible(true);
        field.setSuffixComponent(new Span(suffix));
        field.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT, TextFieldVariant.LUMO_SMALL);
        field.setWidth(width + "px");
        field.setMin(0);
        field.setAutoselect(true);
        return field;
    }
    
    public static IntegerField createIntField(int width, String suffix) {
        return createIntField(null, width, suffix);
    }
    
    public static BigDecimalField createBigDecimalField(int width, String suffix) {
        return createBigDecimalField(null, width, suffix);
    }
    
    public static BigDecimalField createBigDecimalField(String caption, int width, String suffix) {
        BigDecimalField field = new BigDecimalField(caption);
        field.setLocale(new Locale("HU"));
        field.setSuffixComponent(new Span(suffix));
        field.setWidth(width + "px");
        //field.setStep(0.01);
        field.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT, TextFieldVariant.LUMO_SMALL);
        field.setAutoselect(true);
        return field;
    }
    
    public static Span createSpacer(String width) {
        Span spacer = new Span();
        spacer.setWidth(width);
        return spacer;
    }
    
    public static Div createVerticalSpacer(int height) {
        Div div = new Div();
        div.setHeight(height + "px");
        return div;
    }
    
    public static Span createSpacer(int width) {
        return createSpacer(width + "px");
    }
    
    public static <T> ComboBox<T> createComboBox(String caption, ItemLabelGenerator<T> itemLabelGenerator, List<T> items) {
        ComboBox<T> comboBox = createComboBox(100);
        comboBox.setLabel(caption);
        comboBox.setItems(items);
        comboBox.setItemLabelGenerator(itemLabelGenerator);
        
        return comboBox;
    }
    
    public static <T> ComboBox<T> createComboBox(int width, String caption) {
        ComboBox<T> comboBox = new ComboBox<>(caption);
        comboBox.setWidth(width, Unit.PIXELS);
        //comboBox.getElement().setAttribute("theme", "small");
        return comboBox;
    }
    
    public static <T> ComboBox<T> createComboBox(int width) {
        return createComboBox(width, "");
    }
    
    @SafeVarargs
    public static <T> ComboBox<T> createComboBox(String caption, ItemLabelGenerator<T> itemLabelGenerator, T ... items) {
        return createComboBox(caption, itemLabelGenerator, Arrays.asList(items));
    }
    
    @SuppressWarnings("unchecked")
    public static <T> CheckboxGroup<T> createCheckBoxGroup(String label, ItemLabelGenerator<T> itemLabelGenerator, T... items) {
        CheckboxGroup<T> checkBoxGroup = new CheckboxGroup<>();
        checkBoxGroup.setLabel(label);
        checkBoxGroup.setItems(items);
        checkBoxGroup.setItemLabelGenerator(itemLabelGenerator);
        checkBoxGroup.getElement().setAttribute("theme", "small");
        return checkBoxGroup;
    }
    
    @SafeVarargs
    public static <T> CheckboxGroup<T> createVerticalCheckBoxGroup(String label, ItemLabelGenerator<T> itemLabelGenerator, T... items) {
        CheckboxGroup<T> checkBoxGroup = createCheckBoxGroup(label, itemLabelGenerator, items);
        checkBoxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        return checkBoxGroup;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> RadioButtonGroup<T> createRadioButtonGroup(String label, T... items) {
        RadioButtonGroup<T> radioButtonGroup = new RadioButtonGroup<>();
        radioButtonGroup.setLabel(label);
        radioButtonGroup.setItems(items);
        radioButtonGroup.getElement().setAttribute("theme", "small");
        return radioButtonGroup;
    }
    
    public static HungarianDatePicker createHungarianDatePicker(String label) {
        HungarianDatePicker datePicker = new HungarianDatePicker();  
        datePicker.setLabel(label);
        return datePicker;
    }

}
