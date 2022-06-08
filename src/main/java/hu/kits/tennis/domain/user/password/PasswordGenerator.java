package hu.kits.tennis.domain.user.password;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class PasswordGenerator {

    private static final Random random = new Random();
    
    public static String generateRandomPassword() {
        
        int numberOfLowerCaseLetters = 5;
        int numberOfUpperCaseLetters = 1;
        int numberOfNumbers = 2;
        
        List<Character> chars = new ArrayList<>();
        for(int i=0;i<numberOfLowerCaseLetters;i++) {
            chars.add(createRandomChar());
        }
        for(int i=0;i<numberOfUpperCaseLetters;i++) {
            chars.add(Character.toUpperCase(createRandomChar()));
        }
        for(int i=0;i<numberOfNumbers;i++) {
            chars.add(createRandomDigit());
        }
        
        Collections.shuffle(chars);
        
        String password = chars.stream().map(Object::toString).collect(Collectors.joining());
        
        return password;
    }
    
    private static char createRandomDigit() {
        return (char)(random.nextInt(10) + '0');
    }
    
    private static char createRandomChar() {
        return (char)(random.nextInt('z' - 'a' + 1) + 'a');
    }
    
}
