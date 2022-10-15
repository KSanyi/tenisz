package hu.kits.tennis.infrastructure.ui.views.utr.ranking;

import static hu.kits.tennis.common.StringUtil.cleanNameString;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

import java.util.Comparator;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.domain.utr.PlayerWithUTR;
import hu.kits.tennis.domain.utr.UTR;
import hu.kits.tennis.domain.utr.UTRService;
import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

class UTRRankingGrid extends Grid<PlayerWithUTR> {
    
    private final UTRService utrService;
    
    private ListDataProvider<PlayerWithUTR> dataProvider;
    
    UTRRankingGrid() {
        
        utrService = Main.resourceFactory.getUTRService();
        
        addComponentColumn(this::createMobileComponent)
            .setVisible(false);
        
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
        
        UI.getCurrent().getPage().retrieveExtendedClientDetails(e -> updateVisibleColumns(e.getBodyClientWidth()));
        UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> updateVisibleColumns(e.getWidth()));
    }
    
    private void updateVisibleColumns(int width) {
        boolean mobile = width < VaadinUtil.MOBILE_BREAKPOINT;
        var columns = getColumns();

        // "Mobile" column
        columns.get(0).setVisible(mobile);
        // "Desktop" columns
        for (int i = 1; i < columns.size(); i++) {
            columns.get(i).setVisible(!mobile);
        }
        
    }

    private Component createUTRChangeComponent(PlayerWithUTR playerWithUTR) {
        UTR utrChange = playerWithUTR.utrChange();
        if(utrChange.isDefinded() && utrChange.value().doubleValue() != 0) {
            double diff = utrChange.value().doubleValue();
            if(Math.abs(diff) >= 0.03) {
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
        UIUtils.setTooltip("UTR változás az elmúlt hét napban", span);
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
                .sorted(comparing(p -> p.player().id()))
                .map(e -> String.format(Formatters.HU_LOCALE, "%s\t%s\t%,.2f", e.player().id(), e.player().name(), e.utr().value()))
                .collect(joining("\n"));
    }
    
    private Component createMobileComponent(PlayerWithUTR playerWithUTR) {
        Label rank = new Label(String.valueOf(playerWithUTR.rank()));
        Label name = new Label(playerWithUTR.player().name());
        Label utr = new Label(playerWithUTR.utr().toString());
        HorizontalLayout layout = new HorizontalLayout(rank, name, createUTRChangeComponent(playerWithUTR), utr);
        layout.expand(name);
        return layout;
    }

}

