package hu.kits.tennis.infrastructure.ui.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;

public class RouteNotFoundView extends Div implements HasErrorParameter<NotFoundException>{

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        event.forwardTo(HomeView.class);
        return 0;
    }

}
