package hu.kits.tennis.domain.user.password;

public interface PasswordHasher {

    String createPasswordHash(String password);
    
    boolean checkPassword(String passwordHash, String candidatePassword);
    
}
