package hu.kits.tennis.infrastructure.ui.views.ktr.ranking;

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
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;

import hu.kits.tennis.domain.ktr.KTR;
import hu.kits.tennis.domain.ktr.PlayerWithKTR;
import hu.kits.tennis.infrastructure.ui.vaadin.components.Badge;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo.BadgeColor;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo.BadgeShape;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo.BadgeSize;

class KTRRankingGridMobile extends Grid<PlayerWithKTR> {

    private ListDataProvider<PlayerWithKTR> dataProvider;
    
    KTRRankingGridMobile() {
        
        addComponentColumn(KTRRankingCard::new);

        setSelectionMode(SelectionMode.NONE);
        setSizeFull();
    }
    
    void setKTRRankingList(List<PlayerWithKTR> entries) {
        dataProvider = new ListDataProvider<>(entries);
        setItems(dataProvider);
    }
    
    void filter(String playerNamePart) {
        dataProvider.clearFilters();
        dataProvider.addFilter(playerWithKTR -> cleanNameString(playerWithKTR.player().name()).contains(cleanNameString(playerNamePart)));
    }
    
    private static class KTRRankingCard extends ListItem {
        
        KTRRankingCard(PlayerWithKTR playerWithKTR) {
            
            addClassNames(
                    Background.CONTRAST_5,
                    Display.FLEX, 
                    FlexDirection.ROW, 
                    AlignItems.CENTER, 
                    Padding.SMALL, 
                    BorderRadius.LARGE,
                    JustifyContent.BETWEEN);
            
            Span rank = new Span();
            rank.addClassNames(FontSize.SMALL, TextColor.SECONDARY);
            rank.setText("#" + playerWithKTR.rank());
            
            Span name = new Span();
            name.addClassNames(FontSize.XLARGE, FontWeight.SEMIBOLD);
            name.setText(playerWithKTR.player().name());
            
            Badge ktrBadge = createKTRGroupBadge(playerWithKTR.ktr());
            
            Span ktrChangeSpan = KTRRankingGrid.createKTRChangeSpan(playerWithKTR.ktrChange());
            Span ktr = new Span(ktrChangeSpan, ktrBadge);
            ktr.addClassNames(Gap.SMALL);
            
            String stats = playerWithKTR.numberOfWins() + " / " + playerWithKTR.numberOfMatches() + " győzelem";
            Span statsSpan = new Span(stats);
            statsSpan.addClassNames(FontSize.SMALL, TextColor.SECONDARY);
            
            Component trophies = KTRRankingGrid.createTrophiesComponent(playerWithKTR);
            Component link = KTRRankingGrid.createPlayerStatsLink(playerWithKTR);
            
            Div left = new Div(rank, name, trophies, link);
            left.addClassNames(Display.FLEX, FlexDirection.COLUMN);
            
            Div right = new Div(ktr, statsSpan);
            right.addClassNames(Display.FLEX, FlexDirection.COLUMN, AlignItems.END);
            
            add(left, right);
        }
        
        private static Badge createKTRGroupBadge(KTR ktr) {
            Badge badge = new Badge("KTR: " + ktr, BadgeColor.CONTRAST_PRIMARY, BadgeSize.M, BadgeShape.PILL);
            badge.setWidth("80px");
            return badge;
        }
        
    }

}

