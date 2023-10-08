package hu.kits.tennis.infrastructure.ui.views.ktr.ranking;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.ktr.PlayerWithKTR;
import hu.kits.tennis.domain.ktr.KTRService;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.user.Role;
import hu.kits.tennis.infrastructure.ui.MainLayout;
import hu.kits.tennis.infrastructure.ui.util.AllowedRoles;
import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;
import hu.kits.tennis.infrastructure.ui.vaadin.SplitViewFrame;
import hu.kits.tennis.infrastructure.ui.vaadin.components.navigation.bar.AppBar;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.views.View;
import hu.kits.tennis.infrastructure.ui.views.ktr.forecast.KTRForecastWindow;
import hu.kits.tennis.infrastructure.ui.views.ktr.playerstats.PlayerStatsComponent;

@Route(value = "ktr-ranking", layout = MainLayout.class)
@PageTitle("KTR Rangsor")
@AllowedRoles({Role.ADMIN})
public class KTRRankingView extends SplitViewFrame implements View {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final KTRService ktrService = Main.applicationContext.getKTRService();
    
    private final TextField filter = new TextField();
    private final KTRRankingGrid ktrRankingGrid = new KTRRankingGrid();
    private final KTRRankingGridMobile ktrRankingGridMobile = new KTRRankingGridMobile();
    private final PlayerStatsComponent playerStatsView = new PlayerStatsComponent();
    
    private final Button ktrForecastButton = UIUtils.createButton("KTR előrejelzés", ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
    
    private List<PlayerWithKTR> ktrRankingList;
    
    private HorizontalLayout content;
    
    public KTRRankingView() {
        
        filter.setPlaceholder("Játékos szűrő");
        filter.addValueChangeListener(v -> ktrRankingGrid.filter(v.getValue()));
        filter.addValueChangeListener(v -> ktrRankingGridMobile.filter(v.getValue()));
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        
        ktrRankingGridMobile.setVisible(false);
        
        ktrForecastButton.addClickListener(click -> KTRForecastWindow.open(ktrRankingList));
    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initAppBar();
        setViewContent(createContent());
        
        ktrRankingGrid.addSelectionListener(this::playerSelected);
        
        UI.getCurrent().getPage().retrieveExtendedClientDetails(e -> updateVisibleParts(e.getBodyClientWidth()));
        UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> updateVisibleParts(e.getWidth()));
        
        refresh();
    }
    
    private void playerSelected(SelectionEvent<Grid<PlayerWithKTR>, PlayerWithKTR> event) {
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
        UIUtils.setTooltip("KTR infó", helpIcon);
        helpIcon.setColor("#0C6CE9");
        helpIcon.addClickListener(click -> KTRInfoDialog.openDialog());
        HorizontalLayout header = new HorizontalLayout(filter, helpIcon, ktrForecastButton);
        
        VerticalLayout column1 = new VerticalLayout(header, ktrRankingGrid, ktrRankingGridMobile);
        column1.setPadding(false);
        column1.setSpacing(false);
        column1.setSizeUndefined();
        column1.setHorizontalComponentAlignment(Alignment.CENTER, header);
        
        content = new HorizontalLayout(column1, playerStatsView);
        content.setFlexGrow(1, column1);
        content.setFlexGrow(2, playerStatsView);
        content.setSizeFull();
        content.setPadding(true);
        
        return content;
    }
    
    public void refresh() {
        ktrRankingList = ktrService.calculateKTRRanking();
        ktrRankingGrid.setKTRRankingList(ktrRankingList);
        ktrRankingGridMobile.setKTRRankingList(ktrRankingList);
    }
    
    private void updateVisibleParts(int width) {
        
        boolean isMobile = width < VaadinUtil.MOBILE_BREAKPOINT;
        boolean isSmallScreen = width < VaadinUtil.SMALL_SCREEN_BREAKPOINT;
        
        playerStatsView.setVisible(!isSmallScreen);
        filter.setWidth(isMobile ? "130px" : "200px");
        content.setPadding(!isMobile);
        ktrRankingGrid.setVisible(!isMobile);
        ktrRankingGridMobile.setVisible(isMobile);
    }
    
}
