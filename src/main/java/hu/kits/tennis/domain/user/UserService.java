package hu.kits.tennis.domain.user;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.common.KITSException;
import hu.kits.tennis.common.Pair;
import hu.kits.tennis.domain.email.EmailCreator;
import hu.kits.tennis.domain.email.EmailSender;
import hu.kits.tennis.domain.user.Requests.PasswordChangeRequest;
import hu.kits.tennis.domain.user.Requests.UserCreationRequest;
import hu.kits.tennis.domain.user.Requests.UserDataUpdateRequest;
import hu.kits.tennis.domain.user.Requests.UserRegistrationRequest;
import hu.kits.tennis.domain.user.UserData.Status;
import hu.kits.tennis.domain.user.password.PasswordGenerator;
import hu.kits.tennis.domain.user.password.PasswordHasher;

public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final UserRepository userRepository;
    private final EmailSender emailSender;
    private final PasswordHasher passwordHasher;
    
    public UserService(UserRepository userRepository, EmailSender emailSender, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.emailSender = emailSender;
        this.passwordHasher = passwordHasher;
    }
    
    public Users loadAllUsers() {
        return userRepository.loadAllUsers();
    }
    
    public List<UserData> loadNewlyRegisteredUsers() {

        Users users = userRepository.loadAllUsers();
        
        return users.loadNewlyRegistered();
    }
    
    public UserData findUser(String userId) {
        return userRepository.loadUser(userId);
    }
    
    public void register(UserRegistrationRequest userRegistrationRequest) {
        
        UserData userData = new UserData(
                userRegistrationRequest.email(),
                userRegistrationRequest.name(), 
                Role.MEMBER, 
                userRegistrationRequest.phone(), 
                userRegistrationRequest.email(), 
                Status.REGISTERED);
        
        logger.info("Registering new user: {}", userData);
        
        Users users = userRepository.loadAllUsers();
        
        validateNewRegistrationData(userData);
        
        userRepository.saveNewUser(userData, userRegistrationRequest.password());
        
        emailSender.sendEmail(EmailCreator.createRegistrationNotificationEmail(users.adminEmails(), userData));
    }
    
    public void activateUser(UserData user) {
        userRepository.activateUser(user.userId());
        
        if(user.status() == Status.REGISTERED) {
            emailSender.sendEmail(EmailCreator.createActivationNotificationEmail(user));            
        }
        
        logger.info("User {} is activated", user.name());
    }
    
    public void inactivateUser(UserData user) {
        userRepository.inActivateUser(user.userId());
        
        logger.info("User {} is inactivated", user);
    }
    
    public void saveNewUser(UserCreationRequest userCreationRequest) {
        
        UserData userData = userCreationRequest.userData();
        logger.info("Saving new user: {}", userData);
        
        validateNewUserData(userData);
        
        if(userData.userId().isEmpty()) {
            userData.setEmailAsUserId();
        }
        
        String password = PasswordGenerator.generateRandomPassword();
        String passwordHash = passwordHasher.createPasswordHash(password);
        
        userRepository.saveNewUser(userCreationRequest.userData(), passwordHash);
        
        if(!userData.email().isEmpty()) {
            emailSender.sendEmail(EmailCreator.createNewUserEmail(password, userData));            
        }
    }
    
    public void updateUser(String userId, UserDataUpdateRequest userDataUpdateRequest) {
        
        logger.info("Updating user: {} with new data: {}", userId, userDataUpdateRequest.userData());
        //validateUserDataChange(userDataUpdateRequest.userData());
        userRepository.updateUser(userId, userDataUpdateRequest.userData());
    }
    
    private void validateNewRegistrationData(UserData userData) {
        
        Users users = userRepository.loadAllUsers();
        if(users.hasUserWithEmail(userData)) {
            logger.info("User exists with this email address: {}", userData.email());
            throw new KITSException("Létezik már felhasználó ezzel az email címmel");
        }
        if(!userData.userId().isEmpty() && users.hasUserWithId(userData.userId())) {
            logger.info("User exists with this id: {}", userData.userId());
            throw new KITSException("Létezik már felhasználó ezzel az azonosítóval");
        }
    }
    
    private void validateNewUserData(UserData userData) {
        
        Users users = userRepository.loadAllUsers();
        if(!userData.userId().isEmpty() && users.hasUserWithId(userData.userId())) {
            logger.info("User exists with this id: {}", userData.userId());
            throw new KITSException("Létezik már felhasználó ezzel az azonosítóval");
        }
    }
    
    private void validateUserDataChange(UserData userData) {
        
        Users users = userRepository.loadAllUsers();
        if(users.hasUserWithEmail(userData)) {
            logger.info("User exists with this email address: {}", userData.email());
            throw new KITSException("Létezik már felhasználó ezzel az email címmel");
        }
    }
    
    public void deleteUser(String userId) {
        
        logger.info("Deleting user: {}", userId);
        
        userRepository.deleteUser(userId);
    }

    public UserData authenticateUser(String userIdOrEmail, String password) throws AuthenticationException {
        
        Optional<Pair<UserData, String>> userWithPasswordHash;
        try {
            userWithPasswordHash = userRepository.findUserWithPasswordHash(userIdOrEmail);
        } catch(IllegalStateException ex) {
            logger.info("Authentication failure. '{}' can not be used for authentication", userIdOrEmail);
            throw new AuthenticationException("A '" + userIdOrEmail + " nem használható belépésre!");
        }
        
        if(userWithPasswordHash.isPresent()) {
            
            UserData user = userWithPasswordHash.get().getFirst();
            String passwordHash = userWithPasswordHash.get().getSecond();
            
            if(passwordHasher.checkPassword(passwordHash, password)) {
                if(user.status() == Status.ACTIVE) {
                    logger.info("Authentication success for user '{}'", userIdOrEmail);
                    return user;
                } else {
                    logger.info("Authentication failure. User '{}' status is: {}", userIdOrEmail, user.status());
                    throw new KITSException(user.status().name());
                }
            } else {
                logger.info("Authentication failure. Wrong password for user '{}'", userIdOrEmail);
                throw new AuthenticationException();
            }
        } else {
            logger.info("Authentication failure. User with user id '{}' not found", userIdOrEmail);
            throw new AuthenticationException();
        }
    }
    
    public void changePassword(String userId, PasswordChangeRequest passwordChangeRequest) throws AuthenticationException {
        
        String oldPassword = passwordChangeRequest.oldPassword();
        String newPassword = passwordChangeRequest.newPassword(); 
        
        logger.info("Password change request for user '{}'", userId);
        
        authenticateUser(userId, oldPassword);
        
        userRepository.changePassword(userId, passwordHasher.createPasswordHash(newPassword));
        
        logger.info("Successful password change for '{}'", userId);
    }
    
    public void generateNewPassword(String userId) {
        
        logger.info("New password generation request for user '{}'", userId);
        
        String newPassword = PasswordGenerator.generateRandomPassword();
        
        userRepository.changePassword(userId, passwordHasher.createPasswordHash(newPassword));
        
        UserData userData = userRepository.loadUser(userId);
        
        logger.info("Successful password generation for '{}'", userId);
        emailSender.sendEmail(EmailCreator.createNewPasswordEmail(newPassword, userData));        
    }

}
