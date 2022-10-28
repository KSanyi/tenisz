package hu.kits.tennis.domain.user;

import static java.util.stream.Collectors.joining;

import java.util.Objects;
import java.util.stream.Stream;

public class UserData {

    private String userId;
    private final String name;
    private final Role role;
    private final String phone;
    private final String email;
    private final Status status;
    private final int playerId;
    
    public UserData(String userId, String name, Role role, String phone, String email, Status status, int playerId) {
        this.userId = userId;
        this.name = name;
        this.role = role;
        this.phone = phone;
        this.email = email;
        this.status = status;
        this.playerId = playerId;
    }

    public boolean isNew() {
        return userId.isEmpty();
    }
    
    public void setEmailAsUserId() {
        userId = email;
    }
    
    public static UserData unknown(String userId) {
        return new UserData(userId, userId, Role.MEMBER, "???", "???", Status.INACTIVE, 0);
    }

    public static UserData createNew() {
        return new UserData("", "", Role.MEMBER, "", "", Status.ACTIVE, 0);
    }
    
    public String userId() {
        return userId;
    }

    public String name() {
        return name;
    }

    public Role role() {
        return role;
    }

    public String phone() {
        return phone;
    }

    public String email() {
        return email;
    }

    public Status status() {
        return status;
    }
    
    public int playerId() {
        return playerId;
    }
    
    public String initials() {
        if(name.length() > 5) {
            return Stream.of(name.split(" ")).map(part -> String.valueOf(part.charAt(0))).collect(joining());    
        } else {
            return name;
        }
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        UserData otherUserData = (UserData) other;
        return userId.equals(otherUserData.userId);
    }

    @Override
    public String toString() {
        return name + "(" + userId + ")";
    }
    
    public static final UserData ANONYMUS = new UserData("ANONYMUS", "ANONYMUS", Role.ANONYMUS, "", "", Status.ANONYMUS, 0);

    public static enum Status {
        REGISTERED("Regisztrált", "Még az adminnak jóvá kell hagyni a regisztrációt!"),
        ACTIVE("Aktív", ""),
        INACTIVE("Inaktív", "Letiltott felhasználó"),
        ANONYMUS("ANONYMUS", "");

        private final String label;
        private final String description;

        private Status(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String label() {
            return label;
        }

        public String description() {
            return description;
        }
    }

    public static UserData createAnonymus(int id) {
        return new UserData("ANONYMUS_" + id, "ANONYMUS_" + id, Role.ANONYMUS, "", "", Status.ANONYMUS, 0);
    }

}
