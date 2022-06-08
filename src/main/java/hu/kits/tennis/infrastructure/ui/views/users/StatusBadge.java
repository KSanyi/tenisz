package hu.kits.tennis.infrastructure.ui.views.users;

import hu.kits.tennis.domain.user.UserData.Status;
import hu.kits.tennis.infrastructure.ui.vaadin.components.Badge;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo.BadgeColor;

class StatusBadge extends Badge {

    StatusBadge(Status status) {
        super(status.label());
        BadgeColor badgeColor = switch(status) {
            case ACTIVE -> BadgeColor.SUCCESS;
            case INACTIVE -> BadgeColor.ERROR;
            case REGISTERED -> BadgeColor.NORMAL;
            default -> BadgeColor.NORMAL;
        };
        
        UIUtils.setTheme(badgeColor.getThemeName(), this);
        UIUtils.setTooltip(status.description(), this);
        setWidth("80px");
    }

}
