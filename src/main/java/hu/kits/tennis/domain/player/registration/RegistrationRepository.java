package hu.kits.tennis.domain.player.registration;

import java.util.List;

import hu.kits.tennis.domain.player.registration.Registration.RegistrationStatus;

public interface RegistrationRepository {

    void saveNewRegistration(Registration registration);

    List<Registration> loadAllRegistrations();

    void setRegistrationStatus(int id, RegistrationStatus accepted);
    
}
