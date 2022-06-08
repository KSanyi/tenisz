package hu.kits.tennis.domain.email;

import java.util.List;

public class Email {

    private final String recipient;
    private final String subject;
    private final List<String> ccs;
    private final String content;
    
    public Email(String recipient, String subject, List<String> ccs, String content) {
        this.recipient = recipient;
        this.subject = subject;
        this.ccs = ccs;
        this.content = content;
    }

    public String recipient() {
        return recipient;
    }

    public String subject() {
        return subject;
    }

    public List<String> ccs() {
        return ccs;
    }

    public String content() {
        return content;
    }

    @Override
    public String toString() {
        return String.format("To: %s subject: %s", recipient, subject);
    }
    
}
