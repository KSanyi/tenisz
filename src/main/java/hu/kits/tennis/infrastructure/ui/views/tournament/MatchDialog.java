package hu.kits.tennis.infrastructure.ui.views.tournament;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePickerVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.value.ValueChangeMode;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.Clock;
import hu.kits.tennis.common.Pair;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.domain.utr.Match;
import hu.kits.tennis.domain.utr.MatchResult;
import hu.kits.tennis.domain.utr.MatchResult.SetResult;
import hu.kits.tennis.domain.utr.MatchResultInfo;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.infrastructure.ui.component.ConfirmationDialog;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

class MatchDialog extends Dialog {

    private final TournamentService tournamentService = Main.resourceFactory.getTournamentService();
    
    private final DatePicker datePicker = new DatePicker("Dátum");
    
    private final Match match;
    
    private final ScoreFields scoreFields;
    
    private final Runnable matchChangeCallback;
    
    MatchDialog(String title, Match match, int bestOfNSets, Runnable matchChangeCallback) {
        
        this.match = match;
        this.matchChangeCallback = matchChangeCallback;
        
        scoreFields = new ScoreFields(bestOfNSets);
        if(match.result() != null) {
            scoreFields.setMatchResult(match.result());
        }

        datePicker.setValue(match.date() != null ? match.date() : Clock.today());

        setDraggable(true);
        setResizable(true);
        
        setHeaderTitle(title);
        add(createForm());
        
        Button deleteButton = UIUtils.createButton("Törlés", ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        deleteButton.getStyle().set("margin-right", "auto");
        deleteButton.addClickListener(click -> delete());
        //getFooter().add(deleteButton);
        
        Button saveButton = UIUtils.createButton("Mentés", ButtonVariant.LUMO_PRIMARY);
        getFooter().add(saveButton);
        saveButton.addClickListener(click -> save());
        saveButton.addClickShortcut(Key.ENTER);
        
        //addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        setWidth("420px");
    }
    
    private Component createForm() {

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);

        layout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        
        datePicker.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        datePicker.setWidth("130px");
        datePicker.setLocale(new Locale("HU"));
        
        VerticalLayout playersLayout = new VerticalLayout(createPlayerLabel(match.player1()), createPlayerLabel(match.player2()));
        playersLayout.setPadding(false);
        
        HorizontalLayout playersWithScoresLayout = new HorizontalLayout(playersLayout, scoreFields);
        playersWithScoresLayout.setSizeFull();
        playersWithScoresLayout.setAlignItems(Alignment.CENTER);
        
        Div spacer = new Div();
        spacer.setHeight("10px");
        Label matchIdLabel = UIUtils.createH6Label("Match id: " + match.id());
        layout.add(matchIdLabel, datePicker, spacer, playersWithScoresLayout);
        layout.setAlignSelf(Alignment.START, matchIdLabel);
        
        return layout;
    }
    
    private static Component createPlayerLabel(Player player) {
        return UIUtils.createH3Label(player != null ? player.name() : "?");
    }

    private void save() {
        
        if(scoreFields.hasValidScore()) {
            MatchResult matchResult = scoreFields.getMatchResult();
            tournamentService.setTournamentMatchResult(new MatchResultInfo(match, datePicker.getValue(), matchResult));
            matchChangeCallback.run();
            close();
        } else {
            KITSNotification.showError("Hibás eredmény");
        }
    }
    
    private void delete() {
        new ConfirmationDialog("Biztos hogy törldöd a meccset?", () -> {
            tournamentService.deleteMatch(match);
            matchChangeCallback.run();
            close();
        }).open();
    }

}

class ScoreFields extends HorizontalLayout {
    
    private final List<Pair<TextField, TextField>> scoreFields = new ArrayList<>();
    private final List<Component> scoreFieldsHolder = new ArrayList<>();
    private final int setsNeedToWin;
    
    ScoreFields(int bestOfNSets) {
        setsNeedToWin = bestOfNSets / 2 + 1;
        for(int i=0;i<setsNeedToWin;i++) {
            addSet();
        }
        
        scoreFields.get(0).first().focus();
    }
    
    void setMatchResult(MatchResult result) {
        for(int i=0;i<result.setResults().size();i++) {
            SetResult setResult = result.setResults().get(i);
            scoreFields.get(i).first().setValue(String.valueOf(setResult.player1Games()));
            scoreFields.get(i).second().setValue(String.valueOf(setResult.player2Games()));
        }
    }

    MatchResult getMatchResult() {
        List<SetResult> setResults = new ArrayList<>();
        
        for(int setNumber=0;setNumber<scoreFields.size();setNumber++) {
            setResults.add(getSetResult(setNumber));
        }
        return new MatchResult(setResults);
    }
    
    private SetResult getSetResult(int setNumber) {
        var fields = scoreFields.get(setNumber);
        return new SetResult(getScore(fields.first()), getScore(fields.second()));
    }

    boolean hasValidScore() {
        return scoreFields.stream().allMatch(fields -> hasValidGameNumber(fields.first()) && hasValidGameNumber(fields.second()) && getScore(fields.first()) != getScore(fields.second()));
    }
    
    private static boolean hasValidGameNumber(TextField textField) {
        String value = textField.getValue();
        try {
            int intValue = Integer.parseInt(value);
            return intValue >= 0;
        } catch(Exception ex) {
            return false;
        }
    }
    
    int getScore(TextField textField) {
        return Integer.parseInt(textField.getValue());
    }

    private TextField createScoreField() {
        TextField scoreField = new TextField();
        scoreField.setPattern("[0-9]*");
        scoreField.setMaxLength(2);
        scoreField.setPreventInvalidInput(true);
        scoreField.setAutoselect(true);
        scoreField.setWidth(40, Unit.PIXELS);
        scoreField.addThemeVariants(TextFieldVariant.LUMO_ALIGN_CENTER);
        scoreField.addValueChangeListener(e -> scoreChanged());
        
        return scoreField;
    }

    private void scoreChanged() {
        int player1Sets = 0;
        int player2Sets = 0;
        boolean hasNotFilledField = false;
        for(int setNumber=0;setNumber<scoreFields.size();setNumber++) {
            if(hasValidGameNumber(scoreFields.get(setNumber).first()) && hasValidGameNumber(scoreFields.get(setNumber).second())) {
                SetResult setResult = getSetResult(setNumber);
                if(setResult.isPlayer1Winner()) {
                    player1Sets++;
                } else if(setResult.isPlayer2Winner()) {
                    player2Sets++;
                } else {
                    return;
                }
                
                if(player1Sets == setsNeedToWin || player2Sets == setsNeedToWin) {
                    for(setNumber++;setNumber<scoreFields.size();setNumber++) {
                        scoreFields.remove(setNumber);
                        scoreFieldsHolder.remove(setNumber);
                    }
                    return;
                }
                
            } else {
                hasNotFilledField = true;
            }
        }
        if(!hasNotFilledField) {
            addSet();
            scoreFields.get(scoreFields.size() - 1).first().focus();
        }
    }

    private void addSet() {
        TextField scoreField1 = createScoreField();
        TextField scoreField2 = createScoreField();
        
        if(!scoreFields.isEmpty()) {
            scoreFields.get(scoreFields.size() - 1).second().addValueChangeListener(e -> scoreField1.focus());
        }
        scoreField1.addValueChangeListener(e -> scoreField2.focus());
        
        scoreFields.add(new Pair<>(scoreField1, scoreField2));
        VerticalLayout verticalLayout = new VerticalLayout(scoreField1, scoreField2);
        verticalLayout.setPadding(false);
        verticalLayout.setSpacing(false);
        add(verticalLayout);
        scoreFieldsHolder.add(verticalLayout);
        scoreField1.setValueChangeMode(ValueChangeMode.EAGER);
        scoreField2.setValueChangeMode(ValueChangeMode.EAGER);
    }
    
}
