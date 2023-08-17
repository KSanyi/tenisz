package hu.kits.tennis.domain.player.registration;

import java.lang.invoke.MethodHandles;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.domain.invoice.InvoiceService;
import hu.kits.tennis.domain.ktr.KTR;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.PlayersService;
import hu.kits.tennis.domain.player.registration.Registration.RegistrationData;
import hu.kits.tennis.domain.player.registration.Registration.RegistrationStatus;

public class RegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final PlayersService playersService;
    private final RegistrationRepository registartionRepository;
    private final InvoiceService invoiceService;
    
    public RegistrationService(PlayersService playersService, RegistrationRepository registartionRepository, InvoiceService invoiceService) {
        this.playersService = playersService;
        this.registartionRepository = registartionRepository;
        this.invoiceService = invoiceService;
    }
    
    public List<Registration> loadAllNewRegistrations() {
        return registartionRepository.loadAllRegistrations().stream()
                .filter(r -> r.status() == RegistrationStatus.NEW)
                .sorted(Comparator.comparing(Registration::timestamp))
                .toList();
    }
    
    public void saveNewRegistration(RegistrationData registrationData) {
        Registration registation = Registration.createNew(registrationData);
        logger.info("New registration arrived: {}", registation);
        registartionRepository.saveNewRegistration(registation);
    }
    
    public void approveRegistration(Registration registration, KTR statingKTR, String comment) {
        logger.info("Registration is accepted for player: {} with starting  KTR {}", registration.data().name(), statingKTR);
        registartionRepository.setRegistrationStatus(registration.id(), RegistrationStatus.ACCEPTED);
        Player player = registration.data().toPlayer(statingKTR, comment);
        Player savedPlayer = playersService.saveNewPlayer(player);
        logger.info("Creating player in invoice system");
        invoiceService.createPartnerForPlayer(savedPlayer);
        logger.info("Player created in invoice system");
    }

    public boolean isEmailAlreadyRegistered(String email) {
        return playersService.findPlayerByEmail(email).isPresent();
    }
}
