package hu.kits.tennis.infrastructure.ui.views.players;

import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.provider.ListDataProvider;

import hu.kits.tennis.common.StringUtil;
import hu.kits.tennis.domain.ktr.KTR;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.PlayersService;
import hu.kits.tennis.infrastructure.ui.vaadin.components.Badge;
import hu.kits.tennis.infrastructure.ui.vaadin.components.FlexBoxLayout;
import hu.kits.tennis.infrastructure.ui.vaadin.util.FontSize;
import hu.kits.tennis.infrastructure.ui.vaadin.util.LineHeight;
import hu.kits.tennis.infrastructure.ui.vaadin.util.TextColor;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.Overflow;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.PointerEvents;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.TextOverflow;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo.BadgeColor;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo.BadgeShape;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo.BadgeSize;
import hu.kits.tennis.infrastructure.ui.vaadin.util.layout.size.Right;
import hu.kits.tennis.infrastructure.ui.vaadin.util.layout.size.Vertical;

class PlayersGrid extends Grid<Player> {
    
    private static final int MOBILE_BREAKPOINT = 800;
    
    private final PlayersService playersService;
    
    private ListDataProvider<Player> dataProvider;
    
    PlayersGrid(PlayersService playersService) {
        
        this.playersService = playersService;
        
        addComponentColumn(UserDataMobileTemplate::new)
            .setVisible(false);
        
        addColumn(Player::id)
            .setHeader("Id")
            .setSortable(true)
            .setFlexGrow(1);
        
        addColumn(Player::name)
            .setHeader("Név")
            .setSortable(true)
            .setComparator((p1, p2) -> StringUtil.HUN_COLLATOR.compare(p1.name(), p2.name()))
            .setFlexGrow(3);
        
        addColumn(p -> p.contact().email())
            .setHeader("Email")
            .setSortable(true)
            .setTooltipGenerator(p -> p.contact().email())
            .setFlexGrow(2);
        
        addColumn(p -> p.contact().phone())
            .setHeader("Telefonszám")
            .setSortable(true)
            .setFlexGrow(2);
        
        addColumn(p -> p.contact().address())
            .setHeader("Cím")
            .setSortable(true)
            .setTooltipGenerator(p -> p.contact().address().toString())
            .setFlexGrow(3);
        
        addColumn(p -> p.contact().comment())
            .setHeader("Megjegyzés")
            .setSortable(true)
            .setFlexGrow(2);
        
        addColumn(Player::startingKTR)
            .setHeader("Induló KTR")
            .setSortable(true)
            .setFlexGrow(1);
        
        addColumn(player -> player.organisations().stream().map(org -> org.name).collect(joining(" ")))
            .setHeader("Szervezetek")
            .setSortable(true)
            .setFlexGrow(2);
        
        UI.getCurrent().getPage().retrieveExtendedClientDetails(e -> updateVisibleColumns(e.getBodyClientWidth()));
        UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> updateVisibleColumns(e.getWidth()));
        refresh();
    }
    
    private static Badge createKTRGroupBadge(KTR startingKTR) {
        
        BadgeColor badgeColor = switch(startingKTR.ktrGroup()) {
            case 10 -> BadgeColor.ERROR_PRIMARY;
            case 9 -> BadgeColor.ERROR_PRIMARY;
            case 8 -> BadgeColor.ERROR;
            case 7 -> BadgeColor.ERROR;
            case 6 -> BadgeColor.NORMAL_PRIMARY;
            case 5 -> BadgeColor.NORMAL;
            default -> BadgeColor.SUCCESS;
        };
        Badge badge = new Badge(startingKTR != null ? startingKTR.toString() : "", badgeColor, BadgeSize.M, BadgeShape.PILL);
        badge.setWidth("80px");
        return badge;
    }
    
    void refresh() {
        dataProvider = new ListDataProvider<>(playersService.loadAllPlayers().entries());
        setItems(dataProvider);
    }
    
    private void updateVisibleColumns(int width) {
        boolean mobile = width < MOBILE_BREAKPOINT;
        List<Grid.Column<Player>> columns = getColumns();

        // "Mobile" column
        columns.get(0).setVisible(mobile);

        // "Desktop" columns
        for (int i = 1; i < columns.size(); i++) {
            columns.get(i).setVisible(!mobile);
        }
    }
    
    /**
     * A layout for displaying User info in a mobile friendly format.
     */
    static class UserDataMobileTemplate extends FlexBoxLayout {

        private Player player;

        public UserDataMobileTemplate(Player player) {
            this.player = player;

            UIUtils.setLineHeight(LineHeight.M, this);
            UIUtils.setPointerEvents(PointerEvents.NONE, this);

            setPadding(Vertical.S);
            setSpacing(Right.L);

            FlexBoxLayout name = getName();

            FlexBoxLayout column = new FlexBoxLayout(name);
            column.setFlexDirection(FlexDirection.COLUMN);
            column.setOverflow(Overflow.HIDDEN);

            add(column);
        }

        private FlexBoxLayout getName() {
            Label owner = UIUtils.createLabel(FontSize.M, TextColor.BODY, player.name());
            UIUtils.setOverflow(Overflow.HIDDEN, owner);
            UIUtils.setTextOverflow(TextOverflow.ELLIPSIS, owner);

            Badge ktrBadge = createKTRGroupBadge(player.startingKTR());
            
            FlexBoxLayout wrapper = new FlexBoxLayout(owner, ktrBadge);
            wrapper.setAlignItems(Alignment.CENTER);
            wrapper.setFlexGrow(1, owner);
            wrapper.setFlexShrink("0", ktrBadge);
            wrapper.setSpacing(Right.M);
            return wrapper;
        }

    }

    public void filter(String value) {
        dataProvider.clearFilters();
        String[] filterParts = StringUtil.cleanNameString(value).split(" ");
        Stream.of(filterParts)
            .forEach(filterPart -> dataProvider.addFilter(player -> player.matches(filterPart)));
    }

}

