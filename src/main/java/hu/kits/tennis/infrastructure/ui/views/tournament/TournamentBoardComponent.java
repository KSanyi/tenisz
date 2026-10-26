package hu.kits.tennis.infrastructure.ui.views.tournament;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.MathUtil;
import hu.kits.tennis.domain.ktr.KTR;
import hu.kits.tennis.domain.ktr.PlayersWithKTR;
import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.match.MatchResult;
import hu.kits.tennis.domain.match.MatchResult.SetResult;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentBoard;
import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;

@CssImport("./styles/tournament-board.css")
class TournamentBoardComponent extends Div {

    private Tournament tournament;

    private TournamentBoard board;

    private PlayersWithKTR playersWithKTR;

    private final Runnable matchChangeCallback;

    TournamentBoardComponent(Tournament tournament, TournamentBoard board, Runnable matchChangeCallback) {

        this.matchChangeCallback = matchChangeCallback;

        addClassName("tournament-board");

        setBoard(tournament, board);
    }

    void setBoard(Tournament tournament, TournamentBoard board) {

        this.tournament = tournament;
        this.board = board;
        this.playersWithKTR = Main.applicationContext.getPlayersService().loadAllPlayersWithKTR();

        removeAll();

        int rounds = board.numberOfRounds();
        for(int round=1;round<=rounds;round++) {
            add(createRoundColumn(round));
        }
        add(createWinnerColumn());
    }

    private Div createRoundColumn(int round) {
        int rounds = board.numberOfRounds();
        int matchesInRound = MathUtil.pow2(rounds - round);

        Div matches = new Div();
        matches.addClassName("round-matches");

        if(matchesInRound == 1) {
            matches.add(createMatchSlot(round, 1));
        } else {
            for(int i=1;i<=matchesInRound;i+=2) {
                Div pair = new Div(createMatchSlot(round, i), createMatchSlot(round, i+1));
                pair.addClassName("match-pair");
                matches.add(pair);
            }
        }

        Div column = new Div(createRoundHeader(createHeader(rounds, round)), matches);
        column.addClassName("round");
        if(round == 1) {
            column.addClassName("first-round");
        }
        return column;
    }

    private Div createWinnerColumn() {
        Match finalMatch = board.finalMatch();

        Div card = new Div();
        card.addClassName("champion-card");
        if(finalMatch != null && finalMatch.result() != null) {
            Icon trophy = VaadinIcon.TROPHY.create();
            trophy.addClassName("champion-icon");
            Span name = new Span(finalMatch.winner().name());
            name.addClassName("player-name");
            card.add(trophy, name);
        } else {
            card.addClassName("empty");
        }

        Div slot = new Div(card);
        slot.addClassName("match-slot");

        Div matches = new Div(slot);
        matches.addClassName("round-matches");

        Div column = new Div(createRoundHeader(createHeader(board.numberOfRounds(), board.numberOfRounds() + 1)), matches);
        column.addClassNames("round", "winner-round");
        return column;
    }

    private static Div createRoundHeader(String title) {
        Div header = new Div(new Span(title));
        header.addClassName("round-header");
        return header;
    }

    private Div createMatchSlot(int round, int matchNumberInRound) {
        Match match = board.getMatch(round, matchNumberInRound);

        Div card = new Div();
        card.addClassName("match-card");

        Player player1 = match != null ? match.player1() : null;
        Player player2 = match != null ? match.player2() : null;
        MatchResult result = match != null ? match.result() : null;

        card.add(createPlayerRow(player1, result, true), createPlayerRow(player2, result, false));

        if(match != null && VaadinUtil.isUserLoggedIn()) {
            card.addClassName("clickable");
            card.addClickListener(click -> {
                String title = createHeader(board.numberOfRounds(), round) + " meccs " + matchNumberInRound;
                new TournamentMatchDialog(title, match, tournament.params().bestOfNSets(), matchChangeCallback).open();
            });
        }

        Div slot = new Div(card);
        slot.addClassName("match-slot");
        return slot;
    }

    private Div createPlayerRow(Player player, MatchResult result, boolean isPlayer1) {

        Span name = new Span(player != null ? player.name() : "");
        name.addClassName("player-name");

        Span ktr = new Span();
        ktr.addClassName("player-ktr");
        if(player != null) {
            KTR playerKTR = playersWithKTR.getKTR(player.id());
            if(playerKTR.isDefinded()) {
                ktr.setText("KTR " + playerKTR);
            }
        }

        Span statsLink = new Span();
        if(player != null && !player.equals(Player.BYE)) {
            Anchor anchor = new Anchor("/ui/player-stats/" + player.id(), "↗", AnchorTarget.BLANK);
            anchor.addClassName("player-stats-link");
            anchor.getElement().setAttribute("title", "Játékos statisztika");
            anchor.getElement().setAttribute("onclick", "event.stopPropagation()");
            statsLink.add(anchor);
        }

        Div scores = new Div();
        scores.addClassName("scores");

        Div row = new Div(name, ktr, statsLink, scores);
        row.addClassName("player-row");

        if(result != null) {
            boolean isWinner = isPlayer1 ? result.isPlayer1Winner() : result.isPlayer2Winner();
            row.addClassName(isWinner ? "winner" : "loser");

            for(SetResult setResult : result.setResults()) {
                int games = isPlayer1 ? setResult.player1Score() : setResult.player2Score();
                boolean setWon = isPlayer1 ? setResult.isPlayer1Winner() : setResult.isPlayer2Winner();
                Span setScore = new Span(String.valueOf(games));
                setScore.addClassName("set-score");
                if(setWon) {
                    setScore.addClassName("set-won");
                }
                scores.add(setScore);
            }
        }

        return row;
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

}
