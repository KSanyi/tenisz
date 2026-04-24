package hu.kits.tennis.infrastructure.ui.views.ktr.head2head;

import java.util.List;

import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;

import hu.kits.tennis.common.StringUtil;
import hu.kits.tennis.domain.match.Head2HeadData;

class Head2HeadGrid extends Grid<Head2HeadData> {

    private ListDataProvider<Head2HeadData> dataProvider;

    Head2HeadGrid() {
        addColumn(h2h -> h2h.player1().name())
            .setHeader("Játékos 1")
            .setClassNameGenerator(h2h -> h2h.player1Wins() > h2h.player2Wins() ? "bold" : "")
            .setFlexGrow(3)
            .setSortable(true);

        addColumn(h2h -> h2h.player1Wins() + " - " + h2h.player2Wins())
            .setHeader("Eredmény")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setWidth("100px")
            .setFlexGrow(0);

        addColumn(h2h -> h2h.player2().name())
            .setHeader("Játékos 2")
            .setClassNameGenerator(h2h -> h2h.player2Wins() > h2h.player1Wins() ? "bold" : "")
            .setFlexGrow(3)
            .setSortable(true);

        addColumn(Head2HeadData::numberOfMatches)
            .setHeader("Meccsek")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setWidth("90px")
            .setFlexGrow(0)
            .setSortable(true);

        setWidthFull();
        setHeightFull();
        setMinWidth("450px");
    }

    void setData(List<Head2HeadData> items) {
        dataProvider = new ListDataProvider<>(items);
        setItems(dataProvider);
    }

    void filter(String filterText) {
        dataProvider.clearFilters();
        String[] parts = StringUtil.cleanNameString(filterText).split(" ");
        for (String part : parts) {
            if (!part.isEmpty()) {
                dataProvider.addFilter(h2h ->
                    StringUtil.cleanNameString(h2h.player1().name()).contains(part) ||
                    StringUtil.cleanNameString(h2h.player2().name()).contains(part));
            }
        }
    }
}
