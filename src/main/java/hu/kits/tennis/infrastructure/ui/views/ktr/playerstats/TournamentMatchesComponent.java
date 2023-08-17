package hu.kits.tennis.infrastructure.ui.views.ktr.playerstats;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;

import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.domain.match.MatchInfo;
import hu.kits.tennis.domain.tournament.BasicTournamentInfo;

class TournamentMatchesComponent extends Div {

    void setMatches(List<MatchInfo> matches) {
        
        removeAll();
        
        Map<BasicTournamentInfo, List<MatchInfo>> matchesByTournaments = matches.stream().collect(Collectors.groupingBy(m -> m.tournamentInfo()));
        List<BasicTournamentInfo> tournamentsSorted = matchesByTournaments.keySet().stream().sorted(Comparator.comparing(BasicTournamentInfo::date).reversed()).toList();
        
        for(var tournament : tournamentsSorted) {
            TournamentMatchesGrid matchesGrid = new TournamentMatchesGrid();
            matchesGrid.setItems(matchesByTournaments.get(tournament));
            matchesGrid.setAllRowsVisible(true);
            add(new H4(tournament.name() + " " + Formatters.formatDateLong(tournament.date())), matchesGrid);
        }
    }
    
}
