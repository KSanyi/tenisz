package hu.kits.tennis.domain.email;

import java.util.List;

import hu.kits.tennis.domain.user.UserData;

public class EmailCreator {

    public static Email createRegistrationNotificationEmail(List<String> adminEmails, UserData user) {
        String content = String.format("""
                Sziasztok,<br/>
                <br/>
                Új felhasználó regisztrált<br/>
                Azonosító: %s<br/>
                Név: %s<br/>
                Email: %s<br/>
                Telefonszám: %s<br/>
                <br/>
                Üdv,<br/>
                <br/>
                OPFR  
                """, user.userId(), user.name(), user.email(), user.phone());
        
        return new Email(adminEmails.get(0), "Új OPFR regisztráció értesítés", adminEmails, content);
    }

    public static Email createActivationNotificationEmail(UserData userData) {
        String content = String.format("""
                Kedves %s,<br/>
                <br/>
                %s felhasználói névvel regisztrált OPFR tagsága mostantól aktív!<br/>
                <br/>
                Üdvözlettel,<br/>
                <br/>
                OPFR  
                """, userData.name(), userData.email());
        
        return new Email(userData.email(), "OPFR regisztráció", List.of(), content);
    }

    public static Email createNewUserEmail(String password, UserData userData) {
        String content = String.format("""
                Kedves %s,<br/>
                <br/>
                %s felhasználói névvel regisztrált OPFR tagsága mostantól aktív!<br/>
                Ideiglenes jelszava: <b>%s</b><br/>
                <br/>
                Üdvözlettel,<br/>
                <br/>
                OPFR  
                """, userData.name(), userData.email(), password);
        
        return new Email(userData.email(), "OPFR regisztráció", List.of(), content);
    }

    public static Email createNewPasswordEmail(String newPassword, UserData userData) {
        String content = String.format("""
                Kedves %s,<br/>
                <br/>
                Új ideiglenes jelszava: <b>%s</b><br/>
                <br/>
                Üdvözlettel,<br/>
                <br/>
                OPFR  
                """, userData.name(), newPassword);
        
        return new Email(userData.email(), "OPFR új jelszó", List.of(), content);
    }

}
