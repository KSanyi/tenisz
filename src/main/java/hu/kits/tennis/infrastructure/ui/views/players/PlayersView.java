package hu.kits.tennis.infrastructure.ui.views.players;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.user.Role;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.domain.utr.PlayersService;
import hu.kits.tennis.infrastructure.ui.MainLayout;
import hu.kits.tennis.infrastructure.ui.util.AllowedRoles;
import hu.kits.tennis.infrastructure.ui.vaadin.SplitViewFrame;
import hu.kits.tennis.infrastructure.ui.vaadin.components.FlexBoxLayout;
import hu.kits.tennis.infrastructure.ui.vaadin.components.detailsdrawer.DetailsDrawer;
import hu.kits.tennis.infrastructure.ui.vaadin.components.navigation.bar.AppBar;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.BoxSizing;
import hu.kits.tennis.infrastructure.ui.vaadin.util.layout.size.Horizontal;
import hu.kits.tennis.infrastructure.ui.vaadin.util.layout.size.Top;
import hu.kits.tennis.infrastructure.ui.views.View;

@Route(value = "players", layout = MainLayout.class)
@PageTitle("Játékosok")
@AllowedRoles({Role.ADMIN})
public class PlayersView extends SplitViewFrame implements View {

    private final PlayersService playersService = Main.resourceFactory.getPlayersService();
    
    private final TextField filter = new TextField();
    private final PlayersGrid playersGrid;
    private final PlayerDetailsDrawer playerDetailsDrawer;
    private final Button newUserButton = UIUtils.createPrimaryButton("Új felhasználó", VaadinIcon.PLUS);
    
    public PlayersView() {
        
        playerDetailsDrawer = new PlayerDetailsDrawer(playersService, DetailsDrawer.Position.RIGHT, this);
        playersGrid = new PlayersGrid(playersService);
        playersGrid.setSizeFull();
        
        playersGrid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(this::showDetails));
        newUserButton.addClickListener(click -> newUser());
    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initAppBar();
        setViewContent(createContent());
        setViewDetails(playerDetailsDrawer);
        playerDetailsDrawer.hide();
    }
    
    private static void initAppBar() {
        AppBar appBar = MainLayout.get().getAppBar();
        appBar.removeAllActionItems();
    }
    
    private Component createContent() {
        
        VerticalLayout tableWithFilter = new VerticalLayout(filter, playersGrid);
        tableWithFilter.setPadding(false);
        tableWithFilter.setSpacing(false);
        tableWithFilter.setSizeUndefined();
        tableWithFilter.setHeightFull();
        tableWithFilter.setHorizontalComponentAlignment(Alignment.CENTER, filter);
        
        filter.addValueChangeListener(v -> playersGrid.filter(v.getValue()));
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        
        newUserButton.setWidth("160px");
        FlexBoxLayout content = new FlexBoxLayout(newUserButton, tableWithFilter);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setSizeFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        
        return content;
    }
    
    private void showDetails(Player player) {
        playerDetailsDrawer.setPlayer(player);
        playerDetailsDrawer.show();
    }
    
    private void newUser() {
        playerDetailsDrawer.setNewPlayer();
        playerDetailsDrawer.show();
    }

    public void refresh() {
        playersGrid.refresh();
    }
    
}
