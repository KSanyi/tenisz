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

import hu.kits.tennis.Main;
import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.match.MatchInfo;
import hu.kits.tennis.domain.match.MatchService;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.Players;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.domain.tournament.TournamentParams.Status;
import hu.kits.tennis.domain.tournament.TournamentParams.Structure;
import hu.kits.tennis.domain.utr.UTRService;
import hu.kits.tennis.infrastructure.ui.MainLayout;
import hu.kits.tennis.infrastructure.ui.component.ConfirmationDialog;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;
import hu.kits.tennis.infrastructure.ui.vaadin.SplitViewFrame;
import hu.kits.tennis.infrastructure.ui.vaadin.components.navigation.bar.AppBar;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.views.View;
import hu.kits.tennis.infrastructure.ui.views.tournaments.TournamentsView;
import hu.kits.tennis.infrastructure.ui.views.utr.MatchesGrid;
import hu.kits.tennis.infrastructure.ui.views.utr.matches.SimpleMatchDialog;

@Route(value = "tournament/:tournamentId", layout = MainLayout.class)
@PageTitle("Tournament")
public class TournamentView extends SplitViewFrame implements View, BeforeEnterObserver  {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final UTRService utrService = Main.applicationContext.getUTRService();
    private final TournamentService tournamentService = Main.applicationContext.getTournamentService();
    private final MatchService matchService = Main.applicationContext.getMatchService();
    
    private final Button deleteButton = UIUtils.createButton("Verseny törlése", ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
    
    private final ContestantsTable contestantsTable = new ContestantsTable(this);
    private TournamentBoardComponent mainBoard;
    private TournamentBoardComponent consolationBoard;
    private VerticalLayout tableWithButton;
    private MatchesGrid matchesGrid;
    private final Label matchCounter = new Label("");
    
    private Tournament tournament;
    
    public TournamentView() {
        deleteButton.addClickListener(click -> deleteTournament());
    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initAppBar();
    }
    
    private Component createContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        
        Label title = UIUtils.createH2Label(tournament.params().name() + " " + Formatters.formatDateLong(tournament.params().date()));
        HorizontalLayout header = new HorizontalLayout(title);
        
        if(tournament.params().structure() == Structure.NA) {
            matchesGrid = new MatchesGrid();
            matchesGrid.getColumnByKey("date").setVisible(false);
            matchesGrid.getColumnByKey("tournament").setVisible(false);
            matchesGrid.setSizeFull();
            
            matchesGrid.addItemClickListener(e -> {
                if(e.getClickCount() > 1) {
                    openMatchEditor(e.getItem());
                }
            });

            Button addMatchButton = createAddMatchButton();
            Button recalculateButton = createRecalculateButton();
            
            VerticalLayout leftColumn = new VerticalLayout(matchesGrid, new HorizontalLayout(matchCounter, addMatchButton, recalculateButton));
            leftColumn.setPadding(false);
            
            HorizontalLayout horizontalLayout = new HorizontalLayout(leftColumn, contestantsTable);
            horizontalLayout.setSizeFull();
            horizontalLayout.setFlexGrow(1, leftColumn);
            layout.add(header, horizontalLayout, deleteButton);
            layout.setHorizontalComponentAlignment(Alignment.END, deleteButton);
            
        } else if(tournament.params().structure() == Structure.BOARD_AND_CONSOLATION || tournament.params().structure() == Structure.SIMPLE_BOARD) {

            Runnable matchChangeCallback = () -> refresh();
            
            mainBoard = new TournamentBoardComponent(tournament, tournament.mainBoard(), matchChangeCallback);
            
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
            
            layout.add(header, horizontalLayout, deleteButton);
            layout.setHorizontalComponentAlignment(Alignment.END, deleteButton);
            
            if(tournament.params().structure() == Structure.BOARD_AND_CONSOLATION) {
                consolationBoard = new TournamentBoardComponent(tournament, tournament.consolationBoard(), matchChangeCallback);
                layout.add(UIUtils.createH2Label("Vigaszág"), consolationBoard);
            }
        }
        
        layout.setSizeFull();
        
        return layout;
    }
    
    private Button createRecalculateButton() {
        Button button = new Button("UTR számolás");
        button.setIcon(new Icon(VaadinIcon.AUTOMATION));

        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.addClickListener(click -> recalculateUTRs());
        return button;
    }
    
    private void recalculateUTRs() {
        utrService.recalculateAllUTRs();
        refresh();
        KITSNotification.showInfo("UTR kiszámolva");
    }
    
    private void openMatchEditor(MatchInfo matchInfo) {
        
        Players players = new Players(tournament.contestants().stream().map(c -> c.player()).collect(toList()));
        Match match = matchService.loadMatch(matchInfo.id());
        
        new SimpleMatchDialog(match, players, tournament.params().bestOfNSets(), () -> refresh()).open();
    }

    private Button createAddMatchButton() {
        Button button = new Button("Meccs");
        button.setIcon(new Icon(VaadinIcon.PLUS));

        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.addClickListener(click -> {
            tournament = tournamentService.findTournament(tournament.id()).get();
            openDialog();
        });
        
        return button;
    }
    
    private void openDialog() {
        
        Players players = new Players(tournament.contestants().stream().map(c -> c.player()).collect(toList()));
        Runnable callback = () -> {
            refresh();
            openDialog();
            KITSNotification.showInfo("Meccs elmentve");
        };
        
        new SimpleMatchDialog(
            Match.createNew(tournament.id(), 1, tournament.matches().size()+1, tournament.params().date(), null, null),
            players, 
            tournament.params().bestOfNSets(), 
            callback).open();
    }

    private void fillMainBoard() {
        tournamentService.createMatches(tournament.id());
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
        
        if(tournament.params().structure() == Structure.NA) {
            contestantsTable.setPlayers(tournament.simplePlayersLineup());
            List<MatchInfo> matches = matchService.loadMatchesOfTournament(tournament.id());
            matchesGrid.setItems(matches);
            matchCounter.setText(matches.size() + " meccs");
        } else if(tournament.params().structure() == Structure.BOARD_AND_CONSOLATION || tournament.params().structure() == Structure.SIMPLE_BOARD) {
            contestantsTable.setPlayers(tournament.playersLineup());
            tableWithButton.setVisible(tournament.status() == Status.DRAFT);
            mainBoard.setBoard(tournament, tournament.mainBoard());
            if(tournament.params().structure() == Structure.BOARD_AND_CONSOLATION) {
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
            VaadinUtil.logUserAction(logger, "views tournament {}", tournament.get().params().name());
            createUI();
        }
    }
    
    public void updateContestants(List<Player> players) {
        tournamentService.updateContestants(tournament, players);
    }

    private void updateVisibleParts(int width) {
        boolean mobile = width < VaadinUtil.MOBILE_BREAKPOINT;
        if(tableWithButton != null) {
            tableWithButton.setVisible(!mobile && tournament.status() == Status.DRAFT);    
        }
        contestantsTable.setVisible(!mobile && tournament.status() == Status.DRAFT);
        deleteButton.setVisible(!mobile);
    }
    
    private void deleteTournament() {
        new ConfirmationDialog("Biztosan törölni akarod a versenyt?", () -> {
            tournamentService.deleteTournament(tournament);
            UI.getCurrent().navigate(TournamentsView.class);
            KITSNotification.showInfo("Verseny törölve");
        }).open();
    }
    
}
