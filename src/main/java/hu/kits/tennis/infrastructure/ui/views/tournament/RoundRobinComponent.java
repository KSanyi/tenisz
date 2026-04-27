package hu.kits.tennis.infrastructure.ui.views.tournament;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.tournament.RoundRobinStanding;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;

class RoundRobinComponent extends VerticalLayout {

    private final Runnable changeCallback;
    private Tournament tournament;

    RoundRobinComponent(Tournament tournament, Runnable changeCallback) {
        this.tournament = tournament;
        this.changeCallback = changeCallback;
        setPadding(false);
        setSpacing(true);
        setSizeFull();
        rebuild();
    }

    void setTournament(Tournament tournament) {
        this.tournament = tournament;
        rebuild();
    }

    private void rebuild() {
        removeAll();

        List<Player> players = tournament.contestants().stream()
                .map(c -> c.player())
                .filter(p -> !p.equals(Player.BYE))
                .toList();

        List<Match> matches = tournament.matches();

        Map<String, Match> matchLookup = buildMatchLookup(matches);

        add(buildStandingsGrid(players, matches));
        if (!players.isEmpty() && !matches.isEmpty()) {
            add(buildMatchMatrix(players, matchLookup));
        }
    }

    private static Map<String, Match> buildMatchLookup(List<Match> matches) {
        Map<String, Match> lookup = new HashMap<>();
        for (Match m : matches) {
            if (m.player1() != null && m.player2() != null) {
                lookup.put(m.player1().id() + "_" + m.player2().id(), m);
                lookup.put(m.player2().id() + "_" + m.player1().id(), m);
            }
        }
        return lookup;
    }

    private static Grid<RoundRobinStanding> buildStandingsGrid(List<Player> players, List<Match> matches) {
        Grid<RoundRobinStanding> grid = new Grid<>();
        grid.addColumn(RoundRobinStanding::rank)
                .setHeader("#").setWidth("50px")
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.CENTER);
        grid.addColumn(s -> s.player().name())
                .setHeader("Játékos").setFlexGrow(2);
        grid.addColumn(s -> s.wins() + " - " + s.losses())
                .setHeader("Győzelem - Vereség")
                .setWidth("170px")
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.CENTER);
        grid.addColumn(s -> s.setsWon() + " - " + s.setsLost())
                .setHeader("Szett")
                .setWidth("90px")
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.CENTER);
        grid.addColumn(s -> s.gamesWon() + " - " + s.gamesLost())
                .setHeader("Game")
                .setWidth("90px")
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.CENTER);
        
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setAllRowsVisible(true);
        grid.setWidthFull();
        grid.setItems(RoundRobinStanding.calculate(players, matches));
        
        return grid;
    }

    private Grid<Player> buildMatchMatrix(List<Player> players, Map<String, Match> matchLookup) {
        Grid<Player> grid = new Grid<>();

        grid.addColumn(Player::name)
                .setHeader("")
                .setFrozen(true)
                .setWidth("180px")
                .setFlexGrow(0);

        for (Player opponent : players) {
            grid.addComponentColumn(rowPlayer -> createMatchCell(rowPlayer, opponent, matchLookup))
                    .setHeader(opponent.name())
                    .setTextAlign(ColumnTextAlign.CENTER)
                    .setWidth("150px")
                    .setFlexGrow(0)
                    .setKey(String.valueOf(opponent.id()));
        }

        grid.setItems(players);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setAllRowsVisible(true);
        grid.setWidthFull();
        return grid;
    }

    private Component createMatchCell(Player rowPlayer, Player colPlayer, Map<String, Match> matchLookup) {
        if (rowPlayer.equals(colPlayer)) {
            Span dash = new Span("—");
            dash.getStyle().set("color", "var(--lumo-contrast-30pct)");
            return dash;
        }

        Match match = matchLookup.get(rowPlayer.id() + "_" + colPlayer.id());
        if (match == null) {
            return new Span("");
        }

        String label;
        String color = null;
        if (match.result() == null) {
            label = "·";
        } else {
            boolean rowIsPlayer1 = rowPlayer.equals(match.player1());
            label = rowIsPlayer1 ? match.result().toString() : match.result().swap().toString();
            boolean rowWon = rowIsPlayer1 ? match.result().isPlayer1Winner() : match.result().isPlayer2Winner();
            color = rowWon ? "var(--lumo-success-text-color)" : "var(--lumo-error-text-color)";
        }

        if (VaadinUtil.isUserLoggedIn()) {
            Button btn = new Button(label);
            btn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            if (color != null) btn.getStyle().set("color", color);
            btn.addClickListener(e -> {
                String title = match.player1().name() + " vs " + match.player2().name();
                new TournamentMatchDialog(title, match, tournament.params().bestOfNSets(), changeCallback).open();
            });
            return btn;
        } else {
            Span span = new Span(label);
            if (color != null) span.getStyle().set("color", color);
            return span;
        }
    }
}
