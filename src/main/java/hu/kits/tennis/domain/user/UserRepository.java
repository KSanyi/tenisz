package hu.kits.tennis.domain.user;

import java.util.Optional;

import hu.kits.tennis.common.Pair;

public interface UserRepository {

    Users loadAllUsers();
    
    void saveNewUser(UserData userData, String passwordHash);
    
    Optional<Pair<UserData, String>> findUserWithPasswordHash(String userIdOrEmail);

    void changePassword(String userId, String newPasswordHash);

    void updateUser(String userId, UserData user);

    void deleteUser(String userId);

    UserData loadUser(String userId);

    void activateUser(String userId);

    void inActivateUser(String userId);

    Optional<UserData> findUserByEmail(String email);

}
