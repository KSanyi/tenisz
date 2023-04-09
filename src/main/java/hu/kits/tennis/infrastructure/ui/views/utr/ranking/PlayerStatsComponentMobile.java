package hu.kits.tennis.infrastructure.ui.views.utr.ranking;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.utr.PlayerStats;
import hu.kits.tennis.domain.utr.UTRHistory;
import hu.kits.tennis.domain.utr.UTRService;
import hu.kits.tennis.infrastructure.ui.vaadin.components.Badge;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo.BadgeColor;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo.BadgeShape;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo.BadgeSize;
import hu.kits.tennis.infrastructure.ui.views.utr.MatchesGridMobile;

class PlayerStatsComponentMobile extends VerticalLayout {

    private final UTRService utrService = Main.applicationContext.getUTRService();
    
    private final Label nameLabel = UIUtils.createH3Label("");
    private final Badge utrBadge = new Badge("", BadgeColor.CONTRAST_PRIMARY, BadgeSize.M, BadgeShape.PILL);
    private final Div utrHistoryChartHolder = new Div();
    
    private final MatchesGridMobile matchesGrid = new MatchesGridMobile();
    
    public PlayerStatsComponentMobile() {
        matchesGrid.setSizeFull();

        setPadding(false);
        utrHistoryChartHolder.addClassNames(Padding.NONE, Margin.NONE);
        
        Span headerRow = new Span(nameLabel, utrBadge);
        headerRow.addClassNames(Padding.XSMALL, Display.FLEX, FlexDirection.ROW, Gap.SMALL, Padding.Bottom.NONE);
        add(headerRow, utrHistoryChartHolder, matchesGrid);
        utrHistoryChartHolder.setWidthFull();
        
        setSizeFull();
    }
    
    void setPlayer(Player player) {
        PlayerStats playerStats = utrService.loadPlayerStats(player);
        setPlayerStats(playerStats);
    }

    private void setPlayerStats(PlayerStats playerStats) {
        
        nameLabel.setText(playerStats.player().name());
        utrBadge.setText("UTR: " + playerStats.utrDetails().utr().toString());
        
        matchesGrid.setItems(playerStats.matches());
        matchesGrid.setBestWorstAndUTRRelevantMatches(playerStats.bestUTRMatch().orElse(null), playerStats.worstUTRMatch().orElse(null), playerStats.utrDetails().relevantMatches());
        
        setUTRHistoryChart(playerStats.utrHistory());
    }
    
    private void setUTRHistoryChart(UTRHistory utrHistory) {
        utrHistoryChartHolder.removeAll();
        UTRHistoryChart chart = new UTRHistoryChart(utrHistory);
        chart.setHeight("100px");
        chart.setWidthFull();
        utrHistoryChartHolder.add(chart);
    }
    
}
