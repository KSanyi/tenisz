package hu.kits.tennis.infrastructure.ui.views.utr.ranking;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.TemplateRenderer;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.common.StringUtil;
import hu.kits.tennis.domain.utr.MatchInfo;
import hu.kits.tennis.domain.utr.MatchService;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.infrastructure.ui.views.utr.MatchesGrid;

@CssImport(themeFor = "vaadin-grid", value = "./styles/match-grid.css")
public class PlayerMatchesGrid extends MatchesGrid {
    
    private final MatchService matchService;
    
    private ListDataProvider<MatchInfo> dataProvider;
    
    private Player player;
    
    public PlayerMatchesGrid() {
        
        matchService = Main.resourceFactory.getMatchService();
    }
    
    void setPlayer(Player player) {
        this.player = player;
        refresh();
    }
    
    void refresh() {
        List<MatchInfo> entries = matchService.loadMatchesForPlayer(player).stream()
                .sorted(comparing((MatchInfo matchInfo) -> matchInfo.date()).reversed())
                .collect(toList());
        dataProvider = new ListDataProvider<>(entries);
        setItems(dataProvider);
    }

    void filter(String filterText) {
        dataProvider.clearFilters();
        String[] filterParts = StringUtil.cleanNameString(filterText).split(" ");
        Stream.of(filterParts).forEach(filterPart -> dataProvider.addFilter(match -> match.matches(filterPart)));
    }
    
}