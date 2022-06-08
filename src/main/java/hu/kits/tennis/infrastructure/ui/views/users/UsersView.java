package hu.kits.tennis.infrastructure.ui.views.users;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.user.Role;
import hu.kits.tennis.domain.user.UserData;
import hu.kits.tennis.domain.user.UserService;
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

@Route(value = "users", layout = MainLayout.class)
@PageTitle("Felhasználók")
@AllowedRoles({Role.ADMIN})
public class UsersView extends SplitViewFrame implements View {

    private final UserDetailsDrawer detailsDrawer;
    private final UsersGrid usersGrid;
    private final Button newUserButton = UIUtils.createPrimaryButton("Új felhasználó", VaadinIcon.PLUS);
    
    public UsersView() {
        
        UserService userService = Main.resourceFactory.getUserService();
        detailsDrawer = new UserDetailsDrawer(userService, DetailsDrawer.Position.RIGHT, this);
        usersGrid = new UsersGrid(userService);
        
        usersGrid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(this::showDetails));
        newUserButton.addClickListener(click -> newUser());
    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initAppBar();
        setViewContent(createContent());
        setViewDetails(detailsDrawer);
        detailsDrawer.hide();
    }
    
    private static void initAppBar() {
        AppBar appBar = MainLayout.get().getAppBar();
        appBar.removeAllActionItems();
    }
    
    private Component createContent() {
        newUserButton.setWidth("160px");
        FlexBoxLayout content = new FlexBoxLayout(newUserButton, usersGrid);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setWidthFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        
        return content;
    }
    
    private void showDetails(UserData userData) {
        detailsDrawer.setUser(userData);
        detailsDrawer.show();
    }
    
    private void newUser() {
        detailsDrawer.setUser(UserData.createNew());
        detailsDrawer.show();
    }

    public void refresh() {
        usersGrid.refresh();
    }
    
}
