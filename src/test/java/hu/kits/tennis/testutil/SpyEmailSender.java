package hu.kits.tennis.testutil;

import hu.kits.tennis.domain.email.Email;
import hu.kits.tennis.domain.email.EmailSender;

public class SpyEmailSender implements EmailSender {

    private Email lastEmailSent;
    
    @Override
    public void sendEmail(Email email) {
        lastEmailSent = email;
    }

    public Email getLastEmailSent() {
        return lastEmailSent;
    }

}
