package hu.kits.tennis.testutil;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.match.MatchResult;
import hu.kits.tennis.domain.match.MatchResult.SetResult;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.Player.Contact;
import hu.kits.tennis.domain.utr.UTR;

public class TestUtil {

    private static Random random = new Random();
    
    public static final Player player1 = new Player(1, "P1", Contact.EMPTY, UTR.of(9.), Set.of());
    public static final Player player2 = new Player(2, "P2", Contact.EMPTY, UTR.of(9.), Set.of());
    public static final Player player3 = new Player(3, "P3", Contact.EMPTY, UTR.of(8.5), Set.of());
    public static final Player player4 = new Player(4, "P4", Contact.EMPTY, UTR.of(8.5), Set.of());
    public static final Player player5 = new Player(5, "P5", Contact.EMPTY, UTR.of(8.), Set.of());
    public static final Player player6 = new Player(6, "P6", Contact.EMPTY, UTR.of(8.), Set.of());
    public static final Player player7 = new Player(7, "P7", Contact.EMPTY, UTR.of(7.7), Set.of());
    public static final Player player8 = new Player(8, "P8", Contact.EMPTY, UTR.of(7.5), Set.of());
    
    public static final List<Player> players = List.of(player1, player2, player3, player4, player5, player6, player7, player8);
    
    public static List<Match> generateRandomTournament(LocalDate date, int numberOfMatches) {
        
        return IntStream.range(0, numberOfMatches).mapToObj(i -> generateRandomMatch(date)).toList();
    }
    
    public static Match generateRandomMatch(LocalDate date) {
        
        Player player1 = selectRandomPlayer();
        Player player2 = selectRandomPlayer();
        while(player1.equals(player2)) {
            player2 = selectRandomPlayer();
        }
        
        return new Match(null, null, null, null, date, player1, player2, generateMatchResult(player1, player2));
    }

    private static MatchResult generateMatchResult(Player player1, Player player2) {
        SetResult setResult = generateSetResult(player1, player2);
        
        return new MatchResult(List.of(setResult));
    }

    private static SetResult generateSetResult(Player player1, Player player2) {
        double player1UTR = player1.startingUTR().value();
        double player2UTR = player2.startingUTR().value();
        
        int player1Games = 0;
        int player2Games = 0;
        
        while(player1Games < 6 && player2Games < 6) {
            double value = random.nextDouble();
            if(value < player1UTR / (player1UTR + player2UTR)) {
                player1Games++;
            } else {
                player2Games++;
            }
        }
        
        return new SetResult(player1Games, player2Games);
    }

    private static Player selectRandomPlayer() {
        return players.get(random.nextInt(players.size()));
    }
    
    public static LocalDate date(String dateString) {
        return LocalDate.parse(dateString);
    }
    
    public static int findFreePort() {
        try {
            ServerSocket socket = new ServerSocket(0);
            int port = socket.getLocalPort();
            socket.close();
            return port;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
}
