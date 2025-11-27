package hu.kits.tennis.domain.player.registration;

import java.time.LocalDateTime;
import java.util.Set;

import hu.kits.tennis.common.Clock;
import hu.kits.tennis.domain.ktr.KTR;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.Player.Address;
import hu.kits.tennis.domain.player.Player.Contact;
import hu.kits.tennis.domain.tournament.Organization;

public record Registration(Integer id, RegistrationData data, RegistrationStatus status, LocalDateTime timestamp) {

    public record RegistrationData(
            String name, 
            String phone, 
            String email, 
            int zip, 
            String town, 
            String streetAddress, 
            String experience, 
            String playFrequency, 
            String venue,
            String hasPlayedInTournament) {

        public Player toPlayer(KTR stratingKTR, String comment) {
            return new Player(
                    null,
                    name,
                    new Contact(email, phone, new Address(zip, town, streetAddress), comment),
                    stratingKTR,
                    Set.of(Organization.KVTK));
        }

        public String addressString() {
            return new Address(zip, town, streetAddress).toString();
        }
    }
    
    public enum RegistrationStatus {
        NEW, ACCEPTED, DELETED
    }
    
    public static Registration createNew(RegistrationData data) {
        return new Registration(null, data, RegistrationStatus.NEW, Clock.now());
    }
    
}
