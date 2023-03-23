package hu.kits.tennis.infrastructure.ui.views.tournament;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.ItemClickEvent;

import hu.kits.tennis.common.MathUtil;
import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.match.MatchResult;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.Tournament.Board;
import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;
import hu.kits.tennis.infrastructure.ui.views.tournament.TournamentBoard.Row;

@CssImport(value = "./styles/tournament-board.css", themeFor = "vaadin-grid")
class TournamentBoard extends Grid<Row> {

    private Tournament tournament;
    
    private Board board;
    
    private final List<Row> rows = new ArrayList<>();
    
    private final Runnable matchChangeCallback;
    
    TournamentBoard(Tournament tournament, Board board, Runnable matchChangeCallback) {
        
        this.tournament = tournament;
        this.board = board;
        this.matchChangeCallback = matchChangeCallback;
        
        setBoard(board);
        
        setAllRowsVisible(true);
        
        setSelectionMode(SelectionMode.NONE);
        
        addItemClickListener(e -> itemClicked(e));
        
        addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS);
        addThemeVariants(GridVariant.LUMO_COMPACT);
    }
    
    private void setBoard(Board board) {
        this.board = board;
        int rounds = board.numberOfRounds();
        
        this.removeAllColumns();
        rows.clear();
        
        for(int i=1;i<=rounds+1;i++) {
            int round = i;
            
            addColumn(row -> playerNameAndResult(row, round - 1))
                .setKey(String.valueOf(round))
                .setHeader(createHeader(rounds, round))
                .setTextAlign(ColumnTextAlign.CENTER)
                .setFlexGrow(1)
                .setWidth(i == 1 ? "200px" : "250px")
                .setClassNameGenerator(row -> createCellStyle(round, row.rowNum));
        }
        
        for(int i=1;i<=MathUtil.pow2(rounds+1)-1;i++) {
            rows.add(new Row(rounds, i));
        }
    }
    
    private PlayerWithResult findPlayerWithResult(Match match, Player player) {
        if(player == null) {
            return new PlayerWithResult(null, null);
        }
        
        Optional<Match> prevMatchForPlayer = board.findPrevMatch(match, player);
        MatchResult prevMatchResultForPlayer = prevMatchForPlayer.map(Match::result).orElse(null);
        return new PlayerWithResult(player, prevMatchResultForPlayer);
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
        
        if(VaadinUtil.isUserLoggedIn()) {
            Row row = e.getItem();
            String key = e.getColumn().getKey();
            int round = Integer.parseInt(key) - 1;
            
            if(round > 0 && isMatchCell(round, row.rowNum)) {
                int matchNumberInRound = row.rowNum / MathUtil.pow2(round+1) + 1;
                Match match = board.getMatch(round, matchNumberInRound);
                if(match != null) {
                    String title = createHeader(board.numberOfRounds(), round) + " meccs " + matchNumberInRound;
                    new TournamentMatchDialog(title, match, tournament.bestOfNSets(), matchChangeCallback).open();
                }
            } 
        }
    }

    private static boolean isMatchCell(int round, int rowNum) {
        return (rowNum) % MathUtil.pow2(round+1) == MathUtil.pow2(round);
    }

    private static String createCellStyle(int round, int rowNum) {
        int startRow = MathUtil.pow2(round-1) - 1;
        
        int rowsBetweenMatches = MathUtil.pow2(round + 1);
        int x = (rowNum - startRow) % rowsBetweenMatches;
        
        if(isMatchCell(round-1, rowNum)) {
            return "playerCell";
        } else if(x <= rowsBetweenMatches/2 && x > 0) {
            return null;
        } else {
            return "emptyCell";
        }
    }
    
    void setBoard(Tournament tournament, Board board) {
        
        this.tournament = tournament;
        setBoard(board);
        
        for(Match match : board.matches().values()) {
            Player player1 = match.player1();
            Player player2 = match.player2();
            
            var roundAndMatchNumberInRound = MathUtil.roundAndMatchNumberInRound(match.tournamentMatchNumber(), board.numberOfRounds());
            int round = roundAndMatchNumberInRound.first();
            int matchNumberInRound = roundAndMatchNumberInRound.second();
            
            setPlayer(round, matchNumberInRound, 1, findPlayerWithResult(match, player1));
            setPlayer(round, matchNumberInRound, 2, findPlayerWithResult(match, player2));
        }
        
        Match finalMatch = board.finalMatch();
        
        if(finalMatch != null && finalMatch.result() != null) {
            setPlayer(board.numberOfRounds() + 1, 1, 1, new PlayerWithResult(finalMatch.winner(), finalMatch.result()));
        }
        
        setItems(rows);
    }

    private void setPlayer(int round, int match, int playerNumber, PlayerWithResult playerWithResult) {
        int startRow = MathUtil.pow2(round-1) - 1;
        
        int rowsBetweenMatches = MathUtil.pow2(round + 1);
        int rowsBetweenPlayers = MathUtil.pow2(round);
        
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
        
        Row(int rounds, int rowNum) {
            this.rowNum = rowNum;
            values = new ArrayList<>();
            for(int i=0;i<=rounds;i++) {
                values.add(null);
            }
        }
        
        void clear() {
            values.replaceAll(p -> null);
        }

    }
    
    static record PlayerWithResult(Player player, MatchResult result) {
        
    }

}
