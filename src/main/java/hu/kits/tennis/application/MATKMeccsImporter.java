package hu.kits.tennis.application;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.domain.tournament.Organizer;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.Tournament.Type;
import hu.kits.tennis.domain.tournament.TournamentInfo;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.domain.utr.Match;
import hu.kits.tennis.domain.utr.MatchInfo;
import hu.kits.tennis.domain.utr.MatchRepository;
import hu.kits.tennis.domain.utr.MatchResult;
import hu.kits.tennis.domain.utr.MatchResult.SetResult;
import hu.kits.tennis.domain.utr.MatchService;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.domain.utr.PlayerRepository;
import hu.kits.tennis.domain.utr.Players;

public class MATKMeccsImporter {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy", new Locale("HU"));
    
    private final PlayerRepository playerRepository;
    private final MatchService matchService;
    private final MatchRepository matchRepository;
    private final TournamentService tournamentService;
    
    public MATKMeccsImporter(ResourceFactory resourceFactory) {
        playerRepository = resourceFactory.getPlayerRepository();
        matchService = resourceFactory.getMatchService();
        tournamentService = resourceFactory.getTournamentService();
        matchRepository = resourceFactory.getMatchRepository();
    }
    
    public void importMatches() throws IOException {
        
        List<String> lines = Files.readAllLines(Paths.get("c:\\Users\\kocso\\Desktop\\Tenisz\\MATK\\meccsek.txt"));
        
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
                
                matchService.saveMatch(playedMatch);
                
                //System.out.println(date + " " + playerOne + " " + playerTwo + " " + score1 + " " + score2 + " " + type);                
            }
        } catch(Exception ex) {
            logger.error("Error parsing line " + rowNum + ": " + line + ": " + ex);
        }
    }
    
    private Player findOrCreatePlayer(Players players, String playerName) {
        return players.findPlayer(playerName)
                .orElseGet(() -> playerRepository.saveNewPlayer(Player.createNew(playerName)));
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
    
    public void createTournaments() {
        
        Map<LocalDate, List<MatchInfo>> matchesByDate = matchService.loadAllMatches().stream()
                .filter(match -> match.tournamentInfo().equals(TournamentInfo.UNKNOWN))
                .collect(Collectors.groupingBy(match -> match.date()));
        
        for(LocalDate date : matchesByDate.keySet()) {
            
            List<MatchInfo> matches = matchesByDate.get(date);
            Tournament tournament = tournamentService.createTournament(Organizer.MATK, "MATK Napi verseny " + Formatters.formatDate(date), "", date, Type.NA, 1);
            List<Player> players = findPlayers(matches);
            for(MatchInfo match : matches) {
                matchRepository.updateTournament(match.id(), tournament.id(), 1, matches.indexOf(match) + 1);
            }
           
            tournamentService.updateContestants(tournament, players);
        }
    }
    
    private static List<Player> findPlayers(List<MatchInfo> matches) {
        return matches.stream().flatMap(m -> Stream.of(m.player1(), m.player2())).distinct().toList();
    }
    
    

}
