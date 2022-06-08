package hu.kits.tennis.infrastructure.email;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Personalization;

import hu.kits.tennis.common.Environment;
import hu.kits.tennis.domain.email.Email;
import hu.kits.tennis.domain.email.EmailSender;

public class SendGridEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private static final String KSG_EMAIL = "kocso.sandor.gabor@gmail.com";
    
    private static final String SENDER = "kvtk1992@gmail.com";
    
    private final Environment environment;

    private final SendGrid sendGrid;
    
    public SendGridEmailSender(Environment environment, String sendGridPassword) {
        this.environment = environment;
        sendGrid = new SendGrid(sendGridPassword);
    }

    @Override
    public void sendEmail(Email email) {
        
        if(environment == Environment.DEV) {
            log.info("No email sending in {}. Email {} not sent", environment, email);
            return;
        } 
        
        if(email.recipient().contains("test")) {
            log.info("No email sending for {}. Email {} not sent", email.recipient(), email);
            return;
        }
        
        try {
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            
            Mail mail = new Mail();
            mail.setFrom(new com.sendgrid.helpers.mail.objects.Email(SENDER));
            mail.setSubject(email.subject());
            
            String content = environment == Environment.TEST ? prependTestEmail(email.content()) : email.content();
            
            mail.addContent(new Content("text/html", content));
            
            Personalization personalization = new Personalization();
            personalization.addTo(new com.sendgrid.helpers.mail.objects.Email(email.recipient()));
            for(String cc : email.ccs()) {
                if(!cc.equals(email.recipient())) {
                    personalization.addCc(new com.sendgrid.helpers.mail.objects.Email(cc));    
                }
            }
            if(environment == Environment.TEST && !email.recipient().equals(KSG_EMAIL) && !email.ccs().contains(KSG_EMAIL)) {
                personalization.addBcc(new com.sendgrid.helpers.mail.objects.Email(KSG_EMAIL));                
            }
            mail.addPersonalization(personalization);
            
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            
            if(response.getStatusCode() == 202) {
                log.info("Email sent: {}", email);    
            } else {
                log.error("Error sending email: {}, status code: {}, body: {}", email, response.getStatusCode(), response.getBody());
            }
        } catch(Exception ex) {
            log.error("Error during email sending", ex);
            throw new RuntimeException(ex);
        }
    }
    
    private static String prependTestEmail(String content) {
        return "--------------------- TEST ---------------------<br/><br/>" + content;
    }
    
}
