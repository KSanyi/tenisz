package hu.kits.tennis.infrastructure.ui.views.utr.ranking;

import static hu.kits.tennis.common.StringUtil.cleanNameString;

import java.util.Comparator;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.ListDataProvider;

import hu.kits.tennis.domain.utr.PlayerWithUTR;
import hu.kits.tennis.domain.utr.UTR;
import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

class UTRRankingGrid extends Grid<PlayerWithUTR> {
    
    private ListDataProvider<PlayerWithUTR> dataProvider;
    
    private Column<PlayerWithUTR> linkColumn;
    
    UTRRankingGrid() {
        
        addColumn(playerWithUTR -> playerWithUTR.rank())
            .setHeader("Rank")
            .setAutoWidth(true)
            .setSortable(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(0);
        
        addColumn(playerWithUTR -> playerWithUTR.player().name())
            .setHeader("Név")
            .setSortable(true)
            .setFlexGrow(1);
        
        addComponentColumn(UTRRankingGrid::createUTRComponent)
            .setHeader("UTR")
            .setComparator(Comparator.comparing(PlayerWithUTR::utr))
            .setWidth("110px")
            .setFlexGrow(0);
        
        addColumn(playerWithUTR -> playerWithUTR.numberOfMatches())
            .setHeader("Match")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setSortable(true)
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        addColumn(playerWithUTR -> playerWithUTR.numberOfWins())
            .setHeader("Wins")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setSortable(true)
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        addComponentColumn(UTRRankingGrid::createTrophiesComponent)
            .setHeader("Trophies")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setComparator(Comparator.comparing(PlayerWithUTR::numberOfTrophies))
            .setWidth("110px")
            .setFlexGrow(0);
        
        linkColumn = addComponentColumn(UTRRankingGrid::createPlayerStatsLink)
            .setHeader("")
            .setFlexGrow(0);
        
        setWidthFull();
        setHeightFull();
        setMinWidth("690px");
        
        UI.getCurrent().getPage().retrieveExtendedClientDetails(e -> updateVisibleColumns(e.getBodyClientWidth()));
        UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> updateVisibleColumns(e.getWidth()));
    }
    
    private static Component createUTRComponent(PlayerWithUTR playerWithUTR) {
        Span span = new Span(new Label(playerWithUTR.utr().toString()));
        span.add(createUTRChangeSpan(playerWithUTR.utrChange()));
        return span;
    }
    
    static Span createUTRChangeSpan(UTR utrChange) {
        if(utrChange.isDefinded() && utrChange.value().doubleValue() != 0) {
            double diff = utrChange.value().doubleValue();
            if(Math.abs(diff) >= 0.05) {
                if(diff > 0) {
                    return createUTRChangeSpan(utrChange, "arrow-up", "var(--lumo-success-text-color)");
                } else {
                    return createUTRChangeSpan(utrChange, "arrow-down", "var(--lumo-error-text-color)");
                }  
            }
        }
        return new Span();
    }
    
    private static Span createUTRChangeSpan(UTR utrChange, String arrow, String color) {
        Icon icon = new Icon("lumo", arrow);
        Label label = new Label(utrChange.toString());
        label.getStyle().set("font-size", "11px");
        Span span = new Span(icon, label);
        span.getStyle().set("color", color);
        UIUtils.setTooltip("UTR változás az elmúlt hét napban", span);
        return span;
    }
    
    static Component createPlayerStatsLink(PlayerWithUTR playerWithUTR) {
        Anchor anchor = createAnchor(playerWithUTR.player().id());
        UIUtils.setTooltip("Megnyitás új böngészőablakban", anchor);
        return anchor;
    }
    
    private static Anchor createAnchor(int playerId) {
        return new Anchor("player-stats/" + playerId, "Adatlap", AnchorTarget.BLANK);
    }
    
    static Component createTrophiesComponent(PlayerWithUTR playerWithUTR) {
        
        Span span = new Span();
        for(int i=0;i<playerWithUTR.numberOfTrophies();i++) {
            Icon icon = VaadinIcon.TROPHY.create();
            icon.setColor("Goldenrod");
            icon.setSize("15px");
            span.add(icon);
        }
        
        return span;
    }
    
    void setUTRRankingList(List<PlayerWithUTR> entries) {
        dataProvider = new ListDataProvider<>(entries);
        setItems(dataProvider);
    }
    
    void filter(String playerNamePart) {
        dataProvider.clearFilters();
        dataProvider.addFilter(playerWithUtr -> cleanNameString(playerWithUtr.player().name()).contains(cleanNameString(playerNamePart)));
    }
    
    private void updateVisibleColumns(int width) {
        boolean smallScreen = width < VaadinUtil.SMALL_SCREEN_BREAKPOINT;

        linkColumn.setVisible(smallScreen);
    }

}

