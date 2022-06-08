package hu.kits.tennis.infrastructure.ui.vaadin.components.navigation.bar;

import java.util.ArrayList;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.shared.Registration;

import hu.kits.tennis.domain.user.Role;
import hu.kits.tennis.infrastructure.ui.MainLayout;
import hu.kits.tennis.infrastructure.ui.component.LoginDialog;
import hu.kits.tennis.infrastructure.ui.component.PasswordChangeDialog;
import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;
import hu.kits.tennis.infrastructure.ui.vaadin.components.FlexBoxLayout;
import hu.kits.tennis.infrastructure.ui.vaadin.components.Initials;
import hu.kits.tennis.infrastructure.ui.vaadin.components.navigation.tab.NaviTabs;
import hu.kits.tennis.infrastructure.ui.vaadin.util.LumoStyles;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

@CssImport("./styles/components/app-bar.css")
@CssImport(value = "./styles/components/date-picker.css", themeFor = "vaadin-date-picker")
public class AppBar extends Header {

    private static final String CLASS_NAME = "app-bar";

    private FlexBoxLayout container;

    private Button menuIcon;
    private Button contextIcon;

    private H1 title;
    private FlexBoxLayout actionItems;

    private FlexBoxLayout tabContainer;
    private NaviTabs tabs;
    private ArrayList<Registration> tabSelectionListeners;
    
    private final Button loginButton = UIUtils.createPrimaryButton("Bejelentkezés", VaadinIcon.SIGN_IN);
    
    private final Initials userInitials = new Initials();

    public enum NaviMode {
        MENU, CONTEXTUAL
    }

    public AppBar() {
        setClassName(CLASS_NAME);

        initMenuIcon();
        initContextIcon();
        initTitle();
        initActionItems();
        initContainer();
        initTabs();
        
        ContextMenu contextMenu = new ContextMenu(userInitials);
        contextMenu.setOpenOnClick(true);
        contextMenu.addItem("Jelszóváltoztatás", click -> passwordChange());
        contextMenu.addItem("Kijelentkezés", click -> logout());
        
        loginButton.addClickListener(click -> new LoginDialog().open());
        
        if(VaadinUtil.getUser().role() != Role.ANONYMUS) {
            userLoggedIn();
        } else {
            userInitials.setVisible(false);
        }
    }

    public void setNaviMode(NaviMode mode) {
        if (mode.equals(NaviMode.MENU)) {
            menuIcon.setVisible(true);
            contextIcon.setVisible(false);
        } else {
            menuIcon.setVisible(false);
            contextIcon.setVisible(true);
        }
    }

    private void initMenuIcon() {
        menuIcon = UIUtils.createTertiaryInlineButton(VaadinIcon.MENU);
        menuIcon.addClassName(CLASS_NAME + "__navi-icon");
        menuIcon.addClickListener(e -> MainLayout.get().getNaviDrawer().toggle());
        UIUtils.setAriaLabel("Menu", menuIcon);
        UIUtils.setLineHeight("1", menuIcon);
    }

    private void initContextIcon() {
        contextIcon = UIUtils.createTertiaryInlineButton(VaadinIcon.ARROW_LEFT);
        contextIcon.addClassNames(CLASS_NAME + "__context-icon");
        contextIcon.setVisible(false);
        UIUtils.setAriaLabel("Back", contextIcon);
        UIUtils.setLineHeight("1", contextIcon);
    }

    private void initTitle() {
        this.title = new H1("");
        this.title.setClassName(CLASS_NAME + "__title");
    }
    
    private void initActionItems() {
        actionItems = new FlexBoxLayout();
        actionItems.addClassName(CLASS_NAME + "__action-items");
        actionItems.setVisible(false);
    }

    private void initContainer() {
        container = new FlexBoxLayout(menuIcon, contextIcon, title, actionItems, userInitials, loginButton);
        container.addClassName(CLASS_NAME + "__container");
        container.setAlignItems(FlexComponent.Alignment.CENTER);
        add(container);
    }

    private void initTabs() {

        this.tabs = new NaviTabs();
        this.tabs.setClassName(CLASS_NAME + "__tabs");
        this.tabs.setVisible(false);

        this.tabSelectionListeners = new ArrayList<>();

        tabContainer = new FlexBoxLayout(this.tabs);
        tabContainer.addClassName(CLASS_NAME + "__tab-container");
        tabContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        add(tabContainer);
    }

    /* === MENU ICON === */

    public Button getMenuIcon() {
        return menuIcon;
    }

    public void setContextIcon(Icon icon) {
        contextIcon.setIcon(icon);
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }
    
    public boolean hasTitle() {
        return ! title.getText().isEmpty();
    }
    
    public void addActionItem(Component ... component) {
        actionItems.add(component);
        updateActionItemsVisibility();
    }

    public void removeAllActionItems() {
        actionItems.removeAll();
        updateActionItemsVisibility();
    }

    /* === TABS === */

    public void centerTabs() {
        tabs.addClassName(LumoStyles.Margin.Horizontal.AUTO);
    }

    private void configureTab(Tab tab) {
        tab.addClassName(CLASS_NAME + "__tab");
        updateTabsVisibility();
    }

    public Tab addTab(String text) {
        Tab tab = tabs.addTab(text);
        configureTab(tab);
        return tab;
    }

    public Tab addTab(String text, Class<? extends Component> navigationTarget) {
        Tab tab = tabs.addTab(text, navigationTarget);
        configureTab(tab);
        return tab;
    }

    public Tab getSelectedTab() {
        return tabs.getSelectedTab();
    }

    public void setSelectedTab(Tab selectedTab) {
        tabs.setSelectedTab(selectedTab);
    }

    public void updateSelectedTab(String text, Class<? extends Component> navigationTarget) {
        tabs.updateSelectedTab(text, navigationTarget);
    }

    public void navigateToSelectedTab() {
        tabs.navigateToSelectedTab();
    }

    public void addTabSelectionListener(ComponentEventListener<Tabs.SelectedChangeEvent> listener) {
        Registration registration = tabs.addSelectedChangeListener(listener);
        tabSelectionListeners.add(registration);
    }

    public int getTabCount() {
        return tabs.getTabCount();
    }

    public void removeAllTabs() {
        tabSelectionListeners.forEach(registration -> registration.remove());
        tabSelectionListeners.clear();
        tabs.removeAll();
        updateTabsVisibility();
    }

    /* === RESET === */

    public void reset() {
        title.setText("");
        setNaviMode(AppBar.NaviMode.MENU);
        removeAllActionItems();
        removeAllTabs();
    }

    /* === UPDATE VISIBILITY === */

    private void updateActionItemsVisibility() {
        actionItems.setVisible(actionItems.getComponentCount() > 0);
    }

    private void updateTabsVisibility() {
        tabs.setVisible(tabs.getComponentCount() > 0);
    }

    public void addContextIconClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
        contextIcon.addClickListener(listener);
    }
    
    public void userLoggedIn() {
        userInitials.setVisible(true);
        userInitials.add(VaadinUtil.getUser().initials());
        loginButton.setVisible(false);
    }
    
    private static void logout() {
        MainLayout.get().userLogOut();
    }
    
    private static void passwordChange() {
        new PasswordChangeDialog().open();
    }
}
