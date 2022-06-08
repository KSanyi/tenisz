package hu.kits.tennis.domain.user;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import hu.kits.tennis.domain.user.UserData.Status;

public class Users {

    private final Map<String, UserData> usersMap;

    public Users(List<UserData> users) {
        usersMap = users.stream().collect(toMap(
                UserData::userId, 
                Function.identity()));
    }
    
    public UserData getUser(String userId) {
        return usersMap.getOrDefault(userId, UserData.unknown(userId));
    }
    
    public List<UserData> list() {
        return usersMap.values().stream().sorted(comparing(UserData::name)).collect(toList());
    }

    public boolean hasUserWithEmail(UserData userData) {
        return usersMap.values().stream().anyMatch(user -> user.email().equals(userData.email()) && !user.userId().equals(userData.userId()));
    }
    
    public boolean hasUserWithId(String userId) {
        return usersMap.values().stream().anyMatch(user -> user.userId().equals(userId));
    }

    public List<String> adminEmails() {
        return usersMap.values().stream()
                .filter(user -> user.role() == Role.ADMIN)
                .map(UserData::email)
                .collect(toList());
    }

    public List<UserData> loadNewlyRegistered() {
        return usersMap.values().stream()
                .filter(user -> user.status() == Status.REGISTERED)
                .collect(toList());
    }

}
