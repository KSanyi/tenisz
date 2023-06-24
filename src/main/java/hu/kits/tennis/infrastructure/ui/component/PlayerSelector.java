package hu.kits.tennis.infrastructure.ui.component;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.data.value.ValueChangeMode;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.StringUtil;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.utr.PlayerWithUTR;

public class PlayerSelector extends VerticalLayout {

    private final TextField filter = new TextField();
    private final PlayersGrid playersGrid;
    private final Consumer<Player> callBack;
    
    public PlayerSelector(Consumer<Player> callBack) {
        this(callBack, Main.applicationContext.getPlayersService().loadAllPlayersWithUTR().entries());
    }
    
    public PlayerSelector(Consumer<Player> callBack, List<PlayerWithUTR> players) {
        
        setPadding(false);
        setMargin(false);
        
        this.callBack = callBack;
        playersGrid = new PlayersGrid(players);
        
        add(filter, playersGrid);
        setHorizontalComponentAlignment(Alignment.CENTER, filter);
        
        filter.addValueChangeListener(v -> playersGrid.filter(v.getValue()));
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        
        playersGrid.addItemClickListener(e -> clicked(e));
        
        setWidthFull();
        
        filter.focus();
    }
    
    private void clicked(ItemClickEvent<PlayerWithUTR> e) {
        if(e.getClickCount() > 1) {
            callBack.accept(e.getItem().player());
        }
    }

    public void addSelectionListener(SelectionListener<Grid<PlayerWithUTR>, PlayerWithUTR> listener) {
        playersGrid.addSelectionListener(listener);
    }
    
    private static class PlayersGrid extends Grid<PlayerWithUTR> {

        private ListDataProvider<PlayerWithUTR> dataProvider;
        
        public PlayersGrid(List<PlayerWithUTR> players) {
            addColumn(p -> p.player().id())
                .setHeader("Id")
                .setSortable(true)
                .setFlexGrow(0);
            
            addColumn(p -> p.player().name())
                .setHeader("Név")
                .setSortable(true)
                .setFlexGrow(3);
            
            addColumn(p -> p.utr())
                .setHeader("UTR")
                .setSortable(true)
                .setFlexGrow(0);
            
            dataProvider = new ListDataProvider<>(players);
            setItems(dataProvider);
            
            this.addThemeVariants(GridVariant.LUMO_COMPACT);
            setWidthFull();
        }

        void filter(String value) {
            dataProvider.clearFilters();
            String[] filterParts = StringUtil.cleanNameString(value).split(" ");
            Stream.of(filterParts)
                .forEach(filterPart -> dataProvider.addFilter(p -> p.player().matches(filterPart)));
        }
        
    }

}
