package hu.kits.tennis.infrastructure.ui.views.utr.ranking;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import hu.kits.tennis.domain.user.Role;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.domain.utr.PlayerWithUTR;
import hu.kits.tennis.infrastructure.ui.MainLayout;
import hu.kits.tennis.infrastructure.ui.util.AllowedRoles;
import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;
import hu.kits.tennis.infrastructure.ui.vaadin.SplitViewFrame;
import hu.kits.tennis.infrastructure.ui.vaadin.components.navigation.bar.AppBar;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.views.View;

@Route(value = "utr-ranking", layout = MainLayout.class)
@PageTitle("UTR Rangsor")
@AllowedRoles({Role.ADMIN})
public class UTRRankingView extends SplitViewFrame implements View {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final TextField filter = new TextField();
    private final UTRRankingGrid utrRankingGrid = new UTRRankingGrid();
    private final PlayerStatsComponent playerStatsView = new PlayerStatsComponent();
    
    public UTRRankingView() {
        
        filter.setPlaceholder("Játékos szűrő");
        filter.addValueChangeListener(v -> utrRankingGrid.filter(v.getValue()));
        filter.setValueChangeMode(ValueChangeMode.EAGER);
    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initAppBar();
        setViewContent(createContent());
        
        utrRankingGrid.addSelectionListener(this::playerSelected);
        
        UI.getCurrent().getPage().retrieveExtendedClientDetails(e -> updateVisibleParts(e.getBodyClientWidth()));
        UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> updateVisibleParts(e.getWidth()));
        
        refresh();
    }
    
    private void playerSelected(SelectionEvent<Grid<PlayerWithUTR>, PlayerWithUTR> event) {
        if(event.getFirstSelectedItem().isPresent()) {
            
            Player player = event.getFirstSelectedItem().get().player();
            VaadinUtil.logUserAction(logger, "Looking for {}'s stats", player.name());
            if(playerStatsView.isVisible()) {
                playerStatsView.setPlayer(player);
            }
        }
    }
    
    private static void initAppBar() {
        AppBar appBar = MainLayout.get().getAppBar();
        appBar.removeAllActionItems();
    }
    
    private Component createContent() {
        
        Icon helpIcon = new Icon("vaadin", "info-circle");
        UIUtils.setTooltip("UTR infó", helpIcon);
        helpIcon.setColor("#0C6CE9");
        helpIcon.addClickListener(click -> UTRInfoDialog.openDialog());
        HorizontalLayout header = new HorizontalLayout(filter, helpIcon);
        
        VerticalLayout column1 = new VerticalLayout(header, utrRankingGrid);
        column1.setPadding(false);
        column1.setSpacing(false);
        column1.setSizeUndefined();
        column1.setHorizontalComponentAlignment(Alignment.CENTER, header);
        
        HorizontalLayout content = new HorizontalLayout(column1, playerStatsView);
        content.setFlexGrow(1, column1);
        content.setFlexGrow(2, playerStatsView);
        content.setSizeFull();
        content.setPadding(true);
        
        return content;
    }
    
    public void refresh() {
        utrRankingGrid.refresh();
    }
    
    private void updateVisibleParts(int width) {
        boolean smallScreen = width < VaadinUtil.SMALL_SCREEN_BREAKPOINT;
        playerStatsView.setVisible(!smallScreen);
    }
    
}
