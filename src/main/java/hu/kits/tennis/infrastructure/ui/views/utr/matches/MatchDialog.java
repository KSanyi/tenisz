package hu.kits.tennis.infrastructure.ui.views.utr.matches;

import java.util.Locale;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.converter.StringToIntegerConverter;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.utr.BookedMatch;
import hu.kits.tennis.domain.utr.PlayerRepository;
import hu.kits.tennis.domain.utr.Players;
import hu.kits.tennis.domain.utr.UTRService;
import hu.kits.tennis.infrastructure.ui.component.ComponentFactory;
import hu.kits.tennis.infrastructure.ui.component.ConfirmationDialog;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;

public class MatchDialog extends Dialog {

    private final PlayerRepository playerRepository = Main.resourceFactory.getPlayerRepository();
    private final UTRService utrService = Main.resourceFactory.getUTRService();
    
    private final DatePicker datePicker = new DatePicker("Dátum");
    private final ComboBox<String> matchTypeCombo = ComponentFactory.createComboBox(200, "Meccs típus");
    private final PlayerComboBox player1Combo;
    private final PlayerComboBox player2Combo;
    private final TextField scoreField1 = createScoreField();
    private final TextField scoreField2 = createScoreField();
    
    private final Button cancelButton = new Button("Mégse", click -> close());
    private final Button saveButton = new Button("Mentés", click -> save());
    private final Button deleteButton = new Button("Törlés", click -> delete());
    
    private final Binder<MatchDataBean> binder = new Binder<>();
    
    private final MatchDataBean matchDataBean;
    
    public MatchDialog(MatchDataBean matchDataBean) {
        this.matchDataBean = matchDataBean;
        Players players = playerRepository.loadAllPlayers();
        player1Combo = new PlayerComboBox(players, playerRepository);
        player2Combo = new PlayerComboBox(players, playerRepository);
        
        bind(matchDataBean);
        
        setDraggable(true);
        setResizable(true);
        
        HorizontalLayout buttonBar = createButtonBar();
        VerticalLayout layout = new VerticalLayout(new H4("Új meccs"), createForm(), buttonBar);
        layout.setPadding(false);
        layout.setSpacing(false);
        
        add(layout);
        setWidth(600, Unit.PIXELS);
        
    }
    
    private Component createForm() {

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);

        layout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        
        datePicker.setLocale(new Locale("HU"));
        
        H4 vs = new H4("vs");
        player1Combo.setWidthFull();
        player2Combo.setWidthFull();
        HorizontalLayout playersLayout = new HorizontalLayout(player1Combo, vs, player2Combo);
        playersLayout.setWidthFull();
        playersLayout.setAlignItems(Alignment.BASELINE);
        
        H4 separator = new H4(":");
        HorizontalLayout scoreLayout = new HorizontalLayout(scoreField1, separator, scoreField2);
        scoreLayout.setAlignItems(Alignment.BASELINE);
        
        layout.add(datePicker, playersLayout, scoreLayout);
        
        return layout;
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
    
    private HorizontalLayout createButtonBar() {
        Span spacer = ComponentFactory.createSpacer(1);
        
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        deleteButton.setVisible(! matchDataBean.isNewBean());
        
        HorizontalLayout buttonBar = new HorizontalLayout(spacer, cancelButton, deleteButton, saveButton);
        buttonBar.expand(spacer);
        buttonBar.setWidthFull();
        return buttonBar;
    }
    
    private void bind(MatchDataBean matchDataBean) {
        
        binder.forField(datePicker)
            .asRequired("Kötelező mező")
            .bind(MatchDataBean::getDate, MatchDataBean::setDate);
        
        binder.forField(player1Combo)
            .asRequired("Kötelező mező")
            .withValidator(Validator.from(v ->  v == null || ! v.equals(player2Combo.getValue()), "Nem választható ugyanaz a játékos"))
            .bind(MatchDataBean::getPlayer1, MatchDataBean::setPlayer1);
        
        binder.forField(player2Combo)
            .asRequired("Kötelező mező")
            .withValidator(Validator.from(v ->  v == null || ! v.equals(player1Combo.getValue()), "Nem választható ugyanaz a játékos"))
            .bind(MatchDataBean::getPlayer2, MatchDataBean::setPlayer2);
        
        binder.forField(scoreField1)
            .asRequired()
            .withConverter(new StringToIntegerConverter(""))
            .withValidator(Validator.from(v ->  v + Integer.valueOf(scoreField2.getValue()) >= 4, "Legalább 4 lejátszott game szükséges"))
            .bind(MatchDataBean::getScore1, MatchDataBean::setScore1);
        
        binder.forField(scoreField2)
            .asRequired()
            .withConverter(new StringToIntegerConverter(""))
            .withValidator(Validator.from(v ->  v + Integer.valueOf(scoreField1.getValue()) >= 4, "Legalább 4 lejátszott game szükséges"))
            .bind(MatchDataBean::getScore2, MatchDataBean::setScore2);
        
        binder.readBean(matchDataBean);
    }
    
    private void save() {
        
        boolean valid = binder.writeBeanIfValid(matchDataBean);
        if(valid) {
            BookedMatch bookedMatch = utrService.calculatUTRAndSaveMatch(matchDataBean.toPlayedMatch());
            Notification.show(bookedMatch.toString());
            UI.getCurrent().getPage().reload();
        } else {
            KITSNotification.showError("Javítsd a hibákat!");
        }
    }
    
    private void delete() {
        new ConfirmationDialog("Biztos, hogy törlöd a meccset?", () -> {
            utrService.deleteMatch(matchDataBean.getId());
            UI.getCurrent().getPage().reload();
        }).open();
    }
    
}
