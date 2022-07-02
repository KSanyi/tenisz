package hu.kits.tennis.infrastructure.ui.views.utr.matches;

import static java.util.stream.Collectors.toList;

import java.util.Comparator;
import java.util.List;

import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.selection.SelectionEvent;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.common.StringUtil;
import hu.kits.tennis.domain.utr.BookedMatch;
import hu.kits.tennis.domain.utr.UTRService;

class MatchesGrid extends Grid<BookedMatch> {
    
    private final UTRService utrService;
    
    private ListDataProvider<BookedMatch> dataProvider;
    
    MatchesGrid() {
        
        utrService = Main.resourceFactory.getUTRService();
        
        addColumn(match -> Formatters.formatDate(match.playedMatch().date()))
            .setKey("date")
            .setHeader("Dátum")
            .setSortable(true)
            .setAutoWidth(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(1);
        
        addColumn(match -> match.playedMatch().player1().name())
            .setKey("player1")
            .setHeader("")
            .setAutoWidth(true)
            .setSortable(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(3);
        
        addColumn(match -> match.player1UTR())
            .setHeader("UTR 1")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        addColumn(match -> match.playedMatch().player2().name())
            .setKey("player2")
            .setHeader("")
            .setAutoWidth(true)
            .setSortable(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(3);
        
        addColumn(match -> match.player2UTR())
            .setHeader("UTR 2")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        
        addColumn(TemplateRenderer.<BookedMatch>of("<b>[[item.result]]</b>")
                .withProperty("result", match -> match.playedMatch().result() != null ? match.playedMatch().result().toString() : ""))
            .setHeader("Eredmény")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(1);
        
        addColumn(match -> match.matchUTRForPlayer1())
            .setHeader("Meccs UTR 1")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        addColumn(match -> match.matchUTRForPlayer2())
            .setHeader("Meccs UTR 2")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        setAllRowsVisible(true);
        
        sort(List.of(new GridSortOrder<>(getColumnByKey("date"), SortDirection.DESCENDING)));
        
        addSelectionListener(MatchesGrid::matchSelected);
        
        refresh();
    }
    
    private static void matchSelected(SelectionEvent<Grid<BookedMatch>, BookedMatch> e) {
        e.getFirstSelectedItem().ifPresent(match -> new MatchDialog(new MatchDataBean(match.playedMatch())).open());
    }

    void refresh() {
        List<BookedMatch> entries = utrService.loadBookedMatches().stream()
                .sorted(Comparator.comparing((BookedMatch bookedMatch) -> bookedMatch.playedMatch().date()).reversed())
                .limit(300)
                .collect(toList());
        dataProvider = new ListDataProvider<>(entries);
        setItems(dataProvider);
    }

    void filter(String filterText) {
        dataProvider.clearFilters();
        String[] filterParts = StringUtil.cleanNameString(filterText).split(" ");
        //Stream.of(filterParts).forEach(filterPart -> dataProvider.addFilter(match -> match.matches(filterPart)));
    }
    
}

