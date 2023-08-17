package hu.kits.tennis.infrastructure.ui.views.ktr.playerstats;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.domain.ktr.PlayerStats;
import hu.kits.tennis.domain.ktr.KTRService;
import hu.kits.tennis.domain.ktr.KTRHistory.KTRHistoryEntry;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.views.ktr.MatchesGrid;

public class PlayerStatsComponent extends VerticalLayout {

    private final KTRService ktrService = Main.applicationContext.getKTRService();
    
    private final Label nameLabel = UIUtils.createH2Label("");
    private final Label ktrHighLabel = new Label();
    private final Label matchStatsLabel = new Label();
    private final Label gameStatsLabel = new Label();
    private final Div ktrHistoryChartHolder = new Div();
    
    private final TournamentMatchesComponent tournamentMatchesComponent = new TournamentMatchesComponent();
    private final MatchesGrid matchesGrid = new MatchesGrid();
    
    public PlayerStatsComponent() {
        matchesGrid.setSizeFull();
        tournamentMatchesComponent.setMaxHeight("1px"); // I dont know why it is working but it is needed here
        
        HorizontalLayout nameRow = new HorizontalLayout(nameLabel, ktrHighLabel);
        nameRow.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        
        VerticalLayout leftColumn = new VerticalLayout(nameRow, matchStatsLabel, gameStatsLabel);
        leftColumn.setSpacing(false);
        leftColumn.setPadding(false);
        
        HorizontalLayout headerRow = new HorizontalLayout(leftColumn, ktrHistoryChartHolder);
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
        PlayerStats playerStats = ktrService.loadPlayerStats(player);
        setPlayerStats(playerStats);
    }

    private void setPlayerStats(PlayerStats playerStats) {
        
        nameLabel.setText(playerStats.player().name() + " KTR: " + playerStats.ktrDetails().ktr());
        
        KTRHistoryEntry ktrHigh = playerStats.ktrHigh();
        ktrHighLabel.setText(String.format("KTR csúcs: %s (%s)", ktrHigh.ktr(), Formatters.formatDate(ktrHigh.date())));
        
        matchStatsLabel.setText(String.format("%d mérkőzés: %d győzelem (%s) %d vereség (%s)", playerStats.numberOfMatches(),
                playerStats.numberOfWins(), Formatters.formatPercent(playerStats.winPercentage() / 100),
                playerStats.numberOfLosses(), Formatters.formatPercent(playerStats.lossPercentage() / 100)));
        
        gameStatsLabel.setText(String.format("%d game: %d nyert (%s) %d elvesztett (%s)", playerStats.numberOfGames(),
                playerStats.numberOfGamesWon(), Formatters.formatPercent(playerStats.gamesWinPercentage() / 100),
                playerStats.numberOfGamesLost(), Formatters.formatPercent(playerStats.gamesLossPercentage() / 100)));
        
        tournamentMatchesComponent.setMatches(playerStats.matches());
        matchesGrid.setItems(playerStats.matches());
        matchesGrid.hidePlayer2KTRColumn();
        matchesGrid.setBestWorstAndKTRRelevantMatches(playerStats.bestKTRMatch(), playerStats.worstKTRMatch(), playerStats.ktrDetails().relevantMatchIds());
        
        ktrHistoryChartHolder.removeAll();
        KTRHistoryChart chart = new KTRHistoryChart(playerStats.ktrHistory());
        chart.setHeight("110px");
        chart.setWidth("400px");
        ktrHistoryChartHolder.add(chart);
    }
    
}
