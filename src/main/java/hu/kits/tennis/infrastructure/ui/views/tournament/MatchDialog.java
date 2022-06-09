package hu.kits.tennis.infrastructure.ui.views.tournament;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePickerVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;

import hu.kits.tennis.common.Pair;
import hu.kits.tennis.domain.utr.Match;
import hu.kits.tennis.domain.utr.MatchResult;
import hu.kits.tennis.domain.utr.MatchResult.SetResult;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

public class MatchDialog extends Dialog {

    private final DatePicker datePicker = new DatePicker("Dátum");
    
    private final Match match;
    
    private final ScoreFields scoreFields1;
    private final ScoreFields scoreFields2;
    
    private final Button saveButton = UIUtils.createButton("Mentés", ButtonVariant.LUMO_PRIMARY);
    
    private final Consumer<Pair<Match, MatchResult>> matchResulCallback;
    
    public MatchDialog(String title, Match match, int bestOfNSets, Consumer<Pair<Match, MatchResult>> matchResulCallback) {
        
        this.match = match;
        this.matchResulCallback = matchResulCallback;
        
        scoreFields1 = new ScoreFields(bestOfNSets);
        scoreFields2 = new ScoreFields(bestOfNSets);

        setDraggable(true);
        setResizable(true);
        
        setHeaderTitle(title);
        add(createForm());
        getFooter().add(saveButton);
        saveButton.addClickListener(click -> save());
        
        //addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        setWidth("400px");
    }
    
    private Component createForm() {

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);

        layout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        
        datePicker.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        datePicker.setWidth("130px");
        datePicker.setLocale(new Locale("HU"));
        
        HorizontalLayout player1Layout = new HorizontalLayout(createPlayerLabel(match.player1()), scoreFields1);
        player1Layout.setAlignItems(Alignment.CENTER);
        player1Layout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        player1Layout.setWidthFull();
        HorizontalLayout player2Layout = new HorizontalLayout(createPlayerLabel(match.player2()), scoreFields2);
        player2Layout.setWidthFull();
        player2Layout.setAlignItems(Alignment.CENTER);
        player2Layout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        Div spacer = new Div();
        spacer.setHeight("10px");
        layout.add(datePicker, spacer, player1Layout, player2Layout);
        
        return layout;
    }
    
    private static Component createPlayerLabel(Player player) {
        return UIUtils.createH3Label(player.name());
    }

    private void save() {
        try {
            
            List<SetResult> setResults = new ArrayList<>();
            for(int i=0;i<scoreFields1.size();i++) {
                setResults.add(new SetResult(scoreFields1.getGamesForSet(i), scoreFields1.getGamesForSet(i)));
            }
            
            matchResulCallback.accept(new Pair<>(match, new MatchResult(setResults)));
            close();
        } catch(Exception ex) {
            KITSNotification.showError("Az eredményt meg kell adni");
        }
    }

}

class ScoreFields extends HorizontalLayout {
    
    private final List<TextField> scoreFields = new ArrayList<>();
    
    ScoreFields(int bestOfNSets) {
        int minimumSets = bestOfNSets / 2 + 1;
        for(int i=0;i<minimumSets;i++) {
            TextField scoreField = createScoreField();
            scoreFields.add(scoreField);
            add(scoreField);
        }
    }
    
    public int getGamesForSet(int i) {
        return Integer.parseInt(scoreFields.get(i).getValue());
    }

    public int size() {
        return scoreFields.size();
    }

    private static TextField createScoreField() {
        TextField scoreField1 = new TextField();
        scoreField1.setPattern("\\d{1,2}");
        scoreField1.setPreventInvalidInput(true);
        scoreField1.setAutoselect(true);
        scoreField1.setWidth(45, Unit.PIXELS);
        scoreField1.addThemeVariants(TextFieldVariant.LUMO_ALIGN_CENTER);
        
        return scoreField1;
    }
    
}
