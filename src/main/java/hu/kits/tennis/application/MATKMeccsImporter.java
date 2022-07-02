package hu.kits.tennis.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import hu.kits.tennis.domain.utr.Match;
import hu.kits.tennis.domain.utr.MatchResult;
import hu.kits.tennis.domain.utr.MatchResult.SetResult;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.domain.utr.PlayerRepository;
import hu.kits.tennis.domain.utr.Players;
import hu.kits.tennis.domain.utr.UTRService;

public class MATKMeccsImporter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");
    
    private final PlayerRepository playerRepository;
    private final UTRService utrService;
    
    public MATKMeccsImporter(ResourceFactory resourceFactory) {
        playerRepository = resourceFactory.getPlayerRepository();
        utrService = resourceFactory.getUTRService();
    }
    
    public void importMatches() throws IOException {
        
        List<String> lines = Files.readAllLines(Paths.get("c:\\Users\\Sanyi\\Desktop\\meccsek"));
        
        for(int i=1;i<lines.size();i++) {
            String line = lines.get(i);
            processMatchLine(i+1, line);
        }

    }

    private void processMatchLine(int rowNum, String line) {
        try {
            String[] parts = line.split("\t");
            LocalDate date = LocalDate.parse(parts[0], DATE_FORMAT);
            
            String playerOne = parts[1];
            String playerTwo = parts[3];
            int score1 = Integer.parseInt(parts[4]);
            int score2 = Integer.parseInt(parts[5]);
            String type = parts[10];
            
            if("NAPI Meccs".equals(type)) {
                
                Players players = playerRepository.loadAllPlayers();
                
                Player player1 = findOrCreatePlayer(players, playerOne);
                Player player2 = findOrCreatePlayer(players, playerTwo);
                
                Match playedMatch = new Match(0, null, null, null, date, player1, player2, new MatchResult(List.of(new SetResult(score1, score2))));
                
                utrService.calculatUTRAndSaveMatch(playedMatch);
                
                System.out.println(date + " " + playerOne + " " + playerTwo + " " + score1 + " " + score2 + " " + type);                
            }
        } catch(Exception ex) {
            System.err.println("Error parsing line " + rowNum + ": " + line);
        }
    }
    
    private Player findOrCreatePlayer(Players players, String playerName) {
        return players.findPlayer(playerName)
                .orElseGet(() -> playerRepository.saveNewPlayer(new Player(0, playerName, 0)));
    }
    
    public void importPlayers() throws IOException {
        
        List<String> lines = Files.readAllLines(Paths.get("c:\\Users\\Sanyi\\Desktop\\utr-groups.txt"));
        
        for(int i=1;i<lines.size();i++) {
            String line = lines.get(i);
            processPlayerLine(i+1, line);
        }

    }

    private void processPlayerLine(int rowNum, String line) {
        try {
            String[] parts = line.split("\t");
            
            String playerName = parts[1];
            int utrGroup = Integer.parseInt(parts[2]);
            
            playerRepository.saveNewPlayer(new Player(0, playerName, utrGroup));
        } catch(Exception ex) {
            System.err.println("Error parsing line " + rowNum + ": " + line);
        }
    }

}
