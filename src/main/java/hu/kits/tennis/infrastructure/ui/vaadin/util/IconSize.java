package hu.kits.tennis.infrastructure.ui.vaadin.util;

public enum IconSize {

    S("size-s"), 
    M("size-m"), 
    L("size-l");

    private final String style;

    IconSize(String style) {
        this.style = style;
    }

    public String getClassName() {
        return style;
    }

}
