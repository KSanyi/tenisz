package hu.kits.tennis.infrastructure.ui.views.utr.playerstats;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.utr.PlayerStats;
import hu.kits.tennis.domain.utr.UTRService;
import hu.kits.tennis.domain.utr.UTRWithDate;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.views.utr.MatchesGrid;

public class PlayerStatsComponent extends VerticalLayout {

    private final UTRService utrService = Main.applicationContext.getUTRService();
    
    private final Label nameLabel = UIUtils.createH2Label("");
    private final Label utrHighLabel = new Label();
    private final Label matchStatsLabel = new Label();
    private final Label gameStatsLabel = new Label();
    private final Div utrHistoryChartHolder = new Div();
    
    private final TournamentMatchesComponent tournamentMatchesComponent = new TournamentMatchesComponent();
    private final MatchesGrid matchesGrid = new MatchesGrid();
    
    public PlayerStatsComponent() {
        matchesGrid.setSizeFull();
        tournamentMatchesComponent.setMaxHeight("1px"); // I dont know why it is working but it is needed here
        
        HorizontalLayout nameRow = new HorizontalLayout(nameLabel, utrHighLabel);
        nameRow.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        
        VerticalLayout leftColumn = new VerticalLayout(nameRow, matchStatsLabel, gameStatsLabel);
        leftColumn.setSpacing(false);
        leftColumn.setPadding(false);
        
        HorizontalLayout headerRow = new HorizontalLayout(leftColumn, utrHistoryChartHolder);
        TabSheet matchesTab = new TabSheet();
        matchesTab.setSizeFull();
        matchesTab.add("Versenyek", tournamentMatchesComponent);
        matchesTab.add("Meccsek", matchesGrid);
        add(headerRow, matchesTab);
        
        setPadding(false);
        setSpacing(false);
        setSizeFull();
    }
    
    public void setPlayer(Player player) {
        PlayerStats playerStats = utrService.loadPlayerStats(player);
        setPlayerStats(playerStats);
    }

    private void setPlayerStats(PlayerStats playerStats) {
        
        nameLabel.setText(playerStats.player().name() + " UTR: " + playerStats.utrDetails().utr());
        
        if(playerStats.utrHigh().isPresent()) {
            UTRWithDate utrHigh = playerStats.utrHigh().get();
            utrHighLabel.setText(String.format("UTR csúcs: %s (%s)", utrHigh.utr(), Formatters.formatDate(utrHigh.date())));
        } else {
            utrHighLabel.setText("");
        }
        
        matchStatsLabel.setText(String.format("%d mérkőzés: %d győzelem (%s) %d vereség (%s)", playerStats.numberOfMatches(),
                playerStats.numberOfWins(), Formatters.formatPercent(playerStats.winPercentage()),
                playerStats.numberOfLosses(), Formatters.formatPercent(playerStats.lossPercentage())));
        
        gameStatsLabel.setText(String.format("%d game: %d nyert (%s) %d elvesztett (%s)", playerStats.numberOfGames(),
                playerStats.numberOfGamesWon(), Formatters.formatPercent(playerStats.gamesWinPercentage()),
                playerStats.numberOfGamesLost(), Formatters.formatPercent(playerStats.gamesLossPercentage())));
        
        tournamentMatchesComponent.setMatches(playerStats.matches());
        matchesGrid.setItems(playerStats.matches());
        
        utrHistoryChartHolder.removeAll();
        UTRHistoryChart chart = new UTRHistoryChart(playerStats.utrHistory());
        chart.setHeight("110px");
        chart.setWidth("400px");
        utrHistoryChartHolder.add(chart);
    }
    
}
