package hu.kits.tennis.infrastructure.ui.views.tournament;

import static java.util.stream.Collectors.toList;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.tournament.DrawMode;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.Tournament.Status;
import hu.kits.tennis.domain.tournament.Tournament.Type;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.domain.utr.Match;
import hu.kits.tennis.domain.utr.MatchInfo;
import hu.kits.tennis.domain.utr.MatchService;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.domain.utr.Players;
import hu.kits.tennis.infrastructure.ui.MainLayout;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;
import hu.kits.tennis.infrastructure.ui.vaadin.SplitViewFrame;
import hu.kits.tennis.infrastructure.ui.vaadin.components.navigation.bar.AppBar;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.views.View;
import hu.kits.tennis.infrastructure.ui.views.utr.MatchesGrid;
import hu.kits.tennis.infrastructure.ui.views.utr.matches.SimpleMatchDialog;

@Route(value = "tournament/:tournamentId", layout = MainLayout.class)
@PageTitle("Tournament")
public class TournamentView extends SplitViewFrame implements View, BeforeEnterObserver  {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final TournamentService tournamentService = Main.resourceFactory.getTournamentService();
    private final MatchService matchService = Main.resourceFactory.getMatchService();
    
    private final Button prevButton = UIUtils.createButton(VaadinIcon.ARROW_LEFT, ButtonVariant.LUMO_PRIMARY);
    private final Button nextButton = UIUtils.createButton(VaadinIcon.ARROW_RIGHT, ButtonVariant.LUMO_PRIMARY);
    
    private final ContestantsTable contestantsTable = new ContestantsTable(this);
    private TournamentBoard mainBoard;
    private TournamentBoard consolationBoard;
    private VerticalLayout tableWithButton;
    private MatchesGrid matchesGrid;
    
    private Tournament tournament;
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initAppBar();
    }
    
    private Component createContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        
        Label title = UIUtils.createH2Label(tournament.name());
        HorizontalLayout header = new HorizontalLayout(prevButton, title, nextButton);
        //Label date = UIUtils.createH3Label(Formatters.formatDateLong(tournament.date()));
        
        Optional<String> prevTournamentId = tournamentService.prevTournamentId(tournament);
        if(prevTournamentId.isPresent()) {
            prevButton.addClickListener(click -> UI.getCurrent().navigate(TournamentView.class, new RouteParameters("tournamentId", prevTournamentId.get())));    
        } else {
            prevButton.setEnabled(false);
        }
        
        Optional<String> nextTournamentId = tournamentService.nextTournamentId(tournament);
        if(nextTournamentId.isPresent()) {
            nextButton.addClickListener(click -> UI.getCurrent().navigate(TournamentView.class, new RouteParameters("tournamentId", nextTournamentId.get())));    
        } else {
            nextButton.setEnabled(false);
        }
        
        if(tournament.type() == Type.NA) {
            matchesGrid = new MatchesGrid();
            matchesGrid.getColumnByKey("date").setVisible(false);
            matchesGrid.getColumnByKey("tournament").setVisible(false);
            matchesGrid.setSizeFull();
            
            matchesGrid.addItemClickListener(e -> openMatchEditor(e.getItem()));

            Button createAddMatchButton = createAddMatchButton();
            
            HorizontalLayout horizontalLayout = new HorizontalLayout(matchesGrid, contestantsTable);
            horizontalLayout.setSizeFull();
            horizontalLayout.setFlexGrow(1, matchesGrid);
            layout.add(header, horizontalLayout, createAddMatchButton);
            
        } else if(tournament.type() == Type.BOARD_AND_CONSOLATION || tournament.type() == Type.SIMPLE_BOARD) {

            Runnable matchChangeCallback = () -> refresh();
            
            mainBoard = new TournamentBoard(tournament, tournament.mainBoard(), matchChangeCallback);
            
            Button fillBoardButton = UIUtils.createButton("Táblára", VaadinIcon.ARROW_LEFT, ButtonVariant.LUMO_PRIMARY);
            fillBoardButton.setVisible(VaadinUtil.isUserLoggedIn());
            fillBoardButton.addClickListener(click -> fillMainBoard());
            
            tableWithButton = new VerticalLayout(contestantsTable, fillBoardButton);
            tableWithButton.setPadding(false);
            tableWithButton.setSizeUndefined();
            tableWithButton.setAlignItems(Alignment.CENTER);
            
            HorizontalLayout horizontalLayout = new HorizontalLayout(mainBoard, tableWithButton);
            horizontalLayout.setWidthFull();
            horizontalLayout.setFlexGrow(1, mainBoard);
            
            layout.add(header, horizontalLayout);
            
            if(tournament.type() == Type.BOARD_AND_CONSOLATION) {
                consolationBoard = new TournamentBoard(tournament, tournament.consolationBoard(), matchChangeCallback);
                layout.add(UIUtils.createH2Label("Vigaszág"), consolationBoard);
            }
        }
        
        layout.setSizeFull();
        
        return layout;
    }
    
    private void openMatchEditor(MatchInfo matchInfo) {
        
        Players players = new Players(tournament.contestants().stream().map(c -> c.player()).collect(toList()));
        Match match = matchService.loadMatch(matchInfo.id());
        
        new SimpleMatchDialog(match, players, tournament.bestOfNSets(), () -> refresh()).open();
    }

    private Button createAddMatchButton() {
        Button button = new Button("Új meccs");
        button.setIcon(new Icon(VaadinIcon.PLUS));

        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Players players = new Players(tournament.contestants().stream().map(c -> c.player()).collect(toList()));
        button.addClickListener(click -> new SimpleMatchDialog(
                Match.createNew(tournament.id(), 1, tournament.matches().size()+1, tournament.date(), null, null),
                players, 
                tournament.bestOfNSets(), 
                () -> refresh()).open());
        
        return button;
    }

    private void fillMainBoard() {
        tournamentService.createMatches(tournament.id(), DrawMode.SIMPLE);
        refresh();
    }

    private static void initAppBar() {
        AppBar appBar = MainLayout.get().getAppBar();
        appBar.removeAllActionItems();
    }
    
    public void refresh() {
        tournament = tournamentService.findTournament(tournament.id()).get();
        loadData();
    }
    
    private void loadData() {
        
        if(tournament.type() == Type.NA) {
            contestantsTable.setPlayers(tournament.simplePlayersLineup());
            matchesGrid.setItems(matchService.loadMatchesOfTournament(tournament.id()));
        } else if(tournament.type() == Type.BOARD_AND_CONSOLATION || tournament.type() == Type.SIMPLE_BOARD) {
            contestantsTable.setPlayers(tournament.playersLineup());
            tableWithButton.setVisible(tournament.status() == Status.DRAFT);
            mainBoard.setBoard(tournament, tournament.mainBoard());
            if(tournament.type() == Type.BOARD_AND_CONSOLATION) {
                consolationBoard.setBoard(tournament, tournament.consolationBoard());    
            }
        }
    }

    private void createUI() {
        setViewContent(createContent());
        
        loadData();
        
        UI.getCurrent().getPage().retrieveExtendedClientDetails(e -> updateVisibleParts(e.getBodyClientWidth()));
        UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> updateVisibleParts(e.getWidth()));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String tournamentId = event.getRouteParameters().get("tournamentId").orElse("");
        Optional<Tournament> tournament = tournamentService.findTournament(tournamentId);
        if(tournament.isEmpty()) {
            KITSNotification.showError("A " + tournamentId + " azonosítójú verseny nem található");
            event.forwardTo(TournamentsView.class);
        } else {
            this.tournament = tournament.get();
            VaadinUtil.logUserAction(logger, "views tournament {}", tournament.get().name());
            createUI();
        }
    }
    
    public void updateContestants(List<Player> players) {
        tournamentService.updateContestants(tournament, players);
    }

    private void updateVisibleParts(int width) {
        boolean mobile = width < VaadinUtil.MOBILE_BREAKPOINT;
        if(tableWithButton != null) {
            tableWithButton.setVisible(!mobile || tournament.status() == Status.DRAFT);    
        }
        contestantsTable.setVisible(!mobile || tournament.status() == Status.DRAFT);
    }
    
}
