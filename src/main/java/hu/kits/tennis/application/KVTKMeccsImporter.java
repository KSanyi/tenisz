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
import hu.kits.tennis.domain.utr.PlayerRepository;
import hu.kits.tennis.domain.utr.Players;

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
                .collect(toMap(Tournament::name, Function.identity()));
        
        List<BookedMatch> allMatches = matchRepository.loadAllBookedMatches();
        
        List<Match> matches = new ArrayList<>();
        for(int i=1;i<lines.size();i++) {
            String line = lines.get(i);
            Match match = processMatchLine(i+1, line);
            logger.info("Match {} parsed: {}", i, match);
            if(match != null) {
                if(isDuplicate(allMatches, match)) {
                    logger.info("Match is duplicate");
                } else {
                    matches.add(match);                        
                }
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
            
            String playerOne = parts[1];
            String playerTwo = parts[2];
            int score1 = Integer.parseInt(parts[3]);
            int score2 = Integer.parseInt(parts[4]);
            String level = parts[11];
            
            Tournament tournament = findOrCreateTournament(date, level);
            
            Player player1 = findOrCreatePlayer(playerOne);
            Player player2 = findOrCreatePlayer(playerTwo);
                
            return new Match(0, tournament.id(), null, null, date, player1, player2, new MatchResult(List.of(new SetResult(score1, score2))));
        } catch(Exception ex) {
            logger.error("Error parsing line " + rowNum + ": " + line + ": " + ex);
            return null;
        }
    }
    
    private Tournament findOrCreateTournament(LocalDate date, String level) {
        String tournamentName = "KVTK Napi " + level + " verseny " + Formatters.formatDate(date);
        Tournament tournament = tournaments.get(tournamentName);
        if(tournament == null) {
            tournament = tournamentService.createTournament(Organizer.KVTK, tournamentName, "", date, Type.NA, 1);
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

}
