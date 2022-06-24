package hu.kits.tennis.infrastructure.ui.vaadin;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexLayout;

import hu.kits.tennis.infrastructure.ui.MainLayout;
import hu.kits.tennis.infrastructure.ui.vaadin.components.FlexBoxLayout;

@CssImport("./styles/components/view-frame.css")
public class SplitViewFrame extends Composite<Div> implements HasStyle {

    private String CLASS_NAME = "view-frame";

    private Div header;

    private FlexBoxLayout wrapper;
    private Div content;
    private Div details;

    private Div footer;

    public enum Position {
        RIGHT, BOTTOM
    }

    public SplitViewFrame() {
        setClassName(CLASS_NAME);

        header = new Div();
        header.setClassName(CLASS_NAME + "__header");

        wrapper = new FlexBoxLayout();
        wrapper.setClassName(CLASS_NAME + "__wrapper");

        content = new Div();
        content.setClassName(CLASS_NAME + "__content");

        details = new Div();
        details.setClassName(CLASS_NAME + "__details");

        footer = new Div();
        footer.setClassName(CLASS_NAME + "__footer");

        wrapper.add(content, details);
        getContent().add(header, wrapper, footer);
    }

    public void setViewHeader(Component... components) {
        header.removeAll();
        header.add(components);
    }

    public void setViewContent(Component... components) {
        content.removeAll();
        content.add(components);
    }

    public void setViewDetails(Component... components) {
        details.removeAll();
        details.add(components);
    }

    public void setViewDetailsPosition(Position position) {
        if (position.equals(Position.RIGHT)) {
            wrapper.setFlexDirection(FlexLayout.FlexDirection.ROW);

        } else if (position.equals(Position.BOTTOM)) {
            wrapper.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        }
    }

    public void setViewFooter(Component... components) {
        footer.removeAll();
        footer.add(components);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        MainLayout.get().getAppBar().reset();
    }
}
