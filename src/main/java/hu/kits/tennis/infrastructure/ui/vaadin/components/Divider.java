package hu.kits.tennis.infrastructure.ui.vaadin.components;

import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;

import hu.kits.tennis.infrastructure.ui.vaadin.util.LumoStyles;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

public class Divider extends FlexBoxLayout implements HasSize, HasStyle {

    private static final String CLASS_NAME = "divider";

    private final Div divider;

    public Divider(String height) {
        this(FlexComponent.Alignment.CENTER, height);
    }

    public Divider(FlexComponent.Alignment alignItems, String height) {
        setAlignItems(alignItems);
        setClassName(CLASS_NAME);
        setHeight(height);

        divider = new Div();
        UIUtils.setBackgroundColor(LumoStyles.Color.Contrast._10, divider);
        divider.setHeight("1px");
        divider.setWidthFull();
        add(divider);
    }

}
