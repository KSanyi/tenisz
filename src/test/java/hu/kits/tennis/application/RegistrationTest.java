package hu.kits.tennis.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hu.kits.tennis.common.Clock;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.Player.Address;
import hu.kits.tennis.domain.player.PlayersService;
import hu.kits.tennis.domain.player.registration.Registration;
import hu.kits.tennis.domain.player.registration.Registration.RegistrationData;
import hu.kits.tennis.domain.player.registration.Registration.RegistrationStatus;
import hu.kits.tennis.domain.player.registration.RegistrationService;
import hu.kits.tennis.domain.utr.UTR;
import hu.kits.tennis.infrastructure.ApplicationContext;
import hu.kits.tennis.testutil.InMemoryDataSourceFactory;
import hu.kits.tennis.testutil.SpyEmailSender;

public class RegistrationTest {

    private static final SpyEmailSender spyEmailSender = new SpyEmailSender();
    private static final SpyInvoiceService spyInvoiceService = new SpyInvoiceService();
    
    private static RegistrationService registrationService;
    private static PlayersService playersService;
    
    @SuppressWarnings("static-method")
    @BeforeEach
    private void init() throws Exception {
        DataSource dataSource = InMemoryDataSourceFactory.createDataSource();
        
        ApplicationContext resourceFactory = new ApplicationContext(dataSource, spyEmailSender, null, spyInvoiceService);
        registrationService = resourceFactory.getRegistrationService();
        playersService = resourceFactory.getPlayersService();
    }
    
    @Test
    void registration() {
        
        Clock.setStaticTime(LocalDateTime.of(2023,1,1, 12,0));
        
        registrationService.saveNewRegistration(new RegistrationData(
                "Kiss Péter",
                "+36/70-123-4567",
                "petekiss@gmail.com",
                1132, "Budapest", "Teve utca 9",
                "2 éve", 
                "Heti 1x", 
                "Mini Garros", "Igen"));
        
        List<Registration> registrations = registrationService.loadAllNewRegistrations();
        assertEquals(1, registrations.size());
        
        Registration registration = registrations.get(0);
        RegistrationData data = registration.data();
        
        Assertions.assertEquals(RegistrationStatus.NEW, registration.status());
        Assertions.assertEquals(LocalDateTime.of(2023,1,1, 12,0), registration.timestamp());
        Assertions.assertEquals("Kiss Péter", data.name());
        Assertions.assertEquals("+36/70-123-4567", data.phone());
        Assertions.assertEquals("petekiss@gmail.com", data.email());
        Assertions.assertEquals(1132, data.zip());
        Assertions.assertEquals("Budapest", data.town());
        Assertions.assertEquals("Teve utca 9", data.streetAddress());
        Assertions.assertEquals("2 éve", data.experience());
        Assertions.assertEquals("Heti 1x", data.playFrequency());
        Assertions.assertEquals("Mini Garros", data.venue());
        Assertions.assertEquals("Igen", data.hasPlayedInTournament());
    }
    
    @Test
    void registrationApproval() {
        
        registrationService.saveNewRegistration(new RegistrationData(
                "Kiss Péter",
                "+36/70-123-4567",
                "petekiss@gmail.com",
                1132, "Budapest", "Teve utca 9",
                "2 éve", 
                "Heti 1x", 
                "Mini Garros", "Igen"));
        
        Registration registration = registrationService.loadAllNewRegistrations().get(0);
        registrationService.approveRegistration(registration, UTR.of(7.5), "Jó játékos");
        
        Assertions.assertTrue(registrationService.loadAllNewRegistrations().isEmpty());
        
        Optional<Player> player = playersService.loadAllPlayers().findPlayer("Kiss Péter");
        Assertions.assertTrue(player.isPresent());
        
        Player newPlayer = player.get();
        
        Assertions.assertEquals("Kiss Péter", newPlayer.name());
        Assertions.assertEquals("+36/70-123-4567", newPlayer.contact().phone());
        Assertions.assertEquals("petekiss@gmail.com", newPlayer.contact().email());
        Assertions.assertEquals(new Address(1132, "Budapest", "Teve utca 9"), newPlayer.contact().address());
        Assertions.assertEquals(UTR.of(7.5), newPlayer.startingUTR());
        
        Assertions.assertEquals(newPlayer, spyInvoiceService.lastSavedPlayer);
    }
    
}
