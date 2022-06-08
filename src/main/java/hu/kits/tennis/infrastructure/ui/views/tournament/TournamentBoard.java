package hu.kits.tennis.infrastructure.ui.views.tournament;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.ItemClickEvent;

import hu.kits.tennis.domain.utr.MatchResult;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.infrastructure.ui.views.tournament.TournamentBoard.Row;

@CssImport(value = "./styles/tournament-board.css", themeFor = "vaadin-grid")
class TournamentBoard extends Grid<Row> {

    private static final int[] twoPows = new int[] {1, 2, 4, 8, 16, 32, 64, 128, 256};
    
    private final int rounds;
    
    private final List<Row> rows = new ArrayList<>();
    
    TournamentBoard(int rounds) {
        
        this.rounds = rounds;
        
        for(int i=1;i<=rounds+1;i++) {
            int round = i;
            
            addColumn(row -> row.values.get(round-1) != null ? row.values.get(round-1).player.name() : null)
                .setKey(String.valueOf(round))
                .setHeader(createHeader(rounds, round))
                .setTextAlign(ColumnTextAlign.CENTER)
                .setFlexGrow(1)
                .setClassNameGenerator(row -> createCellStyle(round, row.rowNum));
        }
        
        for(int i=1;i<=pow2(rounds+1)-1;i++) {
            rows.add(new Row(rounds, i));
        }
        
        setItems(rows);
        
        setAllRowsVisible(true);
        
        setSelectionMode(SelectionMode.NONE);
        
        addItemClickListener(e -> itemClicked(e));
        
        
        addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS);
        addThemeVariants(GridVariant.LUMO_COMPACT);
    }
    
    private void itemClicked(ItemClickEvent<Row> e) {
        
        Row row = e.getItem();
        String key = e.getColumn().getKey();
        int round = Integer.parseInt(key);
        
        if(round > 1 && isMatchCell(round, row.rowNum)) {
            PlayerWithResult playerWithResult1 = rows.get(row.rowNum - 1 - pow2(round-2)).values.get(round - 2);
            PlayerWithResult playerWithResult2 = rows.get(row.rowNum - 1 + pow2(round-2)).values.get(round - 2);
            if(playerWithResult1 != null && playerWithResult2 != null) {
                new MatchDialog(playerWithResult1.player, playerWithResult2.player).open();
            }
        }
        
    }

    private static boolean isMatchCell(int round, int rowNum) {
        return (rowNum) % pow2(round) == pow2(round-1);
    }

    private static int pow2(int x) {
        return twoPows[x];
    }
    
    private static String createCellStyle(int round, int rowNum) {
        int startRow = pow2(round-1) - 1;
        
        int rowsBetweenMatches = pow2(round + 1);
        int x = (rowNum - startRow) % rowsBetweenMatches;
        
        if(isMatchCell(round, rowNum)) {
            return "playerCell";
        } else if(x <= rowsBetweenMatches/2 && x > 0) {
            return null;
        } else {
            return "emptyCell";
        }
    }

    void setPlayer(int round, int match, int playerNumber, PlayerWithResult playerWithResult) {
        int startRow = pow2(round-1) - 1;
        
        int rowsBetweenMatches = pow2(round + 1);
        int rowsBetweenPlayers = pow2(round);
        
        int rowNumber = startRow + (match-1) * rowsBetweenMatches + (playerNumber-1) * rowsBetweenPlayers;
        rows.get(rowNumber).values.set(round-1, playerWithResult);
    }
    
    private static String createHeader(int rounds, int round) {
        if(round == rounds + 1) {
            return "Winner";
        } else if(round == rounds) {
            return "Final";
        } else if(round == rounds - 1) {
            return "Semifinal";
        } else if(round == rounds - 2) {
            return "Quarterfinal";
        } else {
            return "Round " + round;
        }
    }
    
    static class Row {
        
        final int rowNum;
        final List<PlayerWithResult> values;
        
        public Row(int rounds, int rowNum) {
            this.rowNum = rowNum;
            values = new ArrayList<>();
            for(int i=0;i<=rounds;i++) {
                values.add(null);
            }
        }

    }
    
    static record PlayerWithResult(Player player, MatchResult result) {
        
    }

}
