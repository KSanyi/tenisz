package hu.kits.tennis.infrastructure.ui.vaadin.util.css.lumo;

public enum BadgeSize {

    S("small"),
    M("medium");

    private final String style;

    BadgeSize(String style) {
        this.style = style;
    }

    public String getThemeName() {
        return style;
    }

}
