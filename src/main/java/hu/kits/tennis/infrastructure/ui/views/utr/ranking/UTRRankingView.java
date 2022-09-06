package hu.kits.tennis.infrastructure.ui.views.utr.ranking;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.olli.ClipboardHelper;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
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
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.util.AllowedRoles;
import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;
import hu.kits.tennis.infrastructure.ui.vaadin.SplitViewFrame;
import hu.kits.tennis.infrastructure.ui.vaadin.components.navigation.bar.AppBar;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.views.View;

@Route(value = "utr-ranking", layout = MainLayout.class)
@PageTitle("Player UTR")
@AllowedRoles({Role.ADMIN})
public class UTRRankingView extends SplitViewFrame implements View {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final TextField filter = new TextField();
    private final Button copyButton = UIUtils.createSmallButton(VaadinIcon.COPY);
    private final ClipboardHelper clipboardHelper = new ClipboardHelper("", copyButton);
    private final UTRRankingGrid utrRankingGrid = new UTRRankingGrid();
    private final PlayerStatsView playerStatsView = new PlayerStatsView();
    
    public UTRRankingView() {
        
        filter.addValueChangeListener(v -> utrRankingGrid.filter(v.getValue()));
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        
        copyButton.getElement().setProperty("title", "UTR lista másolása");
        copyButton.addClickListener(click -> KITSNotification.showInfo("A táblázat a vágólapra másolva"));
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
            } else {
                PlayerStatsView playerStatsViewForDialog = new PlayerStatsView();
                playerStatsViewForDialog.setPlayer(player);
                Dialog dialog = new Dialog(playerStatsViewForDialog);
                dialog.setSizeFull();
                dialog.open();
            }
        }
    }
    
    private static void initAppBar() {
        AppBar appBar = MainLayout.get().getAppBar();
        appBar.removeAllActionItems();
    }
    
    private Component createContent() {
        
        HorizontalLayout header = new HorizontalLayout(filter, clipboardHelper);
        
        VerticalLayout column1 = new VerticalLayout(header, utrRankingGrid);
        column1.setPadding(false);
        column1.setSpacing(false);
        column1.setSizeUndefined();
        column1.setSizeFull();
        column1.setHorizontalComponentAlignment(Alignment.CENTER, header);
        
        HorizontalLayout content = new HorizontalLayout(column1, playerStatsView);
        column1.setMaxWidth("460px");
        content.setSizeFull();
        content.setPadding(true);
        
        return content;
    }
    
    public void refresh() {
        utrRankingGrid.refresh();
        clipboardHelper.setContent(utrRankingGrid.createTableInCopyableFormat());
    }
    
    private void updateVisibleParts(int width) {
        boolean mobile = width < VaadinUtil.MOBILE_BREAKPOINT;
        playerStatsView.setVisible(!mobile);
    }
    
}
