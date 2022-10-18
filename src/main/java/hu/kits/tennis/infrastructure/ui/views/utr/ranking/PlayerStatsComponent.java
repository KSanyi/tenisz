package hu.kits.tennis.infrastructure.ui.views.utr.ranking;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.domain.utr.PlayerStats;
import hu.kits.tennis.domain.utr.UTRService;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.views.utr.MatchesGrid;

class PlayerStatsComponent extends VerticalLayout {

    private final UTRService utrService;
    
    private final Label nameLabel = UIUtils.createH2Label(null);
    private final Label matchStatsLabel = new Label(null);
    private final Label gameStatsLabel = new Label(null);
    
    private final MatchesGrid matchesGrid;
    
    public PlayerStatsComponent() {
        utrService = Main.resourceFactory.getUTRService();
        matchesGrid = new MatchesGrid();
        matchesGrid.setSizeFull();
        add(nameLabel, matchStatsLabel, gameStatsLabel, matchesGrid);
        
        setPadding(false);
        setSpacing(false);
        setSizeFull();
    }
    
    void setPlayer(Player player) {
        PlayerStats playerStats = utrService.loadPlayerStats(player);
        setPlayerStats(playerStats);
    }

    private void setPlayerStats(PlayerStats playerStats) {
        
        nameLabel.setText(playerStats.player().name() + " UTR: " + playerStats.utrDetails().utr());
        
        matchStatsLabel.setText(String.format("%d mérkőzés: %d győzelem (%s) %d vereség (%s)", playerStats.numberOfMatches(),
                playerStats.numberOfWins(), Formatters.formatPercent(playerStats.winPercentage()),
                playerStats.numberOfLosses(), Formatters.formatPercent(playerStats.lossPercentage())));
        
        gameStatsLabel.setText(String.format("%d game: %d nyert (%s) %d elvesztett (%s)", playerStats.numberOfGames(),
                playerStats.numberOfGamesWon(), Formatters.formatPercent(playerStats.gamesWinPercentage()),
                playerStats.numberOfGamesLost(), Formatters.formatPercent(playerStats.gamesLossPercentage())));
        
        matchesGrid.setItems(playerStats.matches());
        matchesGrid.setPlayer2UtrColumnVisible(false);
        matchesGrid.setBestWorstAndUTRRelevantMatches(playerStats.bestUTRMatch().orElse(null), playerStats.worstUTRMatch().orElse(null), playerStats.utrDetails().relevantMatches());
    }
    
}
