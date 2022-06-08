package hu.kits.tennis.infrastructure.ui.vaadin.components;

import com.vaadin.flow.component.orderedlayout.FlexComponent;

import hu.kits.tennis.infrastructure.ui.vaadin.util.FontSize;
import hu.kits.tennis.infrastructure.ui.vaadin.util.FontWeight;
import hu.kits.tennis.infrastructure.ui.vaadin.util.LumoStyles;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.BorderRadius;

public class Initials extends FlexBoxLayout {

    private static final String CLASS_NAME = "initials";

    public Initials() {
        setAlignItems(FlexComponent.Alignment.CENTER);
        setBackgroundColor(LumoStyles.Color.Primary._100);
        setBorderRadius(BorderRadius._50);
        setClassName(CLASS_NAME);
        UIUtils.setFontSize(FontSize.S, this);
        UIUtils.setFontWeight(FontWeight._600, this);
        setHeight(LumoStyles.Size.M);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setWidth(LumoStyles.Size.M);
    }

}
