package hu.kits.tennis.infrastructure.ui.views.utr.ranking;

import static hu.kits.tennis.common.StringUtil.cleanNameString;

import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
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
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
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
            
            Component utrChangeSpan = createUTRChangeComponent(playerWithUTR.utrChange());
            Span utr = new Span(utrChangeSpan, utrBadge);
            
            String stats = playerWithUTR.numberOfWins() + " / " + playerWithUTR.numberOfMatches() + " győzelem";
            Span statsSpan = new Span(stats);
            statsSpan.addClassNames(FontSize.SMALL, TextColor.SECONDARY);
            
            Div left = new Div(rank, name, createAnchor(playerWithUTR));
            left.addClassNames(Display.FLEX, FlexDirection.COLUMN);
            
            Div right = new Div(utr, statsSpan);
            right.addClassNames(Display.FLEX, FlexDirection.COLUMN, AlignItems.END);
            
            add(left, right);
        }
        
        private static Badge createUTRGroupBadge(UTR utr) {
            
            BadgeColor badgeColor = switch(utr.utrGroup()) {
                case 10 -> BadgeColor.CONTRAST_PRIMARY;
                case 9 -> BadgeColor.ERROR_PRIMARY;
                case 8 -> BadgeColor.ERROR;
                case 7 -> BadgeColor.NORMAL_PRIMARY;
                case 6 -> BadgeColor.NORMAL;
                case 5 -> BadgeColor.SUCCESS_PRIMARY;
                case 4 -> BadgeColor.SUCCESS;
                default -> BadgeColor.CONTRAST;
            };
            Badge badge = new Badge("UTR: " + utr, badgeColor, BadgeSize.M, BadgeShape.PILL);
            badge.setWidth("80px");
            return badge;
        }
        
        private static Component createUTRChangeComponent(UTR utrChange) {
            if(utrChange.isDefinded() && utrChange.value().doubleValue() != 0) {
                double diff = utrChange.value().doubleValue();
                if(Math.abs(diff) >= 0.05) {
                    if(diff > 0) {
                        return createChangeSpan(utrChange, "arrow-up", "var(--lumo-success-text-color)");
                    } else {
                        return createChangeSpan(utrChange, "arrow-down", "var(--lumo-error-text-color)");
                    }  
                }
            }
            return new Span();
        }
        
        private static Component createChangeSpan(UTR utrChange, String arrow, String color) {
            Icon icon = new Icon("lumo", arrow);
            Label label = new Label(utrChange.toString());
            label.getStyle().set("font-size", "11px");
            Span span = new Span(icon, label);
            span.getStyle().set("color", color);
            UIUtils.setTooltip("UTR változás az elmúlt hét napban", span);
            return span;
        }
        
        private static Anchor createAnchor(PlayerWithUTR playerWithUTR) {
            return new Anchor("player-stats/" + playerWithUTR.player().id(), "Részletek...", AnchorTarget.BLANK);
        }
        
    }

}

