package hu.kits.tennis.infrastructure.ui.vaadin.components.navigation.drawer;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;

import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

@CssImport("./styles/components/brand-expression.css")
public class BrandExpression extends Div {

    private static final String CLASS_NAME = "brand-expression";

    private final Image logo;
    private final Label title;

    public BrandExpression() {
        setClassName(CLASS_NAME);

        logo = new Image();
        logo.setAlt(" logo");
        logo.setClassName(CLASS_NAME + "__logo");

        title = UIUtils.createH3Label("OPFR");
        title.addClassName(CLASS_NAME + "__title");

        add(logo, title);
    }
    
}
