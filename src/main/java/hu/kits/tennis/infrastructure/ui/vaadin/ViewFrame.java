package hu.kits.tennis.infrastructure.ui.vaadin;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;

import hu.kits.tennis.infrastructure.ui.MainLayout;

@CssImport("./styles/components/view-frame.css")
public class ViewFrame extends Composite<Div> implements HasStyle {

    private String CLASS_NAME = "view-frame";

    private Div header;
    private Div content;
    private Div footer;

    public ViewFrame() {
        setClassName(CLASS_NAME);

        header = new Div();
        header.setClassName(CLASS_NAME + "__header");

        content = new Div();
        content.setClassName(CLASS_NAME + "__content");

        footer = new Div();
        footer.setClassName(CLASS_NAME + "__footer");

        getContent().add(header, content, footer);
    }

    public void setViewHeader(Component... components) {
        header.removeAll();
        header.add(components);
    }

    public void setViewContent(Component... components) {
        content.removeAll();
        content.add(components);
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
