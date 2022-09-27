package hu.kits.tennis.infrastructure.ui.views.utr;

import java.util.List;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.TemplateRenderer;

import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.domain.utr.MatchInfo;

@CssImport(themeFor = "vaadin-grid", value = "./styles/match-grid.css")
public class MatchesGrid extends Grid<MatchInfo> {
 
    public MatchesGrid() {
        
        addColumn(match -> match.date() != null ? Formatters.formatDate(match.date()) : "?")
            .setKey("date")
            .setHeader("Dátum")
            .setSortable(true)
            .setAutoWidth(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(1);
        
        addColumn(match -> match.tournamentInfo().name())
            .setKey("tournament")
            .setHeader("Verseny")
            .setAutoWidth(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(1);
        
        addColumn(TemplateRenderer.<MatchInfo>of("[[item.name1]] <small>([[item.utr1]])</small>")
                .withProperty("name1", match -> match.player1().name())
                .withProperty("utr1", match -> match.player1UTR().toString()))
            .setClassNameGenerator(match -> match.result() != null && match.result().isPlayer1Winner() ? "bold" : "")
            .setKey("player1")
            .setHeader("")
            .setAutoWidth(true)
            .setSortable(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(3);
        
        addColumn(TemplateRenderer.<MatchInfo>of("[[item.name2]] ([[item.utr2]])")
                .withProperty("name2", match -> match.player2().name())
                .withProperty("utr2", match -> match.player2UTR().toString()))
            .setClassNameGenerator(match -> match.result() != null && match.result().isPlayer2Winner() ? "bold" : "")
            .setKey("player2")
            .setHeader("")
            .setAutoWidth(true)
            .setSortable(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(3);
        
        addColumn(match -> match.result())
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
        
        setHeightFull();
        
        sort(List.of(new GridSortOrder<>(getColumnByKey("date"), SortDirection.DESCENDING)));
    }
    
    public void setBestAndWorstMatch(MatchInfo bestMatch, MatchInfo worstMatch) {
        setClassNameGenerator(match -> match.equals(bestMatch) ? "green" : (match.equals(worstMatch) ? "red" : null));
    }
    
}
