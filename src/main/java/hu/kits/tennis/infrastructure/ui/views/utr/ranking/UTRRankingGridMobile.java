package hu.kits.tennis.infrastructure.ui.views.utr.ranking;

import static hu.kits.tennis.common.StringUtil.cleanNameString;

import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderRadius;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;

import hu.kits.tennis.domain.utr.PlayerWithUTR;
import hu.kits.tennis.domain.utr.UTR;
import hu.kits.tennis.infrastructure.ui.vaadin.components.Badge;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo.BadgeColor;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo.BadgeShape;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo.BadgeSize;

class UTRRankingGridMobile extends Grid<PlayerWithUTR> {

    private ListDataProvider<PlayerWithUTR> dataProvider;
    
    UTRRankingGridMobile() {
        
        addComponentColumn(UTRRankingCard::new);
        
        setSizeFull();
    }
    
    void setUTRRankingList(List<PlayerWithUTR> entries) {
        dataProvider = new ListDataProvider<>(entries);
        setItems(dataProvider);
    }
    
    void filter(String playerNamePart) {
        dataProvider.clearFilters();
        dataProvider.addFilter(playerWithUtr -> cleanNameString(playerWithUtr.player().name()).contains(cleanNameString(playerNamePart)));
    }
    
    private static class UTRRankingCard extends ListItem {
        
        UTRRankingCard(PlayerWithUTR playerWithUTR) {
            
            addClassNames(Background.CONTRAST_5,
                    Display.FLEX, 
                    FlexDirection.ROW, 
                    AlignItems.CENTER, 
                    Padding.SMALL, 
                    BorderRadius.LARGE,
                    JustifyContent.BETWEEN);
            
            Span rank = new Span();
            rank.addClassNames(FontSize.SMALL, TextColor.SECONDARY);
            rank.setText("#" + playerWithUTR.rank());
            
            Span name = new Span();
            name.addClassNames(FontSize.XLARGE, FontWeight.SEMIBOLD);
            name.setText(playerWithUTR.player().name());
            
            Badge utrBadge = createUTRGroupBadge(playerWithUTR.utr());
            
            Span utrChangeSpan = UTRRankingGrid.createUTRChangeSpan(playerWithUTR.utrChange());
            Span utr = new Span(utrChangeSpan, utrBadge);
            
            String stats = playerWithUTR.numberOfWins() + " / " + playerWithUTR.numberOfMatches() + " győzelem";
            Span statsSpan = new Span(stats);
            statsSpan.addClassNames(FontSize.SMALL, TextColor.SECONDARY);
            
            Component trophies = UTRRankingGrid.createTrophiesComponent(playerWithUTR);
            Component link = UTRRankingGrid.createPlayerStatsLink(playerWithUTR);
            
            Div left = new Div(rank, name, trophies, link);
            left.addClassNames(Display.FLEX, FlexDirection.COLUMN);
            
            Div right = new Div(utr, statsSpan);
            right.addClassNames(Display.FLEX, FlexDirection.COLUMN, AlignItems.END);
            
            add(left, right);
        }
        
        private static Badge createUTRGroupBadge(UTR utr) {
            Badge badge = new Badge("UTR: " + utr, BadgeColor.CONTRAST_PRIMARY, BadgeSize.M, BadgeShape.PILL);
            badge.setWidth("80px");
            return badge;
        }
        
    }

}

