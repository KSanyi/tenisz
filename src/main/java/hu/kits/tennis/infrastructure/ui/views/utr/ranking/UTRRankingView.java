package hu.kits.tennis.infrastructure.ui.views.utr.ranking;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.olli.ClipboardHelper;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
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
import hu.kits.tennis.infrastructure.ui.vaadin.SplitViewFrame;
import hu.kits.tennis.infrastructure.ui.vaadin.components.FlexBoxLayout;
import hu.kits.tennis.infrastructure.ui.vaadin.components.navigation.bar.AppBar;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.BoxSizing;
import hu.kits.tennis.infrastructure.ui.vaadin.util.layout.size.Horizontal;
import hu.kits.tennis.infrastructure.ui.vaadin.util.layout.size.Top;
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
        refresh();
    }
    
    private void playerSelected(SelectionEvent<Grid<PlayerWithUTR>, PlayerWithUTR> event) {
        if(event.getFirstSelectedItem().isPresent()) {
            Player player = event.getFirstSelectedItem().get().player();
            logger.debug("Looking for {}'s matches", player.name());
            playerStatsView.setPlayer(player);
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
        column1.setHeightFull();
        column1.setHorizontalComponentAlignment(Alignment.CENTER, header);
        
        FlexBoxLayout content = new FlexBoxLayout(column1, playerStatsView);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setSizeFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        content.setFlexDirection(FlexLayout.FlexDirection.ROW);
        content.setSpacing(Horizontal.M);
        return content;
    }
    
    public void refresh() {
        utrRankingGrid.refresh();
        clipboardHelper.setContent(utrRankingGrid.createTableInCopyableFormat());
    }
    
}
