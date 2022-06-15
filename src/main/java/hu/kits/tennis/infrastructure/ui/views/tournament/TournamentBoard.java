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

import hu.kits.tennis.common.MathUtil;
import hu.kits.tennis.common.Pair;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.Tournament.Board;
import hu.kits.tennis.domain.utr.Match;
import hu.kits.tennis.domain.utr.MatchResult;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.infrastructure.ui.views.tournament.TournamentBoard.Row;

@CssImport(value = "./styles/tournament-board.css", themeFor = "vaadin-grid")
class TournamentBoard extends Grid<Row> {

    private final Tournament tournament;
    
    private final Board board;
    
    private final List<Row> rows = new ArrayList<>();
    
    private final Consumer<Pair<Match, MatchResult>> matchResultSetCallback;
    
    TournamentBoard(Tournament tournament, Board board, Consumer<Pair<Match, MatchResult>> matchResultSetCallback) {
        
        this.tournament = tournament;
        this.board = board;
        this.matchResultSetCallback = matchResultSetCallback;
        
        int rounds = board.numberOfRounds();
        
        for(int i=1;i<=rounds+1;i++) {
            int round = i;
            
            addColumn(row -> playerNameAndResult(row, round - 1))
                .setKey(String.valueOf(round))
                .setHeader(createHeader(rounds, round))
                .setTextAlign(ColumnTextAlign.CENTER)
                .setFlexGrow(1)
                .setClassNameGenerator(row -> createCellStyle(round, row.rowNum));
        }
        
        for(int i=1;i<=MathUtil.pow2(rounds+1)-1;i++) {
            rows.add(new Row(rounds, i));
        }
        
        setItems(rows);
        
        for(Match match : board.matches().values()) {
            Player player1 = match.player1();
            Player player2 = match.player2();
            
            var roundAndMatchNumberInRound = MathUtil.roundAndMatchNumberInRound(match.tournamentMatchNumber(), board.numberOfRounds());
            int round = roundAndMatchNumberInRound.getFirst();
            int matchNumberInRound = roundAndMatchNumberInRound.getSecond();
            
            setPlayer(round, matchNumberInRound, 1, findPlayerWithResult(match, player1));
            setPlayer(round, matchNumberInRound, 2, findPlayerWithResult(match, player2));;
        }
        
        Match finalMatch = board.finalMatch();
        
        if(finalMatch != null && finalMatch.result() != null) {
            setPlayer(board.numberOfRounds(), 1, 1, new PlayerWithResult(finalMatch.winner(), finalMatch.result()));
        }
        
        setAllRowsVisible(true);
        
        setSelectionMode(SelectionMode.NONE);
        
        addItemClickListener(e -> itemClicked(e));
        
        addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS);
        addThemeVariants(GridVariant.LUMO_COMPACT);
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
        
        Row row = e.getItem();
        String key = e.getColumn().getKey();
        int round = Integer.parseInt(key) - 1;
        
        if(round > 0 && isMatchCell(round, row.rowNum)) {
            int matchNumberInRound = row.rowNum / MathUtil.pow2(round+1) + 1;
            Match match = board.getMatch(round, matchNumberInRound);
            if(match != null && match.arePlayersSet()) {
                String title = createHeader(board.numberOfRounds(), round) + " meccs " + matchNumberInRound;
                new MatchDialog(title, match, tournament.bestOfNSets(), matchResultSetCallback).open();
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
