package hu.kits.tennis.infrastructure.ui.views.ktr.head2head;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import hu.kits.tennis.domain.match.Head2HeadData;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.views.ktr.MatchesGrid;

class Head2HeadMatchesPanel extends VerticalLayout {

    private final Label titleLabel = UIUtils.createH3Label("");
    private final MatchesGrid matchesGrid = new MatchesGrid();

    Head2HeadMatchesPanel() {
        matchesGrid.setSizeFull();

        setPadding(false);
        setSpacing(false);
        setSizeFull();
        add(titleLabel, matchesGrid);
    }

    void setHead2HeadData(Head2HeadData data) {
        titleLabel.setText(data.player1().name() + " vs " + data.player2().name()
                + "  (" + data.player1Wins() + " - " + data.player2Wins() + ")");
        matchesGrid.setItems(data.matches());
    }
}
