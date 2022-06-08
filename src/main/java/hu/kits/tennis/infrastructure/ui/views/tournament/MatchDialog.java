package hu.kits.tennis.infrastructure.ui.views.tournament;

import java.util.Locale;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.infrastructure.ui.component.ComponentFactory;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

public class MatchDialog extends Dialog {

    private final TournamentService tournamentService = Main.resourceFactory.getTournamentService();
    
    private final DatePicker datePicker = new DatePicker("Dátum");
    
    private final Player player1;
    private final Player player2;
    
    private final TextField scoreField1 = createScoreField();
    private final TextField scoreField2 = createScoreField();
    
    private final Button saveButton = new Button("Mentés", click -> save());
    
    public MatchDialog(Player player1, Player player2) {
        
        this.player1 = player1;
        this.player2 = player2;
        
        setDraggable(true);
        setResizable(true);
        
        VerticalLayout layout = new VerticalLayout(new H4("Meccs"), createForm(), saveButton);
        layout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        layout.setPadding(false);
        layout.setSpacing(false);
        
        add(layout);
        //setWidth(400, Unit.PIXELS);
        
    }
    
    private Component createForm() {

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);

        layout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        
        datePicker.setLocale(new Locale("HU"));
        
        H4 vs = new H4("vs");
        HorizontalLayout playersLayout = new HorizontalLayout(creatPlayerLabel(player1), vs, creatPlayerLabel(player2));
        playersLayout.setWidthFull();
        playersLayout.setAlignItems(Alignment.BASELINE);
        
        H4 separator = new H4(":");
        HorizontalLayout scoreLayout = new HorizontalLayout(scoreField1, separator, scoreField2);
        scoreLayout.setAlignItems(Alignment.BASELINE);
        
        layout.add(datePicker, playersLayout, scoreLayout);
        
        return layout;
    }
    
    private Component creatPlayerLabel(Player player) {
        return UIUtils.createH3Label(player.name());
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
    
    
    private void save() {
        
    }
    
}
