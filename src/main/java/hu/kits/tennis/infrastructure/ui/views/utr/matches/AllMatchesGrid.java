package hu.kits.tennis.infrastructure.ui.views.utr.matches;

import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.selection.SelectionEvent;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.StringUtil;
import hu.kits.tennis.domain.match.MatchInfo;
import hu.kits.tennis.domain.match.MatchService;
import hu.kits.tennis.infrastructure.ui.views.utr.MatchesGrid;

class AllMatchesGrid extends MatchesGrid {
    
    private final MatchService matchService;
    
    private ListDataProvider<MatchInfo> dataProvider;
    
    AllMatchesGrid() {
        
        matchService = Main.resourceFactory.getMatchService();
        addSelectionListener(AllMatchesGrid::matchSelected);
        
        refresh();
    }
    
    private static void matchSelected(SelectionEvent<Grid<MatchInfo>, MatchInfo> e) {
        //e.getFirstSelectedItem().ifPresent(match -> new MatchDialog(new MatchDataBean(match.playedMatch())).open());
    }

    void refresh() {
        List<MatchInfo> entries = matchService.loadAllMatches();
        dataProvider = new ListDataProvider<>(entries);
        setItems(dataProvider);
        
        int indexOfFirstPlayedMatch = entries.stream().filter(m -> m.result() != null).findFirst().map(entries::indexOf).orElse(0);
        scrollToIndex(indexOfFirstPlayedMatch);
    }

    void filter(String filterText) {
        dataProvider.clearFilters();
        String[] filterParts = StringUtil.cleanNameString(filterText).split(" ");
        Stream.of(filterParts).forEach(filterPart -> dataProvider.addFilter(match -> match.matches(filterPart)));
    }
    
}

