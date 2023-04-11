package hu.kits.tennis.infrastructure.ui.views.players.registration;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.player.registration.Registration;
import hu.kits.tennis.domain.player.registration.RegistrationService;
import hu.kits.tennis.domain.user.Role;
import hu.kits.tennis.infrastructure.ui.MainLayout;
import hu.kits.tennis.infrastructure.ui.util.AllowedRoles;
import hu.kits.tennis.infrastructure.ui.vaadin.SplitViewFrame;
import hu.kits.tennis.infrastructure.ui.vaadin.components.FlexBoxLayout;
import hu.kits.tennis.infrastructure.ui.vaadin.components.navigation.bar.AppBar;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.BoxSizing;
import hu.kits.tennis.infrastructure.ui.vaadin.util.layout.size.Horizontal;
import hu.kits.tennis.infrastructure.ui.vaadin.util.layout.size.Top;
import hu.kits.tennis.infrastructure.ui.views.View;

@Route(value = "registrations", layout = MainLayout.class)
@PageTitle("Regisztrációk")
@AllowedRoles({Role.ADMIN})
public class RegistrationsView extends SplitViewFrame implements View {

    private final RegistrationService registrationService = Main.applicationContext.getRegistrationService();
    
    private final RegistrationsGrid playersGrid;
    private final Button newPlayerButton = UIUtils.createPrimaryButton("Új játékos", VaadinIcon.PLUS);
    
    public RegistrationsView() {
        
        playersGrid = new RegistrationsGrid(registrationService.loadAllNewRegistrations());
        playersGrid.setSizeFull();
        
        playersGrid.addSelectionListener(e -> openApprovalWindow(e));
    }
    
    private void openApprovalWindow(SelectionEvent<Grid<Registration>, Registration> event) {
        event.getFirstSelectedItem().ifPresent(e -> new RegistrationApprovalWindow(e, this::refresh).open());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initAppBar();
        setViewContent(createContent());
    }
    
    private static void initAppBar() {
        AppBar appBar = MainLayout.get().getAppBar();
        appBar.removeAllActionItems();
    }
    
    private Component createContent() {
        
        newPlayerButton.setWidth("160px");
        
        FlexBoxLayout content = new FlexBoxLayout(playersGrid);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setSizeFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        
        return content;
    }
    
    public void refresh() {
        playersGrid.setItems(registrationService.loadAllNewRegistrations());
    }
    
}
