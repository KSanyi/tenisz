package hu.kits.tennis.domain.user;

public class Requests {

    public static class UserRegistrationRequest {
        
        private final String name;
        private final String phone;
        private final String email;
        private final String password;
        
        public UserRegistrationRequest(String name, String phone, String email, String password) {
            this.name = name;
            this.phone = phone;
            this.email = email;
            this.password = password;
        }

        public String name() {
            return name;
        }

        public String phone() {
            return phone;
        }

        public String email() {
            return email;
        }

        public String password() {
            return password;
        }
        
    }
    
    public static class UserCreationRequest {
        
        private final UserData userData;

        public UserCreationRequest(UserData userData) {
            this.userData = userData;
        }

        public UserData userData() {
            return userData;
        }
        
    }
    
    public static class UserDataUpdateRequest {
        
        private final UserData userData;

        public UserDataUpdateRequest(UserData userData) {
            this.userData = userData;
        }

        public UserData userData() {
            return userData;
        }
    }
    
    public static class PasswordChangeRequest {
        
        private final String oldPassword;
        private final String newPassword;
        
        public PasswordChangeRequest(String oldPassword, String newPassword) {
            this.oldPassword = oldPassword;
            this.newPassword = newPassword;
        }

        public String oldPassword() {
            return oldPassword;
        }

        public String newPassword() {
            return newPassword;
        }
    }
    
}
