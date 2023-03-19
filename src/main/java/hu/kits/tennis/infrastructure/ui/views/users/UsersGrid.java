package hu.kits.tennis.infrastructure.ui.views.users;

import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;

import hu.kits.tennis.domain.user.Role;
import hu.kits.tennis.domain.user.UserData;
import hu.kits.tennis.domain.user.UserService;
import hu.kits.tennis.infrastructure.ui.vaadin.components.Badge;
import hu.kits.tennis.infrastructure.ui.vaadin.components.FlexBoxLayout;
import hu.kits.tennis.infrastructure.ui.vaadin.util.FontSize;
import hu.kits.tennis.infrastructure.ui.vaadin.util.LineHeight;
import hu.kits.tennis.infrastructure.ui.vaadin.util.LumoStyles;
import hu.kits.tennis.infrastructure.ui.vaadin.util.TextColor;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.Overflow;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.PointerEvents;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.TextOverflow;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo.BadgeColor;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo.BadgeShape;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo.BadgeSize;
import hu.kits.tennis.infrastructure.ui.vaadin.util.layout.size.Right;
import hu.kits.tennis.infrastructure.ui.vaadin.util.layout.size.Vertical;

class UsersGrid extends Grid<UserData> {
    
    private static final int MOBILE_BREAKPOINT = 800;
    
    private final UserService userService;
    
    UsersGrid(UserService userService) {
        
        this.userService = userService;
        
        addComponentColumn(UserDataMobileTemplate::new)
            .setVisible(false);
        
        addColumn(UserData::name)
            .setHeader("Név")
            .setSortable(true)
            .setFlexGrow(3);
        
        addComponentColumn(u -> createRoleBadge(u.role()))
            .setHeader("Típus")
            .setAutoWidth(true)
            .setFlexGrow(1);
        
        addColumn(UserData::email)
            .setHeader("Email")
            .setFlexGrow(4);
        
        addColumn(UserData::phone)
            .setHeader("Telefon")
            .setAutoWidth(true)
            .setFlexGrow(2);
        
        addComponentColumn(user -> new StatusBadge(user.status()))
            .setHeader("Státusz")
            .setAutoWidth(true)
            .setSortable(true)
            .setWidth("100")
            .setFlexGrow(0);
        
        setHeightByRows(true);
        
        UI.getCurrent().getPage().retrieveExtendedClientDetails(e -> updateVisibleColumns(e.getBodyClientWidth()));
        UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> updateVisibleColumns(e.getWidth()));
        refresh();
    }
    
    private static Badge createRoleBadge(Role role) {
        BadgeColor badgeColor = switch(role) {
            case ADMIN -> BadgeColor.ERROR_PRIMARY;
            case MEMBER -> BadgeColor.NORMAL;
            case PARTNER -> BadgeColor.CONTRAST;
            case VISITOR -> BadgeColor.SUCCESS_PRIMARY;
            case ANONYMUS -> BadgeColor.CONTRAST_PRIMARY;
        };
        Badge badge = new Badge(role.label(), badgeColor, BadgeSize.M, BadgeShape.PILL);
        badge.setWidth("80px");
        return badge;
    }
    
    void refresh() {
        setItems(userService.loadAllUsers().list());
    }
    
    private void updateVisibleColumns(int width) {
        boolean mobile = width < MOBILE_BREAKPOINT;
        List<Grid.Column<UserData>> columns = getColumns();

        // "Mobile" column
        columns.get(0).setVisible(mobile);

        // "Desktop" columns
        for (int i = 1; i < columns.size(); i++) {
            columns.get(i).setVisible(!mobile);
        }
    }
    
    /**
     * A layout for displaying User info in a mobile friendly format.
     */
    static class UserDataMobileTemplate extends FlexBoxLayout {

        private UserData userData;

        public UserDataMobileTemplate(UserData userData) {
            this.userData = userData;

            UIUtils.setLineHeight(LineHeight.M, this);
            UIUtils.setPointerEvents(PointerEvents.NONE, this);

            setPadding(Vertical.S);
            setSpacing(Right.L);

            FlexBoxLayout name = getName();
            Label email = getEmail();
            FlexBoxLayout phone = getPhone();

            FlexBoxLayout column = new FlexBoxLayout(name, email, phone);
            column.setFlexDirection(FlexDirection.COLUMN);
            column.setOverflow(Overflow.HIDDEN);

            add(column);
        }

        private FlexBoxLayout getName() {
            Label owner = UIUtils.createLabel(FontSize.M, TextColor.BODY, userData.name());
            UIUtils.setOverflow(Overflow.HIDDEN, owner);
            UIUtils.setTextOverflow(TextOverflow.ELLIPSIS, owner);

            Badge roleBadge = createRoleBadge(userData.role());
            
            FlexBoxLayout wrapper = new FlexBoxLayout(owner, roleBadge);
            wrapper.setAlignItems(Alignment.CENTER);
            wrapper.setFlexGrow(1, owner);
            wrapper.setFlexShrink("0", roleBadge);
            wrapper.setSpacing(Right.M);
            return wrapper;
        }

        private Label getEmail() {
            Label account = UIUtils.createLabel(FontSize.S, TextColor.SECONDARY, userData.email());
            account.addClassNames(LumoStyles.Margin.Bottom.S);
            UIUtils.setOverflow(Overflow.HIDDEN, account);
            UIUtils.setTextOverflow(TextOverflow.ELLIPSIS, account);
            return account;
        }

        private FlexBoxLayout getPhone() {
            Label phone = UIUtils.createH5Label(userData.phone());
            phone.addClassName(LumoStyles.FontFamily.MONOSPACE);

            FlexBoxLayout wrapper = new FlexBoxLayout(phone);
            wrapper.setAlignItems(Alignment.BASELINE);
            return wrapper;
        }
    }

}

