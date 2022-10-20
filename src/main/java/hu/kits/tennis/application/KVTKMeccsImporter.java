package hu.kits.tennis.application;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.domain.tournament.Organizer;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.Tournament.Type;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.domain.utr.BookedMatch;
import hu.kits.tennis.domain.utr.Match;
import hu.kits.tennis.domain.utr.MatchInfo;
import hu.kits.tennis.domain.utr.MatchRepository;
import hu.kits.tennis.domain.utr.MatchResult;
import hu.kits.tennis.domain.utr.MatchResult.SetResult;
import hu.kits.tennis.domain.utr.MatchService;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.domain.utr.Player.Contact;
import hu.kits.tennis.domain.utr.PlayerRepository;
import hu.kits.tennis.domain.utr.Players;
import hu.kits.tennis.domain.utr.UTR;

public class KVTKMeccsImporter {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd", new Locale("HU"));
    
    private final PlayerRepository playerRepository;
    private final MatchService matchService;
    private final MatchRepository matchRepository;
    private final TournamentService tournamentService;
    
    private Players players;
    private Map<String, Tournament> tournaments;
    
    public KVTKMeccsImporter(ResourceFactory resourceFactory) {
        playerRepository = resourceFactory.getPlayerRepository();
        matchService = resourceFactory.getMatchService();
        tournamentService = resourceFactory.getTournamentService();
        matchRepository = resourceFactory.getMatchRepository();
    }
    
    public void importMatches() throws IOException {
        
        List<String> lines = Files.readAllLines(Paths.get("c:\\Users\\kocso\\Desktop\\Tenisz\\KVTK\\meccsek.txt"));
        
        players = playerRepository.loadAllPlayers();
        logger.info("{} players loaded", players.entries().size());
        
        tournaments = tournamentService.loadAllTournaments().stream()
                .filter(tournament -> tournament.organizer() == Organizer.KVTK)
                .filter(tournament -> tournament.organizer() != Organizer.KVTK)
                .collect(toMap(Tournament::name, Function.identity()));
        
        //List<BookedMatch> allMatches = matchRepository.loadAllBookedMatches();
        
        List<Match> matches = new ArrayList<>();
        for(int i=0;i<lines.size();i++) {
            String line = lines.get(i);
            Match match = processMatchLine(i+1, line);
            logger.info("Match {} parsed: {}", i, match);
            if(match != null) {
                //if(isDuplicate(allMatches, match)) {
                //    logger.info("Match is duplicate");
                //} else {
                    matches.add(match);                        
                //}
            }
        }
        
        if(matches.isEmpty()) {
            logger.info("No matches to save");
        } else {
            logger.info("Saving matches");
            matchRepository.save(matches);
            logger.info("{} matches saved", matches.size());    
        }
    }

    private static boolean isDuplicate(List<BookedMatch> allMatches, Match match) {
        return allMatches.stream().anyMatch(m -> isTheSameMatch(match, m.playedMatch()));
    }
    
    private static boolean isTheSameMatch(Match match1, Match match2) {
        return match1.date().equals(match2.date()) && match1.tournamentId().equals(match2.tournamentId()) 
                && match1.player1().equals(match2.player1())
                && match1.player2().equals(match2.player2()) && match1.result().equals(match2.result());
    }

    private Match processMatchLine(int rowNum, String line) {
        try {
            String[] parts = line.split("\t");
            LocalDate date = LocalDate.parse(parts[0], DATE_FORMAT);
            
            int playerOneId = Integer.parseInt(parts[2]);
            int playerTwoId = Integer.parseInt(parts[4]);
            Integer score1_1 = parseGames(parts[5]);
            Integer score2_1 = parseGames(parts[6]);
            Integer score1_2 = parseGames(parts[7]);
            Integer score2_2 = parseGames(parts[8]);
            Integer score1_3 = parseGames(parts[9]);
            Integer score2_3 = parseGames(parts[10]);
            String name = parts[12] + " " + parts[13];
            
            Tournament tournament = findOrCreateTournament(date, name);
            
            Player player1 = new Player(playerOneId, "", null, null, Set.of());//findOrCreatePlayer(playerOne);
            Player player2 = new Player(playerTwoId, "", null, null, Set.of());//
                
            List<SetResult> setResults = new ArrayList<>();
            setResults.add(new SetResult(score1_1, score2_1));
            if(score1_2 != null) {
                setResults.add(new SetResult(score1_2, score2_2));    
            }
            if(score1_3 != null) {
                setResults.add(new SetResult(score1_3, score2_3));
            }
            MatchResult result = new MatchResult(setResults);
            
            return new Match(0, tournament.id(), null, null, date, player1, player2, result);
        } catch(Exception ex) {
            logger.error("Error parsing line " + rowNum + ": " + line + ": " + ex);
            return null;
        }
    }
    
    private static Integer parseGames(String games) {
        return games.isEmpty() ? null :Integer.parseInt(games);
    }
    
    private Tournament findOrCreateTournament(LocalDate date, String name) {
        String tournamentName = "KVTK " + name + " " + Formatters.formatDate(date);
        Tournament tournament = tournaments.get(tournamentName);
        if(tournament == null) {
            tournament = tournamentService.createTournament(Organizer.KVTK, tournamentName, "", date, Type.NA, 1);
            logger.info("Saving tournament " + tournament);
            tournaments.put(tournamentName, tournament);
        }
        return tournament;
    }

    private Player findOrCreatePlayer(String playerName) {
        Optional<Player> player = players.findPlayer(playerName);
        if(player.isPresent()) {
            return player.get();
        } else {
            logger.info("Saving new player: {}", playerName);
            Player newPlayer = playerRepository.saveNewPlayer(Player.createNew(playerName));
            logger.info("New player saved: {}", newPlayer);
            players = players.add(newPlayer);
            return newPlayer;
        }
    }
    
    public void setupTournaments() {
        
        List<Tournament> tournamentsNotSetup = tournamentService.loadAllTournaments().stream()
                .filter(tournament -> tournament.organizer() == Organizer.KVTK)
                .filter(tournament -> tournament.contestants().isEmpty())
                .collect(toList());
        
        for(Tournament tournament : tournamentsNotSetup) {
            List<MatchInfo> matches = matchService.loadMatchesOfTournament(tournament.id());
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

    public void importPlayers() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("c:\\Users\\kocso\\Desktop\\Tenisz\\KVTK\\jatekosok.txt"));
        
        for(String line : lines) {
            playerRepository.saveNewPlayer(new Player(null, line, null, UTR.UNDEFINED, Set.of(Organizer.KVTK)));    
        }
        
        
    }

    public void importContactData() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("c:\\Users\\kocso\\Desktop\\Tenisz\\KVTK\\contact.txt"));
        
        for(String line : lines) {
            String[] parts = line.split("\t");
            if(parts.length == 3) {
                int id = Integer.parseInt(parts[0]);
                String phone = parts[1];
                String email = parts[2];
                Optional<Player> p = playerRepository.findPlayer(id);
                if(p.isEmpty()) {
                    System.err.println("Cant find player with id " + id);
                } else {
                    Player updatedPlayer = p.get();
                    updatedPlayer = new Player(id, updatedPlayer.name(), new Contact(email, phone, ""), updatedPlayer.startingUTR(), updatedPlayer.organisations());
                    playerRepository.updatePlayer(updatedPlayer);
                }  
            }
        }
        
    }

}
