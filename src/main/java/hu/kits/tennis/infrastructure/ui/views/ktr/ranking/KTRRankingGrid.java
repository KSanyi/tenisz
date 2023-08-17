package hu.kits.tennis.infrastructure.ui.views.ktr.ranking;

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

import hu.kits.tennis.common.StringUtil;
import hu.kits.tennis.domain.ktr.KTR;
import hu.kits.tennis.domain.ktr.PlayerWithKTR;
import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

class KTRRankingGrid extends Grid<PlayerWithKTR> {
    
    private ListDataProvider<PlayerWithKTR> dataProvider;
    
    private Column<PlayerWithKTR> linkColumn;
    
    KTRRankingGrid() {
        
        addColumn(playerWithKTR -> playerWithKTR.rank())
            .setHeader("Rank")
            .setAutoWidth(true)
            .setSortable(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(0);
        
        addColumn(playerWithKTR -> playerWithKTR.player().name())
            .setHeader("Név")
            .setSortable(true)
            .setComparator((p1, p2) -> StringUtil.HUN_COLLATOR.compare(p1.player().name(), p2.player().name()))
            .setFlexGrow(1);
        
        addComponentColumn(KTRRankingGrid::createKTRComponent)
            .setHeader("KTR")
            .setComparator(Comparator.comparing(PlayerWithKTR::ktr))
            .setWidth("110px")
            .setFlexGrow(0);
        
        addColumn(playerWithKTR -> playerWithKTR.numberOfMatches())
            .setHeader("Match")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setSortable(true)
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        addColumn(playerWithKTR -> playerWithKTR.numberOfWins())
            .setHeader("Wins")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setSortable(true)
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        addComponentColumn(KTRRankingGrid::createTrophiesComponent)
            .setHeader("Trophies")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setComparator(Comparator.comparing(PlayerWithKTR::numberOfTrophies))
            .setWidth("110px")
            .setFlexGrow(0);
        
        linkColumn = addComponentColumn(KTRRankingGrid::createPlayerStatsLink)
            .setHeader("")
            .setFlexGrow(0);
        
        setWidthFull();
        setHeightFull();
        setMinWidth("700px");
        
        UI.getCurrent().getPage().retrieveExtendedClientDetails(e -> updateVisibleColumns(e.getBodyClientWidth()));
        UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> updateVisibleColumns(e.getWidth()));
    }
    
    private static Component createKTRComponent(PlayerWithKTR playerWithKTR) {
        Span span = new Span(new Label(playerWithKTR.ktr().toString()));
        span.add(createKTRChangeSpan(playerWithKTR.ktrChange()));
        return span;
    }
    
    static Span createKTRChangeSpan(KTR ktrChange) {
        if(ktrChange.isDefinded() && ktrChange.value().doubleValue() != 0) {
            double diff = ktrChange.value().doubleValue();
            if(Math.abs(diff) >= 0.05) {
                if(diff > 0) {
                    return createKTRChangeSpan(ktrChange, "arrow-up", "var(--lumo-success-text-color)");
                } else {
                    return createKTRChangeSpan(ktrChange, "arrow-down", "var(--lumo-error-text-color)");
                }  
            }
        }
        return new Span();
    }
    
    private static Span createKTRChangeSpan(KTR ktrChange, String arrow, String color) {
        Icon icon = new Icon("lumo", arrow);
        Label label = new Label(ktrChange.toString());
        label.getStyle().set("font-size", "11px");
        Span span = new Span(icon, label);
        span.getStyle().set("color", color);
        UIUtils.setTooltip("KTR változás az elmúlt hét napban", span);
        return span;
    }
    
    static Component createPlayerStatsLink(PlayerWithKTR playerWithKTR) {
        Anchor anchor = createAnchor(playerWithKTR.player().id());
        UIUtils.setTooltip("Megnyitás új böngészőablakban", anchor);
        return anchor;
    }
    
    private static Anchor createAnchor(int playerId) {
        return new Anchor("player-stats/" + playerId, "Adatlap", AnchorTarget.BLANK);
    }
    
    static Component createTrophiesComponent(PlayerWithKTR playerWithKTR) {
        
        Span span = new Span();
        String size = playerWithKTR.numberOfTrophies() < 6 ? "15px" : "10px";
        for(int i=0;i<playerWithKTR.numberOfTrophies();i++) {
            Icon icon = VaadinIcon.TROPHY.create();
            icon.setColor("Goldenrod");
            icon.setSize(size);
            span.add(icon);
        }
        
        return span;
    }
    
    void setKTRRankingList(List<PlayerWithKTR> entries) {
        dataProvider = new ListDataProvider<>(entries);
        setItems(dataProvider);
    }
    
    void filter(String playerNamePart) {
        dataProvider.clearFilters();
        dataProvider.addFilter(playerWithKTR -> cleanNameString(playerWithKTR.player().name()).contains(cleanNameString(playerNamePart)));
    }
    
    private void updateVisibleColumns(int width) {
        boolean smallScreen = width < VaadinUtil.SMALL_SCREEN_BREAKPOINT;

        linkColumn.setVisible(smallScreen);
    }

}

