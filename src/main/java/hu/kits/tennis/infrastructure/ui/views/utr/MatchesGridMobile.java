package hu.kits.tennis.infrastructure.ui.views.utr;

import java.util.Collection;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderRadius;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;

import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.domain.match.MatchInfo;
import hu.kits.tennis.domain.match.MatchResult.SetResult;
import hu.kits.tennis.domain.utr.BookedMatch;

public class MatchesGridMobile extends Grid<MatchInfo> {
 
    public MatchesGridMobile() {
        
        addComponentColumn(MatchCard::new);
        
        setSelectionMode(SelectionMode.NONE);
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
    
    private static class MatchCard extends ListItem {
        
        MatchCard(MatchInfo matchInfo) {
            
            addClassNames(Background.CONTRAST_5,
                    Display.FLEX, 
                    FlexDirection.COLUMN, 
                    AlignItems.START, 
                    Padding.SMALL, 
                    BorderRadius.LARGE,
                    JustifyContent.BETWEEN);
            
            Span date = new Span(Formatters.formatDateLong(matchInfo.date()) + " " + matchInfo.tournamentInfo().name());
            date.addClassNames(FontSize.SMALL, TextColor.SECONDARY);
            
            Span names = new Span();
            names.addClassNames(Display.FLEX, FlexDirection.COLUMN);
            
            Span player1Row = createPlayerRow(matchInfo, true);
            Span player2Row = createPlayerRow(matchInfo, false);
            
            names.add(player1Row, player2Row);
            
            add(date, names);
        }
        
        private static Span createPlayerRow(MatchInfo matchInfo, boolean player1) {
            Span player = new Span(player1 ? matchInfo.player1().name() : matchInfo.player2().name());
            player.addClassNames(FontSize.MEDIUM);
            Span playerUTR = new Span("(" + (player1 ? matchInfo.player1UTR() : matchInfo.player2UTR()) + ")");
            playerUTR.addClassNames(FontSize.XSMALL);
            
            Span playerAndUTR = new Span(player, playerUTR);
            playerAndUTR.setWidth("220px");
            if(matchInfo.result().isPlayer1Winner() && player1 || matchInfo.result().isPlayer2Winner() && !player1) {
                playerAndUTR.addClassNames(FontWeight.SEMIBOLD);
            }
            
            Span games = games(matchInfo.result().setResults(), player1);
            
            Span span = new Span(playerAndUTR, games);
            span.addClassNames(Display.FLEX);
            
            return span;
        }
        
        private static Span games(List<SetResult> setResults, boolean player1) {
            Span span = new Span();
            for(var setResult : setResults) {
                Span gameSpan = new Span(String.valueOf(player1 ? setResult.player1Score() : setResult.player2Score()));
                if(setResult.isPlayer1Winner() && player1 || setResult.isPlayer2Winner() && !player1) {
                    gameSpan.addClassNames(FontWeight.SEMIBOLD);
                }
                span.add(gameSpan);
            }
            span.addClassNames(Display.FLEX, FlexDirection.ROW, Gap.SMALL);
            return span;
        }
        
    }
    
}
