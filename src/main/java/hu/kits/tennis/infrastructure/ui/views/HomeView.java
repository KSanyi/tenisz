package hu.kits.tennis.infrastructure.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import hu.kits.tennis.infrastructure.ui.views.tournaments.TournamentsView;

@Route(value = "")
@PageTitle("Home")
public class HomeView extends Div {

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI.getCurrent().navigate(TournamentsView.class);
    }
    
}
