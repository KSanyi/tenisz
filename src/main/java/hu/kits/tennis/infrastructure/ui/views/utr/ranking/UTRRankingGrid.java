package hu.kits.tennis.infrastructure.ui.views.utr.ranking;

import static hu.kits.tennis.common.StringUtil.cleanNameString;
import static java.util.stream.Collectors.joining;

import java.util.Comparator;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.data.provider.ListDataProvider;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.domain.utr.PlayerWithUTR;
import hu.kits.tennis.domain.utr.UTR;
import hu.kits.tennis.domain.utr.UTRService;

class UTRRankingGrid extends Grid<PlayerWithUTR> {
    
    private final UTRService utrService;
    
    private ListDataProvider<PlayerWithUTR> dataProvider;
    
    UTRRankingGrid() {
        
        utrService = Main.resourceFactory.getUTRService();
        
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
        
        addColumn(playerWithUTR -> playerWithUTR.utr())
            .setHeader("UTR")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setSortable(true)
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        addComponentColumn(this::createUTRChangeComponent)
            .setHeader("Változás")
            .setKey("utrChange")
            .setComparator(Comparator.comparing(PlayerWithUTR::utrChange))
            .setFlexGrow(0);
        
        setWidthFull();
        
        /*
        addColumn(playerWithUTR -> playerWithUTR.player().utrGroup())
            .setHeader("UTR csop")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setSortable(true)
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        addColumn(playerWithUTR -> String.format("%.1f", playerWithUTR.utr().value() - playerWithUTR.player().utrGroup()))
            .setHeader("UTR diff")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setSortable(true)
            .setAutoWidth(true)
            .setFlexGrow(0);
            
        setWidth("600px");
        */
        
        setHeightFull();
    }
    
    private Component createUTRChangeComponent(PlayerWithUTR playerWithUTR) {
        UTR utrChange = playerWithUTR.utrChange();
        if(utrChange.isDefinded() && utrChange.value().doubleValue() != 0) {
            double diff = utrChange.value().doubleValue();
            if(diff > 0) {
                return createChangeSpan(utrChange, "arrow-up", "var(--lumo-success-text-color)");
            } else {
                return createChangeSpan(utrChange, "arrow-down", "var(--lumo-error-text-color)");
            }
        } else {
            return new Span();
        }
    }
    
    private static Component createChangeSpan(UTR utrChange, String arrow, String color) {
        Icon icon = new Icon("lumo", arrow);
        Label label = new Label(utrChange.toString());
        label.getStyle().set("font-size", "11px");
        Span span = new Span(icon, label);
        span.getStyle().set("color", color);
        span.getElement().setProperty("title", "UTR változás múlt hétfő óta");
        return span;
    }
    
    void refresh() {
        List<PlayerWithUTR> entries = utrService.calculateUTRRanking();
        dataProvider = new ListDataProvider<>(entries);
        setItems(dataProvider);
    }
    
    void filter(String playerNamePart) {
        dataProvider.clearFilters();
        dataProvider.addFilter(playerWithUtr -> cleanNameString(playerWithUtr.player().name()).contains(cleanNameString(playerNamePart)));
    }
    
    String createTableInCopyableFormat() {
        return dataProvider.getItems().stream()
                .map(e -> String.format(Formatters.HU_LOCALE, "%s\t%s\t%,.2f", e.player().id(), e.player().name(), e.utr().value()))
                .collect(joining("\n"));
    }

}

