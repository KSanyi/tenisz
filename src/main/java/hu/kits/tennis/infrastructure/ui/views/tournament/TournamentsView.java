package hu.kits.tennis.infrastructure.ui.views.tournament;

import static java.util.Comparator.comparing;

import java.util.List;
import java.util.function.Consumer;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.domain.user.Role;
import hu.kits.tennis.infrastructure.ui.MainLayout;
import hu.kits.tennis.infrastructure.ui.util.AllowedRoles;
import hu.kits.tennis.infrastructure.ui.vaadin.SplitViewFrame;
import hu.kits.tennis.infrastructure.ui.vaadin.components.Badge;
import hu.kits.tennis.infrastructure.ui.vaadin.components.FlexBoxLayout;
import hu.kits.tennis.infrastructure.ui.vaadin.components.navigation.bar.AppBar;
import hu.kits.tennis.infrastructure.ui.vaadin.util.FontSize;
import hu.kits.tennis.infrastructure.ui.vaadin.util.LineHeight;
import hu.kits.tennis.infrastructure.ui.vaadin.util.LumoStyles;
import hu.kits.tennis.infrastructure.ui.vaadin.util.TextColor;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.Overflow;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.PointerEvents;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.TextOverflow;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo.BadgeColor;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo.BadgeShape;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo.BadgeSize;
import hu.kits.tennis.infrastructure.ui.vaadin.util.layout.size.Right;
import hu.kits.tennis.infrastructure.ui.vaadin.util.layout.size.Vertical;
import hu.kits.tennis.infrastructure.ui.views.View;

@Route(value = "tournaments", layout = MainLayout.class)
@PageTitle("Versenyek")
@AllowedRoles({Role.ADMIN})
public class TournamentsView extends SplitViewFrame implements View {

    private final TournamentService tournamentService = Main.resourceFactory.getTournamentService();

    private final Button addButton = UIUtils.createPrimaryButton("Új verseny", VaadinIcon.PLUS);
    
    private final TournamentsGrid tournamentsGrid = new TournamentsGrid();
    
    public TournamentsView() {
        addButton.addClickListener(click -> openNewTournamentDialog());
        loadTournaments();
    }
    
    private void openNewTournamentDialog() {
        Consumer<Tournament> callback = t -> loadTournaments();
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
        VerticalLayout layout = new VerticalLayout(addButton, tournamentsGrid);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setSizeFull();
        return layout;
    }
    
    public void refresh() {
        loadTournaments();
    }
    
    private void loadTournaments() {
        
        List<Tournament> tournaments = tournamentService.loadAllTournaments();
        tournamentsGrid.setItems(tournaments);
    }

}

class TournamentsGrid extends Grid<Tournament> {
    
    private static final int MOBILE_BREAKPOINT = 800;
    
    public TournamentsGrid() {
        
        addComponentColumn(TournamentsMobileTemplate::new)
            .setVisible(false);
        
        addColumn(Tournament::name)
            .setHeader("Név")
            .setSortable(true)
            .setFlexGrow(4)
            .setKey("name");
        
        addColumn(tournament -> tournament.organizer().name)
            .setHeader("Szervező")
            .setSortable(true)
            .setFlexGrow(2);
    
        addColumn(new LocalDateRenderer<>(Tournament::date, Formatters.DATE_FORMAT))
            .setHeader("Dátum")
            .setSortable(true)
            .setFlexGrow(4)
            .setComparator(comparing(Tournament::date))
            .setKey("date");
        
        addColumn(t -> t.contestants().size())
            .setHeader("Indulók")
            .setSortable(true)
            .setFlexGrow(1)
            .setTextAlign(ColumnTextAlign.CENTER);
        /*
        addColumn(t -> t.matches().size())
            .setHeader("Lejátszott meccsek")
            .setSortable(true)
            .setFlexGrow(1);
            */
        
        addComponentColumn(t -> new Badge(t.status().name(), BadgeColor.SUCCESS, BadgeSize.M, BadgeShape.PILL))
            .setHeader("Státusz")
            .setSortable(true)
            .setComparator(comparing(Tournament::status))
            .setFlexGrow(1)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setKey("state");
        
        setHeightFull();
        
        UI.getCurrent().getPage().retrieveExtendedClientDetails(e -> updateVisibleColumns(e.getBodyClientWidth()));
        UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> updateVisibleColumns(e.getWidth()));
        
        this.setMultiSort(true);
        
        sort(GridSortOrder.asc(getColumnByKey("state"))
                .thenDesc(getColumnByKey("date"))
                .build());
        
        addSelectionListener(this::rowSelected);
    }
    
    private void updateVisibleColumns(int width) {
        boolean mobile = width < MOBILE_BREAKPOINT;
        List<Grid.Column<Tournament>> columns = getColumns();

        // "Mobile" column
        columns.get(0).setVisible(mobile);

        // "Desktop" columns
        for (int i = 1; i < columns.size(); i++) {
            columns.get(i).setVisible(!mobile);
        }
    }
    
    private void rowSelected(SelectionEvent<Grid<Tournament>,Tournament> event) {
        if(event.getFirstSelectedItem().isPresent()) {
            Tournament tournament = event.getFirstSelectedItem().get();
            getUI().ifPresent(ui -> ui.navigate(TournamentView.class, new RouteParameters("tournamentId", tournament.id())));
        }
    }
    
    static class TournamentsMobileTemplate extends FlexBoxLayout {

        private Tournament tournament;

        public TournamentsMobileTemplate(Tournament tournament) {
            this.tournament = tournament;

            UIUtils.setLineHeight(LineHeight.M, this);
            UIUtils.setPointerEvents(PointerEvents.NONE, this);

            setPadding(Vertical.S);
            setSpacing(Right.L);

            FlexBoxLayout name = getName();
            Label date = getDate();

            FlexBoxLayout column = new FlexBoxLayout(name, date);
            column.setFlexDirection(FlexDirection.COLUMN);
            column.setOverflow(Overflow.HIDDEN);

            add(column);
        }

        private FlexBoxLayout getName() {
            Label owner = UIUtils.createLabel(FontSize.M, TextColor.BODY, tournament.name());
            UIUtils.setOverflow(Overflow.HIDDEN, owner);
            UIUtils.setTextOverflow(TextOverflow.ELLIPSIS, owner);

            Badge badge = new Badge(tournament.contestants().size() + " induló", BadgeColor.SUCCESS, BadgeSize.M, BadgeShape.PILL);
            badge.setWidth("100px");
            
            FlexBoxLayout wrapper = new FlexBoxLayout(owner, badge);
            wrapper.setAlignItems(Alignment.END);
            wrapper.setFlexGrow(1, owner);
            wrapper.setFlexShrink("0", badge);
            wrapper.setSpacing(Right.M);
            return wrapper;
        }

        private Label getDate() {
            Label account = UIUtils.createLabel(FontSize.S, TextColor.SECONDARY,  Formatters.formatDateLong(tournament.date()));
            account.addClassNames(LumoStyles.Margin.Bottom.S);
            UIUtils.setOverflow(Overflow.HIDDEN, account);
            UIUtils.setTextOverflow(TextOverflow.ELLIPSIS, account);
            return account;
        }

    }
    
}
