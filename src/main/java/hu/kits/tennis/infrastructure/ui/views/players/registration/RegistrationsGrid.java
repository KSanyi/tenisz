package hu.kits.tennis.infrastructure.ui.views.players.registration;

import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;

import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.common.StringUtil;
import hu.kits.tennis.domain.player.registration.Registration;

class RegistrationsGrid extends Grid<Registration> {
    
    private static final int MOBILE_BREAKPOINT = 800;
    
    RegistrationsGrid(List<Registration> registrations) {
        
        addColumn(r -> Formatters.formatDateTime(r.timestamp()))
            .setHeader("Idő")
            .setSortable(true)
            .setFlexGrow(2);
        
        addColumn(r -> r.data().name())
            .setHeader("Név")
            .setSortable(true)
            .setComparator((r1, r2) -> StringUtil.HUN_COLLATOR.compare(r1.data().name(), r2.data().name()))
            .setFlexGrow(3);
        
        addColumn(r -> r.data().email())
            .setHeader("Email")
            .setSortable(true)
            .setTooltipGenerator(r -> r.data().email())
            .setFlexGrow(2);
        
        addColumn(r -> r.data().phone())
            .setHeader("Telefonszám")
            .setSortable(true)
            .setFlexGrow(2);
        
        addColumn(r -> r.data().addressString())
            .setHeader("Cím")
            .setSortable(true)
            .setTooltipGenerator(r -> r.data().addressString())
            .setFlexGrow(3);
        
        addColumn(r -> r.data().experience())
            .setHeader("Mióta játszik?")
            .setSortable(true)
            .setFlexGrow(2);
        
        addColumn(r -> r.data().playFrequency())
            .setHeader("Hányszor játszik?")
            .setSortable(true)
            .setFlexGrow(2);
        
        addColumn(r -> r.status())
            .setHeader("Státusz")
            .setSortable(true)
            .setFlexGrow(1);
        
        UI.getCurrent().getPage().retrieveExtendedClientDetails(e -> updateVisibleColumns(e.getBodyClientWidth()));
        UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> updateVisibleColumns(e.getWidth()));
        
        setItems(registrations);
    }

    private void updateVisibleColumns(int width) {
        boolean mobile = width < MOBILE_BREAKPOINT;
        List<Grid.Column<Registration>> columns = getColumns();

        columns.get(2).setVisible(!mobile);
        columns.get(3).setVisible(!mobile);
        columns.get(6).setVisible(!mobile);
        columns.get(7).setVisible(!mobile);
    }

}

