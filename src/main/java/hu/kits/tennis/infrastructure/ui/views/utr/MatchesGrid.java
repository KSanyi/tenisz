package hu.kits.tennis.infrastructure.ui.views.utr;

import java.util.Collection;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.TemplateRenderer;

import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.domain.utr.BookedMatch;
import hu.kits.tennis.domain.utr.MatchInfo;
import hu.kits.tennis.domain.utr.MatchResult;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

@CssImport(themeFor = "vaadin-grid", value = "./styles/match-grid.css")
public class MatchesGrid extends Grid<MatchInfo> {
 
    private final Column<MatchInfo> player1UtrColumn;
    private final Column<MatchInfo> player2UtrColumn;
    
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
        
        addComponentColumn(this::matchResult)
            .setClassNameGenerator(match -> "bold")
            .setHeader("Eredmény")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(1);
        
        player1UtrColumn = addColumn(match -> match.matchUTRForPlayer1())
            .setHeader("Meccs UTR 1")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        player2UtrColumn = addColumn(match -> match.matchUTRForPlayer2())
            .setHeader("Meccs UTR 2")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        setHeightFull();
        
        sort(List.of(new GridSortOrder<>(getColumnByKey("date"), SortDirection.DESCENDING)));
    }
    
    public void setBestWorstAndUTRRelevantMatches(MatchInfo bestMatch, MatchInfo worstMatch, Collection<BookedMatch> utrRelevantMatches) {
        setClassNameGenerator(match -> {
            if(match.equals(bestMatch)) {
                return "green";
            } else if(match.equals(worstMatch)) {
                return "red";
            } else if(utrRelevantMatches.stream().noneMatch(relevantMatch -> relevantMatch.playedMatch().id().equals(match.id()))) {
                return "grey";
            } else {
                return "";
            }
        });
    }
    
    private Component matchResult(MatchInfo matchInfo) {
        MatchResult result = matchInfo.result();
        if(result == null) {
            return new Span();
        } else {
            Label label = new Label(matchInfo.result().toString());
            HorizontalLayout layout = new HorizontalLayout(label);
            layout.setSpacing(false);
            layout.setAlignItems(Alignment.CENTER);
            layout.setJustifyContentMode(JustifyContentMode.CENTER);
            layout.setWidthFull();
            if(matchInfo.isUpset()) {
                Icon icon = VaadinIcon.EXCLAMATION.create();
                icon.setSize("15px");
                UIUtils.setTooltip("Meglepetés", layout);
                layout.add(icon);
                label.setText(label.getText());
            }
            return layout;
        }
    }
    
    public void setPlayer2UtrColumnVisible(boolean visible) {
        player2UtrColumn.setVisible(visible);
        player1UtrColumn.setHeader("Meccs UTR" + (visible ? " 1" :""));
    }
    
}
