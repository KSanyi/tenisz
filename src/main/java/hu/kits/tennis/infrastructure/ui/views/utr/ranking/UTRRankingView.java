package hu.kits.tennis.infrastructure.ui.views.utr.ranking;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import hu.kits.tennis.domain.user.Role;
import hu.kits.tennis.domain.utr.PlayerWithUTR;
import hu.kits.tennis.infrastructure.ui.MainLayout;
import hu.kits.tennis.infrastructure.ui.util.AllowedRoles;
import hu.kits.tennis.infrastructure.ui.vaadin.SplitViewFrame;
import hu.kits.tennis.infrastructure.ui.vaadin.components.FlexBoxLayout;
import hu.kits.tennis.infrastructure.ui.vaadin.components.navigation.bar.AppBar;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.AlignItems;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.BoxSizing;
import hu.kits.tennis.infrastructure.ui.vaadin.util.layout.size.Horizontal;
import hu.kits.tennis.infrastructure.ui.vaadin.util.layout.size.Top;
import hu.kits.tennis.infrastructure.ui.views.View;

@Route(value = "utr-ranking", layout = MainLayout.class)
@PageTitle("Player UTR")
@AllowedRoles({Role.ADMIN})
public class UTRRankingView extends SplitViewFrame implements View {

    private final TextField filter = new TextField();
    private final UTRRankingGrid utrRankingGrid = new UTRRankingGrid();
    private final PlayerMatchesGrid playerMatchesGrid = new PlayerMatchesGrid();
    
    public UTRRankingView() {
    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initAppBar();
        setViewContent(createContent());
        
        utrRankingGrid.addSelectionListener(this::playerSelected);
    }
    
    private void playerSelected(SelectionEvent<Grid<PlayerWithUTR>, PlayerWithUTR> event) {
        if(event.getFirstSelectedItem().isPresent()) {
            playerMatchesGrid.setPlayer(event.getFirstSelectedItem().get().player());
        }
    }
    
    private static void initAppBar() {
        AppBar appBar = MainLayout.get().getAppBar();
        appBar.removeAllActionItems();
    }
    
    private Component createContent() {
        
        VerticalLayout column1 = new VerticalLayout(filter, utrRankingGrid);
        column1.setPadding(false);
        column1.setMargin(false);
        column1.setSpacing(false);
        column1.setSizeUndefined();
        column1.setHorizontalComponentAlignment(Alignment.CENTER, filter);
        
        filter.addValueChangeListener(v -> utrRankingGrid.filter(v.getValue()));
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        
        FlexBoxLayout content = new FlexBoxLayout(column1, playerMatchesGrid);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setWidthFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        content.setFlexDirection(FlexLayout.FlexDirection.ROW);
        content.setSpacing(Horizontal.M);
        
        return content;
    }
    
    public void refresh() {
        utrRankingGrid.refresh();
    }
    
}
