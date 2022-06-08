package hu.kits.tennis.domain.user;

public class AuthenticationException extends Exception {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException() {
        super("");
    }
    
}
