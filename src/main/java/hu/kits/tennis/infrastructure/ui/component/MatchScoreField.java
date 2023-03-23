package hu.kits.tennis.infrastructure.ui.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.value.ValueChangeMode;

import hu.kits.tennis.common.Pair;
import hu.kits.tennis.domain.match.MatchResult;
import hu.kits.tennis.domain.match.MatchResult.SetResult;

public class MatchScoreField extends HorizontalLayout {
    
    private final List<Pair<TextField, TextField>> scoreFields = new ArrayList<>();
    private final List<Component> scoreFieldsHolder = new ArrayList<>();
    private final int setsNeedToWin;
    
    private final List<ScoreChangedListener> scoreChangedListeners = new ArrayList<>();
    
    public MatchScoreField(int bestOfNSets) {
        setsNeedToWin = bestOfNSets / 2 + 1;
        for(int i=0;i<setsNeedToWin;i++) {
            addSet();
        }
        
        scoreFields.get(0).first().focus();
    }
    
    public void setMatchResult(MatchResult result) {
        for(int i=0;i<result.setResults().size();i++) {
            SetResult setResult = result.setResults().get(i);
            scoreFields.get(i).first().setValue(String.valueOf(setResult.player1Score()));
            scoreFields.get(i).second().setValue(String.valueOf(setResult.player2Score()));
        }
    }

    public MatchResult getMatchResult() {
        List<SetResult> setResults = new ArrayList<>();
        
        for(int setNumber=0;setNumber<scoreFields.size();setNumber++) {
            getSetResult(setNumber).ifPresent(setResults::add);
        }
        return new MatchResult(setResults);
    }
    
    private Optional<SetResult> getSetResult(int setNumber) {
        var fields = scoreFields.get(setNumber);
        
        var player1Games = getScore(fields.first());
        var player2Games = getScore(fields.second());
        if(player1Games.isPresent() && player2Games.isPresent()) {
            return Optional.of(new SetResult(player1Games.get(), player2Games.get()));
        } else {
            return Optional.empty();
        }
    }

    public boolean hasValidScore() {
        return scoreFields.stream().allMatch(fields -> hasValidGameNumber(fields.first()) && hasValidGameNumber(fields.second()));
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
    
    Optional<Integer> getScore(TextField textField) {
        return textField.isEmpty() ? Optional.empty() : Optional.of(Integer.parseInt(textField.getValue()));
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
        
        scoreChangedListeners.forEach(listener -> listener.scoreChanged());
        
        int player1Sets = 0;
        int player2Sets = 0;
        boolean hasNotFilledField = false;
        for(int setNumber=0;setNumber<scoreFields.size();setNumber++) {
            if(hasValidGameNumber(scoreFields.get(setNumber).first()) && hasValidGameNumber(scoreFields.get(setNumber).second())) {
                SetResult setResult = getSetResult(setNumber).get();
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
        
        scoreFields.add(Pair.of(scoreField1, scoreField2));
        VerticalLayout verticalLayout = new VerticalLayout(scoreField1, scoreField2);
        verticalLayout.setPadding(false);
        verticalLayout.setSpacing(false);
        add(verticalLayout);
        scoreFieldsHolder.add(verticalLayout);
        scoreField1.setValueChangeMode(ValueChangeMode.EAGER);
        scoreField2.setValueChangeMode(ValueChangeMode.EAGER);
    }

    public void addScoreChangedListener(ScoreChangedListener scoreChangedListener) {
        scoreChangedListeners.add(scoreChangedListener);
    }
    
    public static interface ScoreChangedListener {
        void scoreChanged();
    }
    
}
