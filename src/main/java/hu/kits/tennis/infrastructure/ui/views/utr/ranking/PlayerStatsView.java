package hu.kits.tennis.infrastructure.ui.views.utr.ranking;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.utr.MatchService;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.domain.utr.PlayerStats;
import hu.kits.tennis.infrastructure.ui.views.utr.MatchesGrid;

class PlayerStatsView extends VerticalLayout {

    private final MatchService matchService;
    
    private final MatchesGrid matchesGrid;
    
    public PlayerStatsView() {
        matchService = Main.resourceFactory.getMatchService();
        matchesGrid = new MatchesGrid();
        add(matchesGrid);
        
        setPadding(false);
    }
    
    void setPlayer(Player player) {
        PlayerStats playerStats = matchService.loadPlayerStats(player);
        setPlayerStats(playerStats);
    }

    private void setPlayerStats(PlayerStats playerStats) {
        matchesGrid.setItems(playerStats.matches());
    }
    
}
