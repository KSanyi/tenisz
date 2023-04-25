package hu.kits.tennis.infrastructure.ui.views.utr.playerstats;

import java.util.Comparator;
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
import com.vaadin.flow.data.renderer.LitRenderer;

import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.domain.match.MatchInfo;
import hu.kits.tennis.domain.match.MatchResult;
import hu.kits.tennis.domain.utr.UTR;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

@CssImport(themeFor = "vaadin-grid", value = "./styles/match-grid.css")
class TournamentMatchesGrid extends Grid<MatchInfo> {
 
    TournamentMatchesGrid() {
        
        addColumn(match -> match.date() != null ? Formatters.formatDate(match.date()) : "")
            .setKey("date")
            .setSortable(true)
            .setAutoWidth(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(1);
        
        addColumn(LitRenderer.<MatchInfo>of("${item.name1} <small>${item.utr1}</small>")
                .withProperty("name1", match -> match.player1().name())
                .withProperty("utr1", match -> displayUTR(match.player1UTR())))
            .setClassNameGenerator(match -> match.result() != null && match.result().isPlayer1Winner() ? "bold" : "")
            .setKey("player1")
            .setComparator(Comparator.comparing(MatchInfo::player1UTR))
            .setAutoWidth(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(3);
        
        addColumn(LitRenderer.<MatchInfo>of("${item.name2} ${item.utr2}")
                .withProperty("name2", match -> match.player2().name())
                .withProperty("utr2", match -> displayUTR(match.player2UTR())))
            .setClassNameGenerator(match -> match.result() != null && match.result().isPlayer2Winner() ? "bold" : "")
            .setKey("player2")
            .setComparator(Comparator.comparing(MatchInfo::player2UTR))
            .setAutoWidth(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(3);
        
        addComponentColumn(this::matchResult)
            .setClassNameGenerator(match -> "bold")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(1);
        
        addColumn(match -> match.matchUTRForPlayer1())
            .setTextAlign(ColumnTextAlign.CENTER)
            .setAutoWidth(true)
            .setSortable(true)
            .setFlexGrow(0);
        
        sort(List.of(new GridSortOrder<>(getColumnByKey("date"), SortDirection.DESCENDING)));
    }
    
    private static String displayUTR(UTR utr) {
        return utr.isDefinded() ? "(" + utr + ")" : "";
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
    
}
