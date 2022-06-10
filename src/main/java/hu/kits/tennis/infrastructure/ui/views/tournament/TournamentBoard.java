package hu.kits.tennis.infrastructure.ui.views.tournament;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.ItemClickEvent;

import hu.kits.tennis.common.Pair;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.utr.Match;
import hu.kits.tennis.domain.utr.MatchResult;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.infrastructure.ui.views.tournament.TournamentBoard.Row;

@CssImport(value = "./styles/tournament-board.css", themeFor = "vaadin-grid")
class TournamentBoard extends Grid<Row> {

    private static final int[] twoPows = new int[] {1, 2, 4, 8, 16, 32, 64, 128, 256};
    
    private final Tournament tournament;
    
    private final List<Row> rows = new ArrayList<>();
    
    private final Consumer<Pair<Match, MatchResult>> matchResultSetCallback;
    
    TournamentBoard(Tournament tournament, Consumer<Pair<Match, MatchResult>> matchResultSetCallback) {
        
        this.tournament = tournament;
        this.matchResultSetCallback = matchResultSetCallback;
        
        int rounds = tournament.numberOfRounds();
        
        for(int i=1;i<=rounds+1;i++) {
            int round = i;
            
            addColumn(row -> playerNameAndResult(row, round - 1))
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
        
        for(Match match : tournament.matches().values()) {
            Player player1 = match.player1();
            Player player2 = match.player2();
            var roundAndMatchNumberInRound = tournament.roundAndMatchNumberInRound(match.tournamentMatchNumber());
            int round = roundAndMatchNumberInRound.getFirst();
            int matchNumberInRound = roundAndMatchNumberInRound.getSecond();
            
            Optional<Match> prevMatchForPlayer1 = tournament.findPrevMatch(match, player1);
            MatchResult prevMatchResultForPlayer1 = prevMatchForPlayer1.map(Match::result).orElse(null);
            Optional<Match> prevMatchForPlayer2 = tournament.findPrevMatch(match, player2);
            MatchResult prevMatchResultForPlayer2 = prevMatchForPlayer2.map(Match::result).orElse(null);
            
            setPlayer(round, matchNumberInRound, 1, new PlayerWithResult(player1, prevMatchResultForPlayer1));
            setPlayer(round, matchNumberInRound, 2, new PlayerWithResult(player2, prevMatchResultForPlayer2));
        }
        
        setAllRowsVisible(true);
        
        setSelectionMode(SelectionMode.NONE);
        
        addItemClickListener(e -> itemClicked(e));
        
        addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS);
        addThemeVariants(GridVariant.LUMO_COMPACT);
    }
    
    private static String playerNameAndResult(Row row, int round) {
        PlayerWithResult playerWithResult = row.values.get(round);
        if(playerWithResult != null && playerWithResult.player != null) {
            return playerWithResult.player.name() + (playerWithResult.result() != null ? (" " + playerWithResult.result()) : ""); 
        } else {
            return null;
        }
    }
    
    private void itemClicked(ItemClickEvent<Row> e) {
        
        Row row = e.getItem();
        String key = e.getColumn().getKey();
        int round = Integer.parseInt(key) - 1;
        
        if(round > 0 && isMatchCell(round, row.rowNum)) {
            int matchNumberInRound = row.rowNum / pow2(round+1) + 1;
            Match match = tournament.getMatch(round, matchNumberInRound);
            if(match != null && match.arePlayersSet()) {
                String title = createHeader(tournament.numberOfRounds(), round) + " meccs " + matchNumberInRound;
                new MatchDialog(title, match, tournament.bestOfNSets(), matchResultSetCallback).open();
            }
        }
        
    }

    private static boolean isMatchCell(int round, int rowNum) {
        return (rowNum) % pow2(round+1) == pow2(round);
    }

    private static int pow2(int x) {
        return twoPows[x];
    }
    
    private static String createCellStyle(int round, int rowNum) {
        int startRow = pow2(round-1) - 1;
        
        int rowsBetweenMatches = pow2(round + 1);
        int x = (rowNum - startRow) % rowsBetweenMatches;
        
        if(isMatchCell(round-1, rowNum)) {
            return "playerCell";
        } else if(x <= rowsBetweenMatches/2 && x > 0) {
            return null;
        } else {
            return "emptyCell";
        }
    }

    private void setPlayer(int round, int match, int playerNumber, PlayerWithResult playerWithResult) {
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
