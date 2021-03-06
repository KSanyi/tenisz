package hu.kits.tennis.infrastructure.ui.views.utr.ranking;

import static hu.kits.tennis.common.StringUtil.cleanNameString;

import java.util.List;

import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.utr.PlayerWithUTR;
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
            .setAutoWidth(true)
            .setSortable(true)
            .setFlexGrow(1);
        
        addColumn(playerWithUTR -> playerWithUTR.utr())
            .setHeader("UTR")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setSortable(true)
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        setWidth("400px");
        
        setHeightFull();
        
        refresh();
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

}

