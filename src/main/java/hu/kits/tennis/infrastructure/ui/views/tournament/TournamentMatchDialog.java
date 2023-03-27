package hu.kits.tennis.infrastructure.ui.views.tournament;

import java.util.Locale;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
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

import hu.kits.tennis.Main;
import hu.kits.tennis.common.Clock;
import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.match.MatchResult;
import hu.kits.tennis.domain.match.MatchResultInfo;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.infrastructure.ui.component.ConfirmationDialog;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.component.MatchScoreField;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

class TournamentMatchDialog extends Dialog {

    private final TournamentService tournamentService = Main.resourceFactory.getTournamentService();
    
    private final DatePicker datePicker = new DatePicker("Dátum");
    
    private final Match match;
    
    private final MatchScoreField matchScoreField;
    
    private final Runnable matchChangeCallback;
    
    TournamentMatchDialog(String title, Match match, int bestOfNSets, Runnable matchChangeCallback) {
        
        this.match = match;
        this.matchChangeCallback = matchChangeCallback;
        
        matchScoreField = new MatchScoreField(bestOfNSets);
        if(match.result() != null) {
            matchScoreField.setMatchResult(match.result());
        }

        datePicker.setValue(match.date() != null ? match.date() : Clock.today());

        setDraggable(true);
        setResizable(true);
        
        setHeaderTitle(title);
        add(createForm());
        
        Button deleteButton = UIUtils.createButton("Eredmény törlése", ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        deleteButton.getStyle().set("margin-right", "auto");
        deleteButton.addClickListener(click -> deleteResult());
        getFooter().add(deleteButton);
        deleteButton.setVisible(match.isPlayed());
        
        Button saveButton = UIUtils.createButton("Mentés", ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(click -> save());
        saveButton.addClickShortcut(Key.ENTER);
        
        Button cancelButton = UIUtils.createButton("Mégsem", ButtonVariant.LUMO_CONTRAST);
        cancelButton.addClickListener(click -> close());
        
        getFooter().add(cancelButton, saveButton);
        
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
        
        HorizontalLayout playersWithScoresLayout = new HorizontalLayout(playersLayout, matchScoreField);
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
        if(matchScoreField.hasValidScore()) {
            MatchResult matchResult = matchScoreField.getMatchResult();
            tournamentService.setTournamentMatchResult(new MatchResultInfo(match, datePicker.getValue(), matchResult));
            matchChangeCallback.run();
            close();
        } else {
            KITSNotification.showError("Hibás eredmény");
        }
    }
    
    private void deleteResult() {
        new ConfirmationDialog("Biztos hogy törlöd az eredményt?", () -> {
            tournamentService.deleteMatchResult(match);
            matchChangeCallback.run();
            close();
        }).open();
    }

}
