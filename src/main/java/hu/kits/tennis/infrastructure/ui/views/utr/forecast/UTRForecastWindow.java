package hu.kits.tennis.infrastructure.ui.views.utr.forecast;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.LitRenderer;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.match.MatchResult;
import hu.kits.tennis.domain.match.MatchService;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.utr.BookedMatch;
import hu.kits.tennis.domain.utr.PlayerWithUTR;
import hu.kits.tennis.domain.utr.UTR;
import hu.kits.tennis.domain.utr.UTRCalculator;
import hu.kits.tennis.domain.utr.UTRForecastResult;
import hu.kits.tennis.infrastructure.ui.component.MatchScoreField;
import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;
import hu.kits.tennis.infrastructure.ui.vaadin.util.TextColor;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

public class UTRForecastWindow extends Dialog {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final MatchService matchService = Main.resourceFactory.getMatchService();
    
    private final ComboBox<PlayerWithUTR> player1Combo;
    private final ComboBox<PlayerWithUTR> player2Combo;
    
    private final MatchScoreField matchScoreField;
    
    private final Button calculateButton = UIUtils.createButton("Kalkulál", ButtonVariant.LUMO_PRIMARY);
    
    private final MatchGrid matchGrid = new MatchGrid();
    private final UTRChangeGrid utrChangeGrid = new UTRChangeGrid();
    
    private final List<BookedMatch> allBookedMatches;
    
    public UTRForecastWindow(List<PlayerWithUTR> utrRankingList) {
        allBookedMatches = Main.resourceFactory.getUTRService().loadBookedMatches();
        matchScoreField = new MatchScoreField(3);
        matchScoreField.addScoreChangedListener(() -> inputChanged());
        
        this.setHeaderTitle("UTR előrejelzés");
        
        List<PlayerWithUTR> sortedUTRRankingList = utrRankingList.stream()
                .sorted(comparing(p -> p.player().name()))
                .toList();
        
        player1Combo = createPlayerCombo(sortedUTRRankingList);
        player2Combo = createPlayerCombo(sortedUTRRankingList);
        
        setPlayers(utrRankingList);
        
        add(createForm());
        
        setModal(true);
        setDraggable(true);
        setResizable(true);
        setCloseOnOutsideClick(false);
        
        calculateButton.addClickListener(click -> calculate());
        calculateButton.setEnabled(false);
        
        Button closeButton = UIUtils.createButton(VaadinIcon.CLOSE, ButtonVariant.LUMO_SMALL);
        closeButton.addClickListener(click -> close());
        getHeader().add(closeButton);
        
        setMinWidth("600px");
    }
    
    private void setPlayers(List<PlayerWithUTR> utrRankingList) {
        
        Optional<PlayerWithUTR> player1 = utrRankingList.stream()
                .filter(p -> p.player().id().equals(VaadinUtil.getUser().playerId()))
                .findAny();
        
        if(player1.isPresent()) {
            player1Combo.setValue(player1.get());
            
            Optional<Match> nextMatch = matchService.findNextMatch(player1.get().player());
            if(nextMatch.isPresent()) {
                Player player2 = nextMatch.get().player1().equals(player1.get().player()) ? nextMatch.get().player2() : nextMatch.get().player1();
                PlayerWithUTR player2WithUtr = utrRankingList.stream().filter(p -> p.player().id().equals(player2.id())).findAny().get();
                player2Combo.setValue(player2WithUtr);
            }
        }
    }

    private ComboBox<PlayerWithUTR> createPlayerCombo(List<PlayerWithUTR> players) {
        ComboBox<PlayerWithUTR> comboBox = new ComboBox<>();
        comboBox.setWidth("300px");
        comboBox.setItemLabelGenerator(p -> p.player().name() + " " + p.utr());
        comboBox.setItems(players);
        comboBox.addValueChangeListener(e -> inputChanged());
        comboBox.setPlaceholder("Válassz játékost");
        return comboBox;
    }
    
    private void inputChanged() {
        matchGrid.setItems(List.of());
        utrChangeGrid.setItems(List.of());
        
        calculateButton.setEnabled(!player1Combo.isEmpty() && !player1Combo.isEmpty() && !matchScoreField.getMatchResult().setResults().isEmpty());
    }

    private Component createForm() {

        VerticalLayout layout = new VerticalLayout();
        //layout.setSpacing(false);
        layout.setPadding(false);

        layout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        
        Label description = new Label("Megnézheted, hogy egy fiktív mérkőzés után hogyan változna az UTR-ed.");
        
        VerticalLayout playersLayout = new VerticalLayout(player1Combo, player2Combo);
        playersLayout.setPadding(false);
        playersLayout.setSpacing(false);
        
        HorizontalLayout playersWithScoresLayout = new HorizontalLayout(playersLayout, matchScoreField);
        playersWithScoresLayout.setSizeFull();
        playersWithScoresLayout.setAlignItems(Alignment.CENTER);
        
        Div spacer = new Div();
        spacer.setHeight("10px");
        
        //HorizontalLayout resultLayout = new HorizontalLayout(player1ResultColumn, player2ResultColumn);
        
        layout.add(description, playersWithScoresLayout, calculateButton, matchGrid, utrChangeGrid);
        layout.setAlignSelf(Alignment.START, description);
        
        return layout;
    }
    
    private void calculate() {
        MatchResult matchResult = matchScoreField.getMatchResult();
        VaadinUtil.logUserAction(logger, "forecasts match: {} vs {} {}", player1Combo.getValue().player().name(), player2Combo.getValue().player().name(), matchResult);
        UTRForecastResult utrForecastResult = UTRCalculator.forecast(player1Combo.getValue(), player2Combo.getValue(), allBookedMatches, matchResult);
        matchGrid.setMatch(utrForecastResult.bookedMatch());
        utrChangeGrid.setResult(player1Combo.getValue(), player2Combo.getValue(), utrForecastResult);
    }

    public static void open(List<PlayerWithUTR> utrRankingList) {
        new UTRForecastWindow(utrRankingList).open();
    }
    
    private static class MatchGrid extends Grid<BookedMatch> {
        
        public MatchGrid() {
            
            addColumn(LitRenderer.<BookedMatch>of("${item.name1}")
                    .withProperty("name1", match -> match.playedMatch().player1().name()))
                .setClassNameGenerator(match -> match.playedMatch().result().isPlayer1Winner() ? "bold" : "")
                .setHeader("")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setFlexGrow(3);
            
            addColumn(LitRenderer.<BookedMatch>of("${item.name}")
                    .withProperty("name2", match -> match.playedMatch().player2().name()))
                .setClassNameGenerator(match -> match.playedMatch().result().isPlayer2Winner() ? "bold" : "")
                .setHeader("")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setFlexGrow(3);
            
            addComponentColumn(this::matchResult)
                .setClassNameGenerator(match -> "bold")
                .setHeader("Eredmény")
                .setTextAlign(ColumnTextAlign.CENTER)
                .setFlexGrow(1);
            
            addColumn(match -> match.matchUTRForPlayer1())
                .setKey("player1MatchUTR")
                .setHeader("Meccs UTR J1")
                .setTextAlign(ColumnTextAlign.CENTER)
                .setWidth("120px")
                .setFlexGrow(1);
            
            addColumn(match -> match.matchUTRForPlayer2())
                .setKey("player2MatchUTR")
                .setHeader("Meccs UTR J2")
                .setTextAlign(ColumnTextAlign.CENTER)
                .setWidth("120px")
                .setFlexGrow(1);
            
            setAllRowsVisible(true);
            
            setSelectionMode(SelectionMode.NONE);
            
            this.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        }
        
        private Component matchResult(BookedMatch bookedMatch) {
            MatchResult result = bookedMatch.playedMatch().result();
            if(result == null) {
                return new Span();
            } else {
                Label label = new Label(result.toString());
                HorizontalLayout layout = new HorizontalLayout(label);
                layout.setSpacing(false);
                layout.setAlignItems(Alignment.CENTER);
                layout.setJustifyContentMode(JustifyContentMode.CENTER);
                layout.setWidthFull();
                if(bookedMatch.isUpset()) {
                    Icon icon = VaadinIcon.EXCLAMATION.create();
                    icon.setSize("15px");
                    UIUtils.setTooltip("Meglepetés", layout);
                    layout.add(icon);
                    label.setText(label.getText());
                }
                return layout;
            }
        }
        
        void setMatch(BookedMatch bookedMatch) {
            setItems(List.of(bookedMatch));
            
            updateMatchUTRColumn("player1MatchUTR", bookedMatch.playedMatch().player1());
            updateMatchUTRColumn("player2MatchUTR", bookedMatch.playedMatch().player2());
        }
        
        private void updateMatchUTRColumn(String columnKey, Player player) {
            Label label = new Label("Meccs UTR " + getInitials(player.name()));
            UIUtils.setTooltip("Meccs UTR " + player.name() + " számára", label);
            
            HeaderCell headerCell = getHeaderRows().get(0).getCell(getColumnByKey(columnKey));
            headerCell.setComponent(label);
        }

        private static String getInitials(String name) {
            String[] parts = name.split(" ");
            return Arrays.stream(parts).map(part -> part.substring(0, 1)).collect(joining());
        }
    }
    
    private static class UTRChangeGrid extends Grid<PlayerWithUTR> {
        
        private final Map<PlayerWithUTR, UTR> playerToNewUTRMap = new HashMap<>();
        
        public UTRChangeGrid() {
            
            addColumn(p -> p.player().name())
                .setHeader("")
                .setFlexGrow(3);
            
            addColumn(p -> p.utr())
                .setHeader("Eredeti UTR")
                .setTextAlign(ColumnTextAlign.CENTER)
                .setFlexGrow(1);
            
            addComponentColumn(p -> utrChange(p))
                .setHeader("Változás")
                .setTextAlign(ColumnTextAlign.CENTER)
                .setFlexGrow(1);
            
            addColumn(p -> playerToNewUTRMap.get(p))
                .setHeader("Új UTR")
                .setTextAlign(ColumnTextAlign.CENTER)
                .setFlexGrow(1);
            
            setAllRowsVisible(true);
            
            setSelectionMode(SelectionMode.NONE);
            
            this.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        }
        
        private Span utrChange(PlayerWithUTR playerWithUTR) {
            UTR originalUTR = playerWithUTR.utr();
            UTR newUTR = playerToNewUTRMap.get(playerWithUTR);
            if(originalUTR.isDefinded() && newUTR.isDefinded()) {
                double diff = newUTR.value() - originalUTR.value();
                Span span = new Span(new UTR(diff).toString());
                if(diff > 0) {
                    span = new Span("+" + new UTR(diff).toString());
                    UIUtils.setTextColor(TextColor.SUCCESS, span);
                } else if(diff < 0) {
                    UIUtils.setTextColor(TextColor.ERROR, span);
                }
                return span;
            } else {
                return new Span();
            }
        }

        void setResult(PlayerWithUTR player1, PlayerWithUTR player2, UTRForecastResult utrForecastResult) {
            playerToNewUTRMap.put(player1, utrForecastResult.player1NewUTR());
            playerToNewUTRMap.put(player2, utrForecastResult.player2NewUTR());
            setItems(List.of(player1, player2));
        }
    }
    
}
