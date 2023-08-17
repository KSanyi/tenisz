package hu.kits.tennis.infrastructure.ui.views.ktr.matches;

import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.data.provider.ListDataProvider;

import hu.kits.tennis.common.StringUtil;
import hu.kits.tennis.domain.match.MatchInfo;
import hu.kits.tennis.infrastructure.ui.views.ktr.MatchesGridMobile;

class AllMatchesGridMobile extends MatchesGridMobile {
    
    private ListDataProvider<MatchInfo> dataProvider;
    
    void setMatches(List<MatchInfo> matches) {
        dataProvider = new ListDataProvider<>(matches);
        setItems(dataProvider);
        
        int indexOfFirstPlayedMatch = matches.stream().filter(m -> m.result() != null).findFirst().map(matches::indexOf).orElse(0);
        scrollToIndex(indexOfFirstPlayedMatch);
    }

    void filter(String filterText) {
        dataProvider.clearFilters();
        String[] filterParts = StringUtil.cleanNameString(filterText).split(" ");
        Stream.of(filterParts).forEach(filterPart -> dataProvider.addFilter(match -> match.matches(filterPart)));
    }
    
}

