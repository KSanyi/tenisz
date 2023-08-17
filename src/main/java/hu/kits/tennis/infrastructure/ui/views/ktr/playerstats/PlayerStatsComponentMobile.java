package hu.kits.tennis.infrastructure.ui.views.ktr.playerstats;

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
import hu.kits.tennis.domain.ktr.PlayerStats;
import hu.kits.tennis.domain.ktr.KTRHistory;
import hu.kits.tennis.domain.ktr.KTRService;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.infrastructure.ui.vaadin.components.Badge;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo.BadgeColor;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo.BadgeShape;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo.BadgeSize;
import hu.kits.tennis.infrastructure.ui.views.ktr.MatchesGridMobile;

class PlayerStatsComponentMobile extends VerticalLayout {

    private final KTRService ktrService = Main.applicationContext.getKTRService();
    
    private final Label nameLabel = UIUtils.createH3Label("");
    private final Badge ktrBadge = new Badge("", BadgeColor.CONTRAST_PRIMARY, BadgeSize.M, BadgeShape.PILL);
    private final Div ktrHistoryChartHolder = new Div();
    
    private final MatchesGridMobile matchesGrid = new MatchesGridMobile();
    
    public PlayerStatsComponentMobile() {
        matchesGrid.setSizeFull();

        setPadding(false);
        ktrHistoryChartHolder.addClassNames(Padding.NONE, Margin.NONE);
        
        Span headerRow = new Span(nameLabel, ktrBadge);
        headerRow.addClassNames(Padding.XSMALL, Display.FLEX, FlexDirection.ROW, Gap.SMALL, Padding.Bottom.NONE);
        add(headerRow, ktrHistoryChartHolder, matchesGrid);
        ktrHistoryChartHolder.setWidthFull();
        
        setSizeFull();
    }
    
    void setPlayer(Player player) {
        PlayerStats playerStats = ktrService.loadPlayerStats(player);
        setPlayerStats(playerStats);
    }

    private void setPlayerStats(PlayerStats playerStats) {
        
        nameLabel.setText(playerStats.player().name());
        ktrBadge.setText("KTR: " + playerStats.ktrDetails().ktr().toString());
        
        matchesGrid.setItems(playerStats.matches());
        matchesGrid.setBestWorstAndKTRRelevantMatches(playerStats.bestKTRMatch(), playerStats.worstKTRMatch(), playerStats.ktrDetails().relevantMatchIds());
        
        setKTRHistoryChart(playerStats.ktrHistory());
    }
    
    private void setKTRHistoryChart(KTRHistory ktrHistory) {
        ktrHistoryChartHolder.removeAll();
        KTRHistoryChart chart = new KTRHistoryChart(ktrHistory);
        chart.setHeight("100px");
        chart.setWidthFull();
        ktrHistoryChartHolder.add(chart);
    }
    
}
