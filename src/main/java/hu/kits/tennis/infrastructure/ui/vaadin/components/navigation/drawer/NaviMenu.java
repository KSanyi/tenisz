package hu.kits.tennis.infrastructure.ui.vaadin.components.navigation.drawer;

import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.icon.VaadinIcon;

import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;

@CssImport("./styles/components/navi-menu.css")
public class NaviMenu extends Nav {

    private String CLASS_NAME = "navi-menu";
    private UnorderedList list;

    public NaviMenu() {
        setClassName(CLASS_NAME);
        list = new UnorderedList();
        add(list);
    }

    protected void addNaviItem(NaviItem item) {
        list.add(item);
    }

    protected void addNaviItem(NaviItem parent, NaviItem item) {
        parent.addSubItem(item);
        addNaviItem(item);
    }

    public void filter(String filter) {
        getNaviItems().forEach(naviItem -> {
            boolean matches = ((NaviItem) naviItem).getText().toLowerCase().contains(filter.toLowerCase());
            naviItem.setVisible(matches);
        });
    }

    public NaviItem addNaviItem(VaadinIcon icon, String text, Class<? extends Component> navigationTarget) {
        NaviItem item = new NaviItem(icon, text, navigationTarget);
        addNaviItem(item);
        return item;
    }

    public NaviItem addNaviItem(NaviItem parent, String text, Class<? extends Component> navigationTarget) {
        NaviItem item = new NaviItem(text, navigationTarget);
        addNaviItem(parent, item);
        return item;
    }

    public List<NaviItem> getNaviItems() {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<NaviItem> items = (List) list.getChildren().collect(Collectors.toList());
        return items;
    }

    public void refresh() {
        for(NaviItem naviItem : getNaviItems()) {
            if(naviItem.getNavigationTarget() != null) {
                naviItem.setVisible(VaadinUtil.isViewAllowed(naviItem.getNavigationTarget()));    
            } else {
                naviItem.setVisible(naviItem.getSubItems().stream().anyMatch(subItem -> VaadinUtil.isViewAllowed(subItem.getNavigationTarget())));
            }
        }
    }
    
}
