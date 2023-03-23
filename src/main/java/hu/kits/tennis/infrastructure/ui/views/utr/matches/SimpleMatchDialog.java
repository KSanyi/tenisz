package hu.kits.tennis.infrastructure.ui.views.utr.matches;

import java.util.List;
import java.util.Locale;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePickerVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.match.MatchResult;
import hu.kits.tennis.domain.match.MatchService;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.Players;
import hu.kits.tennis.infrastructure.ui.component.ConfirmationDialog;
import hu.kits.tennis.infrastructure.ui.component.HungarianDatePicker;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.component.MatchScoreField;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

public class SimpleMatchDialog extends Dialog {

    private final MatchService matchService = Main.resourceFactory.getMatchService();
    
    private final DatePicker datePicker = new HungarianDatePicker("Dátum");
    private final ComboBox<Player> player1Combo;
    private final ComboBox<Player> player2Combo;
    
    private final Match match;
    
    private final MatchScoreField matchScoreField;
    
    private final Runnable matchChangeCallback;
    
    public SimpleMatchDialog(Match match, Players players, int bestOfNSets, Runnable matchChangeCallback) {

        this.match = match;
        this.matchChangeCallback = matchChangeCallback;
        
        matchScoreField = new MatchScoreField(bestOfNSets);
        if(match.result() != null) {
            matchScoreField.setMatchResult(match.result());
        }
        if(match.date() != null) {
            datePicker.setValue(match.date());
        }
        
        player1Combo = createPlayerCombo(players.entries(), match.player1());
        player2Combo = createPlayerCombo(players.entries(), match.player2());
        
        setDraggable(true);
        setResizable(true);
        
        add(createForm());
        
        Button deleteButton = UIUtils.createButton("Törlés", ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        deleteButton.getStyle().set("margin-right", "auto");
        deleteButton.addClickListener(click -> delete());
        deleteButton.setVisible(match.id() != null);
        
        Button cancelButton = UIUtils.createButton("Mégsem", ButtonVariant.LUMO_CONTRAST);
        cancelButton.addClickListener(click -> close());
        
        getFooter().add(cancelButton, deleteButton);
        
        Button saveButton = UIUtils.createButton("Mentés", ButtonVariant.LUMO_PRIMARY);
        getFooter().add(saveButton);
        saveButton.addClickListener(click -> save());
        saveButton.addClickShortcut(Key.ENTER);
        
        //addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        setWidth("400px");
        this.setCloseOnOutsideClick(false);
    }
    
    private static ComboBox<Player> createPlayerCombo(List<Player> players, Player player) {
        ComboBox<Player> comboBox = new ComboBox<>();
        comboBox.setMaxWidth("250px");
        comboBox.setWidthFull();
        comboBox.setItemLabelGenerator(Player::name);
        comboBox.setItems(players);
        if(player != null) {
            comboBox.setValue(player);
        }
        return comboBox;
    }

    private Component createForm() {

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);

        layout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        
        datePicker.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        datePicker.setWidth("130px");
        datePicker.setLocale(new Locale("HU"));
        
        VerticalLayout playersLayout = new VerticalLayout(player1Combo, player2Combo);
        playersLayout.setPadding(false);
        playersLayout.setSpacing(false);
        
        HorizontalLayout playersWithScoresLayout = new HorizontalLayout(playersLayout, matchScoreField);
        playersWithScoresLayout.setSizeFull();
        playersWithScoresLayout.setAlignItems(Alignment.CENTER);
        
        Div spacer = new Div();
        spacer.setHeight("10px");
        Label matchIdLabel = UIUtils.createH6Label("Match id: " + match.id());
        matchIdLabel.setVisible(match.id() != null);
        layout.add(matchIdLabel, datePicker, spacer, playersWithScoresLayout);
        layout.setAlignSelf(Alignment.START, matchIdLabel);
        
        return layout;
    }
    
    private void save() {
        
        if(!matchScoreField.hasValidScore()) {
            KITSNotification.showError("Hibás eredmény");
        } else if(datePicker.isEmpty()){
            KITSNotification.showError("Dátum kötelező");
        } else if(player1Combo.isEmpty() || player2Combo.isEmpty()){
            KITSNotification.showError("Játékosok megadása kötelező");
        } else {
            MatchResult matchResult = matchScoreField.getMatchResult();
            Match updatedMatch = new Match(match.id(), match.tournamentId(), match.tournamentBoardNumber(), match.tournamentMatchNumber(), datePicker.getValue(), player1Combo.getValue(), player2Combo.getValue(), matchResult);
            matchService.saveMatch(updatedMatch);
            matchChangeCallback.run();
            close();
        }
    }
    
    private void delete() {
        new ConfirmationDialog("Biztos hogy törlöd a meccset?", () -> {
            matchService.deleteMatch(match);
            matchChangeCallback.run();
            close();
        }).open();
    }

}
