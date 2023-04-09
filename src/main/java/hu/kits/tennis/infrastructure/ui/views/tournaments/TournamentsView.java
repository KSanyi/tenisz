package hu.kits.tennis.infrastructure.ui.views.tournaments;

import static com.vaadin.flow.component.grid.GridSortOrder.desc;

import java.util.List;
import java.util.function.Consumer;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentParams.Type;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.domain.tournament.TournamentSummary;
import hu.kits.tennis.domain.user.Role;
import hu.kits.tennis.infrastructure.ui.MainLayout;
import hu.kits.tennis.infrastructure.ui.util.AllowedRoles;
import hu.kits.tennis.infrastructure.ui.vaadin.SplitViewFrame;
import hu.kits.tennis.infrastructure.ui.vaadin.components.navigation.bar.AppBar;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.views.View;
import hu.kits.tennis.infrastructure.ui.views.tournament.NewTournamentDialog;
import hu.kits.tennis.infrastructure.ui.views.tournament.TournamentView;

@Route(value = "tournaments", layout = MainLayout.class)
@PageTitle("Versenyek")
@AllowedRoles({Role.ADMIN})
public class TournamentsView extends SplitViewFrame implements View {

    private final TournamentService tournamentService = Main.applicationContext.getTournamentService();

    private final Button addButton = UIUtils.createPrimaryButton("Új verseny", VaadinIcon.PLUS);
    
    private final TournamentsGrid dailyTournamentsGrid = new TournamentsGrid();
    private final TournamentsGrid tourTournamentsGrid = new TournamentsGrid();
    
    public TournamentsView() {
        addButton.addClickListener(click -> openNewTournamentDialog());
        loadTournaments();
        
        dailyTournamentsGrid.sort(desc(dailyTournamentsGrid.getColumnByKey("date")).build());
        tourTournamentsGrid.sort(desc(tourTournamentsGrid.getColumnByKey("name")).build());
    }
    
    private static void openNewTournamentDialog() {
        Consumer<Tournament> callback = tournament -> UI.getCurrent().navigate(TournamentView.class, new RouteParameters("tournamentId", tournament.id()));
        new NewTournamentDialog(callback).open();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initAppBar();
        setViewContent(createContent());
    }
    
    private static void initAppBar() {
        AppBar appBar = MainLayout.get().getAppBar();
        appBar.removeAllActionItems();
    }
    
    private Component createContent() {
        
        TabSheet tabsheet = new TabSheet();
        tabsheet.setSizeFull();
        tabsheet.add("TOUR-ok", tourTournamentsGrid);
        tabsheet.add("Napi versenyek", dailyTournamentsGrid);
        
        VerticalLayout layout = new VerticalLayout(addButton, tabsheet);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setSizeFull();
        return layout;
    }
    
    @Override
    public void refresh() {
        loadTournaments();
    }
    
    private void loadTournaments() {
        List<TournamentSummary> tournamentSummaries = tournamentService.loadTournamentSummariesList();
        List<TournamentSummary> dailyTournaments = tournamentSummaries.stream().filter(t -> t.type() == Type.DAILY).toList();
        List<TournamentSummary> tourTournaments = tournamentSummaries.stream().filter(t -> t.type() == Type.TOUR).toList();
        dailyTournamentsGrid.setItems(dailyTournaments);
        tourTournamentsGrid.setItems(tourTournaments);
    }

}
