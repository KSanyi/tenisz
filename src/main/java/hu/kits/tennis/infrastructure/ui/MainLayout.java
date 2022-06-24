package hu.kits.tennis.infrastructure.ui;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.Lumo;

import hu.kits.tennis.domain.user.UserData;
import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;
import hu.kits.tennis.infrastructure.ui.vaadin.components.FlexBoxLayout;
import hu.kits.tennis.infrastructure.ui.vaadin.components.navigation.bar.AppBar;
import hu.kits.tennis.infrastructure.ui.vaadin.components.navigation.drawer.NaviDrawer;
import hu.kits.tennis.infrastructure.ui.vaadin.components.navigation.drawer.NaviItem;
import hu.kits.tennis.infrastructure.ui.vaadin.components.navigation.drawer.NaviMenu;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.Display;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.Overflow;
import hu.kits.tennis.infrastructure.ui.views.View;
import hu.kits.tennis.infrastructure.ui.views.tournament.TournamentsView;
import hu.kits.tennis.infrastructure.ui.views.users.UsersView;
import hu.kits.tennis.infrastructure.ui.views.utr.ranking.UTRRankingView;
import hu.kits.tennis.infrastructure.web.CookieUtil;

@CssImport(value = "./styles/components/floating-action-button.css", themeFor = "vaadin-button")
@CssImport(value = "./styles/components/grid.css", themeFor = "vaadin-grid")
@CssImport("./styles/lumo/border-radius.css")
@CssImport("./styles/lumo/icon-size.css")
@CssImport("./styles/lumo/margin.css")
@CssImport("./styles/lumo/padding.css")
@CssImport("./styles/lumo/shadow.css")
@CssImport("./styles/lumo/spacing.css")
@CssImport("./styles/lumo/typography.css")
@CssImport("./styles/misc/box-shadow-borders.css")
@CssImport(value = "./styles/styles.css", include = "lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge")
public class MainLayout extends FlexBoxLayout implements RouterLayout, AfterNavigationObserver, BeforeEnterObserver  {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String CLASS_NAME = "root";

    private FlexBoxLayout row;
    private NaviDrawer naviDrawer;
    private FlexBoxLayout column;

    private Div appHeaderInner;
    private Main viewContainer;

    private AppBar appBar;

    private View currentView;
    
    public MainLayout() {
        VaadinSession.getCurrent()
                .setErrorHandler((ErrorHandler) errorEvent -> {
                    logger.error("Uncaught UI exception", errorEvent.getThrowable());
                    Notification.show("We are sorry, but an internal error occurred");
                });

        addClassName(CLASS_NAME);
        setFlexDirection(FlexDirection.COLUMN);
        setSizeFull();

        // Initialise the UI building blocks
        initStructure();
        // Populate the navigation drawer
        initNaviItems();
    }

    /**
     * Initialise the required components and containers.
     */
    private void initStructure() {
        naviDrawer = new NaviDrawer();

        viewContainer = new Main();
        viewContainer.addClassName(CLASS_NAME + "__view-container");
        UIUtils.setDisplay(Display.FLEX, viewContainer);
        UIUtils.setFlexGrow(1, viewContainer);
        UIUtils.setOverflow(Overflow.HIDDEN, viewContainer);

        column = new FlexBoxLayout(viewContainer);
        column.addClassName(CLASS_NAME + "__column");
        column.setFlexDirection(FlexDirection.COLUMN);
        column.setFlexGrow(1, viewContainer);
        column.setOverflow(Overflow.HIDDEN);

        row = new FlexBoxLayout(naviDrawer, column);
        row.addClassName(CLASS_NAME + "__row");
        row.setFlexGrow(1, column);
        row.setOverflow(Overflow.HIDDEN);
        add(row);
        setFlexGrow(1, row);
    }

    /**
     * Initialise the navigation items.
     */
    private void initNaviItems() {
        NaviMenu menu = naviDrawer.getMenu();
        
        menu.addNaviItem(VaadinIcon.USERS, "Felhasználók", UsersView.class);
        
        menu.addNaviItem(VaadinIcon.TROPHY, "Versenyek", TournamentsView.class);
        
        /*
        NaviItem utrMenu = menu.addNaviItem(VaadinIcon.AUTOMATION, "UTR", null);
        menu.addNaviItem(utrMenu, "Meccsek", MatchesView.class);
        menu.addNaviItem(utrMenu, "UTR ranking", UTRRankingView.class);
        menu.addNaviItem(utrMenu, "Versenyek", TournamentsView.class);
        */
    }

    /**
     * Configure the app's inner and outer headers and footers.
     */
    private void initHeadersAndFooters() {
        appBar = new AppBar();
        
        UIUtils.setTheme(Lumo.DARK, appBar);
        setAppHeaderInner(appBar);
    }

    private void setAppHeaderInner(Component... components) {
        if (appHeaderInner == null) {
            appHeaderInner = new Div();
            appHeaderInner.addClassName("app-header-inner");
            column.getElement().insertChild(0, appHeaderInner.getElement());
        }
        appHeaderInner.removeAll();
        appHeaderInner.add(components);
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        this.viewContainer.getElement().appendChild(content.getElement());
        currentView = (View)content;
    }

    public NaviDrawer getNaviDrawer() {
        return naviDrawer;
    }

    public static MainLayout get() {
        return (MainLayout) UI.getCurrent().getChildren()
                .filter(component -> component.getClass() == MainLayout.class)
                .findFirst().get();
    }

    public AppBar getAppBar() {
        return appBar;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        NaviItem active = getActiveItem(event);
        if (active != null) {
            if(!getAppBar().hasTitle()) {
                getAppBar().setTitle(active.getText());                
            }
        }
    }

    private NaviItem getActiveItem(AfterNavigationEvent e) {
        for (NaviItem item : naviDrawer.getMenu().getNaviItems()) {
            if (item.isHighlighted(e)) {
                return item;
            }
        }
        return null;
    }

    public void userLoggedIn(UserData user) {
        VaadinUtil.setUser(user);
        CookieUtil.createUserCookie(user.userId());
        appBar.userLoggedIn();
        naviDrawer.refresh();
        currentView.refresh();
        UI.getCurrent().getSession().getSession().setMaxInactiveInterval(60 * 60 * 24);
    }

    public void userLogOut() {
        UserData user = VaadinUtil.getUser();
        UI.getCurrent().getSession().close();
        CookieUtil.deleteUserCookie();
        logger.info("{} logged out", user);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        
        naviDrawer.getMenu().refresh();
        
        var loc = event.getLocation();
        @SuppressWarnings("deprecation")
        Class<?> navigationTarget = UI.getCurrent().getRouter().resolveNavigationTarget(loc).map(n -> n.getNavigationTarget()).get();
        if(!VaadinUtil.isViewAllowed(navigationTarget)) {
            event.forwardTo(UTRRankingView.class);
        }
        
        // Configure the headers and footers (optional)
        initHeadersAndFooters();
        
        getAppBar().setTitle("");
        
        VaadinUtil.logUserAction(logger, "navigated to '{}'", event.getLocation().getPath());
    }

}
