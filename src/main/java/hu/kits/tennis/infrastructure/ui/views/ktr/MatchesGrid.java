package hu.kits.tennis.infrastructure.ui.views.ktr;

import java.util.Comparator;
import java.util.Set;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.LitRenderer;

import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.domain.ktr.KTR;
import hu.kits.tennis.domain.match.MatchInfo;
import hu.kits.tennis.domain.match.MatchResult;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

@CssImport(themeFor = "vaadin-grid", value = "./styles/match-grid.css")
public class MatchesGrid extends Grid<MatchInfo> {
 
    private final Column<MatchInfo> player1KTRColumn;
    private final Column<MatchInfo> player2KTRColumn;
    
    public MatchesGrid() {
        
        addColumn(match -> match.date() != null ? Formatters.formatDateShort(match.date()) : "?")
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
        
        addColumn(LitRenderer.<MatchInfo>of("${item.name1} <small>${item.ktr1}</small>")
                .withProperty("name1", match -> match.player1().name())
                .withProperty("ktr1", match -> displayKTR(match.player1KTR())))
            .setClassNameGenerator(match -> match.result() != null && match.result().isPlayer1Winner() ? "bold" : "")
            .setKey("player1")
            .setComparator(Comparator.comparing(MatchInfo::player1KTR))
            .setHeader("")
            .setAutoWidth(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(3);
        
        addColumn(LitRenderer.<MatchInfo>of("${item.name2} ${item.ktr2}")
                .withProperty("name2", match -> match.player2().name())
                .withProperty("ktr2", match -> displayKTR(match.player2KTR())))
            .setClassNameGenerator(match -> match.result() != null && match.result().isPlayer2Winner() ? "bold" : "")
            .setKey("player2")
            .setComparator(Comparator.comparing(MatchInfo::player2KTR))
            .setHeader("")
            .setAutoWidth(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(3);
        
        addComponentColumn(this::matchResult)
            .setClassNameGenerator(match -> "bold")
            .setHeader("Eredmény")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(1);
        
        player1KTRColumn = addColumn(match -> match.matchKTRForPlayer1())
            .setHeader("Meccs KTR 1")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setAutoWidth(true)
            .setSortable(true)
            .setFlexGrow(0);
        
        player2KTRColumn = addColumn(match -> match.matchKTRForPlayer2())
            .setHeader("Meccs KTR 2")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        setHeightFull();
    }
    
    private static String displayKTR(KTR ktr) {
        return ktr.isDefinded() ? "(" + ktr + ")" : "";
    }
    
    public void setBestWorstAndKTRRelevantMatches(MatchInfo bestMatch, MatchInfo worstMatch, Set<Integer> ktrRelevantMatchIds) {
        setClassNameGenerator(match -> {
            if(match.equals(bestMatch)) {
                return "green";
            } else if(match.equals(worstMatch)) {
                return "red";
            } else if(!ktrRelevantMatchIds.contains(match.id())) {
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
    
    public void hidePlayer2KTRColumn() {
        player2KTRColumn.setVisible(false);
        player1KTRColumn.setHeader("Meccs KTR");
    }
    
}
