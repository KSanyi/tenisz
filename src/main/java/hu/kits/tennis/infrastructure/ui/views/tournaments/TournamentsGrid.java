package hu.kits.tennis.infrastructure.ui.views.tournaments;

import static java.util.Comparator.comparing;

import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.RouteParameters;

import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.domain.tournament.TournamentParams.Level;
import hu.kits.tennis.domain.tournament.TournamentParams.Status;
import hu.kits.tennis.domain.tournament.TournamentSummary;
import hu.kits.tennis.infrastructure.ui.vaadin.components.Badge;
import hu.kits.tennis.infrastructure.ui.vaadin.components.FlexBoxLayout;
import hu.kits.tennis.infrastructure.ui.vaadin.util.FontSize;
import hu.kits.tennis.infrastructure.ui.vaadin.util.LineHeight;
import hu.kits.tennis.infrastructure.ui.vaadin.util.LumoStyles;
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
import hu.kits.tennis.infrastructure.ui.views.tournament.TournamentView;

class TournamentsGrid extends Grid<TournamentSummary> {
    
    private static final int MOBILE_BREAKPOINT = 800;
    
    public TournamentsGrid() {
        
        addComponentColumn(TournamentsMobileTemplate::new)
            .setVisible(false);
        
        addColumn(TournamentSummary::name)
            .setHeader("Név")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setSortable(true)
            .setKey("name");
        
        addComponentColumn(t -> createLevelComponent(t.levelTo()))
            .setHeader("Szint")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setComparator(comparing(t -> t.levelTo()))
            .setSortable(true);
        
        addComponentColumn(t -> createStatusComponent(t.status()))
            .setHeader("Státusz")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setSortable(true)
            .setComparator(comparing(TournamentSummary::status))
            .setKey("state");
        
//        addColumn(tournament -> tournament.organization().name)
//            .setHeader("Szervező")
//            .setSortable(true)
//            .setFlexGrow(2);
    
        addColumn(new LocalDateRenderer<>(TournamentSummary::date, () -> Formatters.LONG_DATE_FORMAT))
            .setHeader("Dátum")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setSortable(true)
            .setComparator(comparing(TournamentSummary::date))
            .setKey("date");
        
        addColumn(LitRenderer.<TournamentSummary>of("<b>${item.name}</b>")
                .withProperty("name", t -> t.winner() != null ? t.winner().name() : ""))
            .setHeader("Győztes")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setComparator(comparing(t -> t.winner() != null ? t.winner().name() : ""))
            .setSortable(true);
        
        addColumn(TournamentSummary::numberOfPlayers)
            .setHeader("Indulók")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setSortable(true);
        
        addColumn(TournamentSummary::numberOfMatchesPlayed)
            .setHeader("Lejátszott meccsek")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setSortable(true);
        
        setHeightFull();
        
        UI.getCurrent().getPage().retrieveExtendedClientDetails(e -> updateVisibleColumns(e.getBodyClientWidth()));
        UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> updateVisibleColumns(e.getWidth()));
        
        this.setMultiSort(true);
        
        addSelectionListener(this::rowSelected);
    }
    
    private static Component createStatusComponent(Status status) {
        BadgeColor color = switch(status) {
            case COMPLETED -> BadgeColor.SUCCESS;
            case DRAFT -> BadgeColor.CONTRAST;
            case LIVE -> BadgeColor.ERROR;
        };
        return new Badge(status.name(), color, BadgeSize.S, BadgeShape.PILL);
    }
    
    private static Component createLevelComponent(Level level) {
        BadgeColor color = switch(level) {
            case L90 -> BadgeColor.CONTRAST;    
            case L125 -> BadgeColor.CONTRAST_PRIMARY;
            case L250 -> BadgeColor.SUCCESS;
            case L375 -> BadgeColor.SUCCESS_PRIMARY;
            case L500 -> BadgeColor.NORMAL;
            case L625 -> BadgeColor.NORMAL_PRIMARY;
            case L750 -> BadgeColor.ERROR;
            case L875 -> BadgeColor.ERROR_PRIMARY;
            case L1000 -> BadgeColor.ERROR_PRIMARY;
            
        };
        return new Badge(level.toString(), color, BadgeSize.M, BadgeShape.PILL);
    }
    
    private void updateVisibleColumns(int width) {
        boolean mobile = width < MOBILE_BREAKPOINT;
        List<Grid.Column<TournamentSummary>> columns = getColumns();

        // "Mobile" column
        columns.get(0).setVisible(mobile);

        // "Desktop" columns
        for (int i = 1; i < columns.size(); i++) {
            columns.get(i).setVisible(!mobile);
        }
    }
    
    private void rowSelected(SelectionEvent<Grid<TournamentSummary>, TournamentSummary> event) {
        if(event.getFirstSelectedItem().isPresent()) {
            TournamentSummary tournament = event.getFirstSelectedItem().get();
            getUI().ifPresent(ui -> ui.navigate(TournamentView.class, new RouteParameters("tournamentId", tournament.id())));
        }
    }
    
    static class TournamentsMobileTemplate extends FlexBoxLayout {

        private TournamentSummary tournament;

        public TournamentsMobileTemplate(TournamentSummary tournament) {
            this.tournament = tournament;

            UIUtils.setLineHeight(LineHeight.M, this);
            UIUtils.setPointerEvents(PointerEvents.NONE, this);

            setPadding(Vertical.S);
            setSpacing(Right.L);

            FlexBoxLayout name = getName();
            Label date = getDate();

            FlexBoxLayout column = new FlexBoxLayout(name, date);
            column.setFlexDirection(FlexDirection.COLUMN);
            column.setOverflow(Overflow.HIDDEN);

            add(column);
        }

        private FlexBoxLayout getName() {
            Label owner = UIUtils.createLabel(FontSize.M, TextColor.BODY, tournament.name());
            UIUtils.setOverflow(Overflow.HIDDEN, owner);
            UIUtils.setTextOverflow(TextOverflow.ELLIPSIS, owner);

            Badge badge = new Badge(tournament.numberOfPlayers() + " induló", BadgeColor.SUCCESS, BadgeSize.M, BadgeShape.PILL);
            badge.setWidth("100px");
            
            FlexBoxLayout wrapper = new FlexBoxLayout(owner, badge);
            wrapper.setAlignItems(Alignment.END);
            wrapper.setFlexGrow(1, owner);
            wrapper.setFlexShrink("0", badge);
            wrapper.setSpacing(Right.M);
            return wrapper;
        }

        private Label getDate() {
            Label account = UIUtils.createLabel(FontSize.S, TextColor.SECONDARY,  Formatters.formatDateLong(tournament.date()));
            account.addClassNames(LumoStyles.Margin.Bottom.S);
            UIUtils.setOverflow(Overflow.HIDDEN, account);
            UIUtils.setTextOverflow(TextOverflow.ELLIPSIS, account);
            return account;
        }

    }
    
}
