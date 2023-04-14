package hu.kits.tennis.domain.player.registration;

import java.lang.invoke.MethodHandles;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.PlayersService;
import hu.kits.tennis.domain.player.registration.Registration.RegistrationData;
import hu.kits.tennis.domain.player.registration.Registration.RegistrationStatus;
import hu.kits.tennis.domain.utr.UTR;

public class RegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final PlayersService playersService;
    private final RegistrationRepository registartionRepository;
    
    public RegistrationService(PlayersService playersService, RegistrationRepository registartionRepository) {
        this.playersService = playersService;
        this.registartionRepository = registartionRepository;
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
    
    public void approveRegistration(Registration registration, UTR statingUTR, String comment) {
        logger.info("Registration is accepted for player: {} with starting  UTR {}", registration.data().name(), statingUTR);
        registartionRepository.setRegistrationStatus(registration.id(), RegistrationStatus.ACCEPTED);
        Player player = registration.data().toPlayer(statingUTR, comment);
        playersService.saveNewPlayer(player);
    }

    public boolean isEmailAlreadyRegistered(String email) {
        return playersService.findPlayerByEmail(email).isPresent();
    }
}
