package hu.kits.tennis.infrastructure.ui.util;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.RegexpValidator;

public class DataValidator {

    public static class PhoneValidator implements Validator<String> {

        private final RegexpValidator regexpValidator = new RegexpValidator("Hibás telefonszám: a helyes formátum: +36/70-123-1234, +39/12-1234-1234", 
                "\\+\\d{2}/\\d{2}\\-\\d{3,4}-\\d{4}");
        
        @Override
        public ValidationResult apply(String value, ValueContext context) {
            return value.isEmpty() ? ValidationResult.ok() : regexpValidator.apply(value, context);
        }
        
    }
    
}
