package hu.kits.tennis.infrastructure.ui.views.tournament;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.Pair;
import hu.kits.tennis.domain.tournament.DrawMode;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.Tournament.Status;
import hu.kits.tennis.domain.tournament.Tournament.Type;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.domain.utr.Match;
import hu.kits.tennis.domain.utr.MatchResult;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.infrastructure.ui.MainLayout;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.vaadin.SplitViewFrame;
import hu.kits.tennis.infrastructure.ui.vaadin.components.navigation.bar.AppBar;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.views.View;

@Route(value = "tournament/:tournamentId", layout = MainLayout.class)
@PageTitle("Tournament")
public class TournamentView extends SplitViewFrame implements View, BeforeEnterObserver  {

    private final TournamentService tournamentService = Main.resourceFactory.getTournamentService();
    
    private final ContestantsTable contestantsTable = new ContestantsTable(this);
    private TournamentBoard mainBoard;
    private TournamentBoard consolationBoard;
    private VerticalLayout tableWithButton;
    
    private Tournament tournament;
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initAppBar();
        createUI();
    }
    
    private Component createContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        
        Label title = UIUtils.createH2Label(tournament.name());
        //Label date = UIUtils.createH3Label(Formatters.formatDateLong(tournament.date()));
        
        contestantsTable.setPlayers(tournament.playersLineup());
        
        Consumer<Pair<Match, MatchResult>> matchResultSetCallback = p -> {
            tournamentService.setTournamentMatchResult(p.getFirst(), p.getSecond());
            refresh();
        };
        
        mainBoard = new TournamentBoard(tournament, tournament.mainBoard(), matchResultSetCallback);
        
        Button fillBoardButton = UIUtils.createButton("Táblára", VaadinIcon.ARROW_LEFT, ButtonVariant.LUMO_PRIMARY);
        fillBoardButton.addClickListener(click -> fillBoard());
        fillBoardButton.setVisible(tournament.status() == Status.DRAFT);
        
        tableWithButton = new VerticalLayout(contestantsTable, fillBoardButton);
        tableWithButton.setPadding(false);
        tableWithButton.setSizeUndefined();
        tableWithButton.setAlignItems(Alignment.CENTER);
        tableWithButton.setVisible(tournament.status() == Status.DRAFT);
        
        HorizontalLayout horizontalLayout = new HorizontalLayout(mainBoard, tableWithButton);
        horizontalLayout.setWidthFull();
        horizontalLayout.setFlexGrow(1, mainBoard);
        
        layout.add(title, horizontalLayout);
        
        if(tournament.type() == Type.BOARD_AND_CONSOLATION) {
            consolationBoard = new TournamentBoard(tournament, tournament.consolationBoard(), matchResultSetCallback);
            layout.add(UIUtils.createH2Label("Vigaszág"), consolationBoard);
        }
        
        return layout;
    }

    private void fillBoard() {
        tournamentService.createMatches(tournament.id(), DrawMode.SIMPLE);
        refresh();
    }

    private static void initAppBar() {
        AppBar appBar = MainLayout.get().getAppBar();
        appBar.removeAllActionItems();
    }
    
    public void refresh() {
        tournament = tournamentService.findTournament(tournament.id()).get();
        createUI();
    }
    
    private void createUI() {
        setViewContent(createContent());
        
        //UI.getCurrent().getPage().retrieveExtendedClientDetails(e -> updateVisibleParts(e.getBodyClientWidth()));
        //UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> updateVisibleParts(e.getWidth()));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String tournamentId = event.getRouteParameters().get("tournamentId").orElse("");
        Optional<Tournament> tournament = tournamentService.findTournament(tournamentId);
        if(tournament.isEmpty()) {
            KITSNotification.showError("A " + tournamentId + " azonosítójú verseny nem található");
        } else {
            this.tournament = tournament.get();
        }
    }
    
    public void updateContestants(List<Player> players) {
        tournamentService.updateContestants(tournament, players);
    }

    /*
    private void updateVisibleParts(int width) {
        boolean mobile = width < MOBILE_BREAKPOINT;
        tableWithButton.setVisible(!mobile || tournament.status() == Status.DRAFT);
    }
    */
    
}
