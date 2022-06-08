package hu.kits.tennis.infrastructure.ui.views.utr.ranking;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.TemplateRenderer;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.common.StringUtil;
import hu.kits.tennis.domain.utr.BookedMatch;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.domain.utr.UTRService;

@CssImport(themeFor = "vaadin-grid", value = "./styles/match-grid.css")
public class PlayerMatchesGrid extends Grid<BookedMatch> {
    
    private final UTRService utrService;
    
    private ListDataProvider<BookedMatch> dataProvider;
    
    private Player player;
    
    public PlayerMatchesGrid() {
        
        utrService = Main.resourceFactory.getUTRService();
        
        addColumn(match -> Formatters.formatDate(match.playedMatch().date()))
            .setKey("date")
            .setHeader("Dátum")
            .setSortable(true)
            .setAutoWidth(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(1);
        
        addColumn(TemplateRenderer.<BookedMatch>of("[[item.name1]] <small>([[item.utr1]])</small>")
                .withProperty("name1", match -> match.playedMatch().player1().name())
                .withProperty("utr1", match -> match.player1UTR().toString()))
            .setClassNameGenerator(match -> match.playedMatch().result().isPlayer1Winner() ? "bold" : "")
            .setKey("player1")
            .setHeader("")
            .setAutoWidth(true)
            .setSortable(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(3);
        
        addColumn(TemplateRenderer.<BookedMatch>of("[[item.name2]] ([[item.utr2]])")
                .withProperty("name2", match -> match.playedMatch().player2().name())
                .withProperty("utr2", match -> match.player2UTR().toString()))
            .setClassNameGenerator(match -> match.playedMatch().result().isPlayer2Winner() ? "bold" : "")
            .setKey("player2")
            .setHeader("")
            .setAutoWidth(true)
            .setSortable(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(3);
        
        addColumn(match -> match.playedMatch().result())
            .setClassNameGenerator(match -> "bold")
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
        
        sort(List.of(new GridSortOrder<>(getColumnByKey("date"), SortDirection.DESCENDING)));
        
        setAllRowsVisible(true);
    }
    
    void setPlayer(Player player) {
        this.player = player;
        refresh();
    }
    
    void refresh() {
        List<BookedMatch> entries = utrService.loadMathesForPlayer(player).stream()
                .sorted(comparing((BookedMatch bookedMatch) -> bookedMatch.playedMatch().date()).reversed())
                .collect(toList());
        dataProvider = new ListDataProvider<>(entries);
        setItems(dataProvider);
    }

    void filter(String filterText) {
        dataProvider.clearFilters();
        String[] filterParts = StringUtil.cleanNameString(filterText).split(" ");
        Stream.of(filterParts).forEach(filterPart -> dataProvider.addFilter(match -> match.matches(filterPart)));
    }
    
}