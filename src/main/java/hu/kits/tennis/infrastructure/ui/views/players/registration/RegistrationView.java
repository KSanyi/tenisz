package hu.kits.tennis.infrastructure.ui.views.players.registration;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.player.registration.Registration.RegistrationData;
import hu.kits.tennis.domain.player.registration.RegistrationService;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.views.ktr.ranking.KTRRankingView;
import hu.kits.tennis.infrastructure.ui.views.players.registration.RegistrationForm.RegistrationDataBean;

@Route(value = "registration")
@PageTitle("Regisztráció")
public class RegistrationView extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final RegistrationService registrationService = Main.applicationContext.getRegistrationService();
    
    private final RegistrationForm registrationForm = new RegistrationForm();
    
    private final Button saveButton = UIUtils.createPrimaryButton("Mentés");
    
    public RegistrationView() {

        logger.info("Init");
        
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setWidthFull();
        setSpacing(false);
        
        add(new H3("KVTK regisztráció"),
            new Label("Kérlek töltsd ki a az alábbi regisztrációs űrlapot. Adataidat bizalmasan kezeljük."),
            registrationForm,
            saveButton);
        
        saveButton.addClickListener(click -> save());
    }

    private void save() {
        logger.info("Save button clicked");
        RegistrationDataBean bean = new RegistrationDataBean();
        boolean valid = registrationForm.writeBeanIfValid(bean);
        if(valid) {
            RegistrationData registrationData = bean.toRegistration();
            if(registrationService.isEmailAlreadyRegistered(registrationData.email())) {
                logger.debug("Registration attempt with existing email address: {}", registrationData);
                KITSNotification.showError("Már regisztráltak ezzel az email címmel!");
                return;
            }
            registrationService.saveNewRegistration(registrationData);
            KITSNotification.showInfo("Sikeres regisztráció! Köszönjük. Hamarosan értesítünk a továbbiakról!", 5000);
            UI.getCurrent().navigate(KTRRankingView.class);
        } else {
            KITSNotification.showError("Hibás adatok");
        }
    }
}
