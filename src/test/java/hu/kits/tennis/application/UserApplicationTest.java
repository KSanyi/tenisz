package hu.kits.tennis.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hu.kits.tennis.common.KITSException;
import hu.kits.tennis.domain.email.Email;
import hu.kits.tennis.domain.user.AuthenticationException;
import hu.kits.tennis.domain.user.Requests.PasswordChangeRequest;
import hu.kits.tennis.domain.user.Requests.UserDataUpdateRequest;
import hu.kits.tennis.domain.user.Requests.UserRegistrationRequest;
import hu.kits.tennis.domain.user.Role;
import hu.kits.tennis.domain.user.UserData;
import hu.kits.tennis.domain.user.UserData.Status;
import hu.kits.tennis.infrastructure.ResourceFactory;
import hu.kits.tennis.domain.user.UserService;
import hu.kits.tennis.domain.user.Users;
import hu.kits.tennis.testutil.InMemoryDataSourceFactory;
import hu.kits.tennis.testutil.SpyEmailSender;

public class UserApplicationTest {

    private static final SpyEmailSender spyEmailSender = new SpyEmailSender();
    
    private static UserService userService;
    
    @SuppressWarnings("static-method")
    @BeforeEach
    private void init() throws Exception {
        DataSource dataSource = InMemoryDataSourceFactory.createDataSource(
                "INSERT INTO USER VALUES('ksanyi', 'PWD', 'Kócsó Sanyi', 'ADMIN', '06703699209', 'kocso.sandor.gabor@gmail.com', 'ACTIVE', 1)",
                "INSERT INTO USER VALUES('csányika', 'PWD', 'Csányi Zsolt', 'MEMBER', '', 'csanyika@xxx.hu', 'ACTIVE', 0)");
        
        ResourceFactory resourceFactory = new ResourceFactory(dataSource, spyEmailSender);
        userService = resourceFactory.getUserService();
    }
    
    @Test
    void listUsers() {
        Users users = userService.loadAllUsers();
        assertEquals(2, users.list().size());
        
        UserData user = users.getUser("ksanyi");
        assertEquals("Kócsó Sanyi", user.name());
        assertEquals("kocso.sandor.gabor@gmail.com", user.email());
        assertEquals("KS", user.initials());
        assertEquals("06703699209", user.phone());
        assertEquals(Role.ADMIN, user.role());
        assertEquals(Status.ACTIVE, user.status());
        assertEquals(1, user.playerId());
    }
    
    @Test
    void registrationProcess() throws AuthenticationException {
        
        UserData admin = userService.findUser("ksanyi");
        
        userService.register(new UserRegistrationRequest("Gipsz Jakab", "062012345678", "jakab@gmail.com", "abcd1234"));
        
        Email email = spyEmailSender.getLastEmailSent();
        assertEquals("Új OPFR regisztráció értesítés", email.subject());
        assertEquals(admin.email(), email.recipient());
        
        Assertions.assertThrows(KITSException.class, () -> {
            userService.authenticateUser("jakab@gmail.com", "abcd1234");
        });
        
        var newlyRegisteredUsers = userService.loadNewlyRegisteredUsers();
        assertEquals(1, newlyRegisteredUsers.size());
        
        var newlyRegisteredUser = newlyRegisteredUsers.get(0);
        
        userService.activateUser(newlyRegisteredUser);
        
        assertEquals(List.of(), userService.loadNewlyRegisteredUsers());
        
        email = spyEmailSender.getLastEmailSent();
        assertEquals("OPFR regisztráció", email.subject());
        assertEquals(newlyRegisteredUser.email(), email.recipient());
        
        userService.authenticateUser("jakab@gmail.com", "abcd1234");
    }
    
    @Test
    void authenticate() throws AuthenticationException {
        UserData user = userService.authenticateUser("ksanyi", "PWD");
        
        assertEquals("Kócsó Sanyi", user.name());
    }
    
    @Test
    void failedAuthentication() {
        Assertions.assertThrows(AuthenticationException.class, () -> {
            userService.authenticateUser("unknwown", "PWD");
        });
        
        Assertions.assertThrows(AuthenticationException.class, () -> {
            userService.authenticateUser("ksanyi", "wrongpassword");
        });
    }
    
    @Test
    void changePassword() throws AuthenticationException {
        
        userService.changePassword("ksanyi", new PasswordChangeRequest("PWD", "abcd1234"));
        
        Assertions.assertThrows(AuthenticationException.class, () -> {
            userService.authenticateUser("ksanyi", "PWD");
        });
        
        userService.authenticateUser("ksanyi", "abcd1234");
    }
    
    @Test
    void generateNewPassword() throws AuthenticationException {
        
        userService.generateNewPassword("ksanyi");
        
        var email = spyEmailSender.getLastEmailSent();
        assertEquals("OPFR új jelszó", email.subject());
        assertEquals("kocso.sandor.gabor@gmail.com", email.recipient());
        
        String newPassword = findPasswordInEmail(email);
        
        Assertions.assertThrows(AuthenticationException.class, () -> {
            userService.authenticateUser("ksanyi", "PWD");
        });
        
        userService.authenticateUser("ksanyi", newPassword);
    }
    
    private static String findPasswordInEmail(Email email) {
        Pattern pattern = Pattern.compile("<b>(\\S+)</b>");
        Matcher matcher = pattern.matcher(email.content());
        matcher.find();
        return matcher.group(1);
    }
    
    @Test
    void updateUser() {
        
        userService.updateUser("csányika", new UserDataUpdateRequest(new UserData("csányika", "Csányi Zsolti", Role.VISITOR, "", "nomail", Status.ACTIVE, 0)));
        
        UserData user = userService.findUser("csányika");
        assertEquals("Csányi Zsolti", user.name());
        assertEquals("nomail", user.email());
        assertEquals("", user.phone());
        assertEquals(Role.VISITOR, user.role());
        assertEquals(Status.ACTIVE, user.status());
    }
    
}
