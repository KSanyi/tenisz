package hu.kits.tennis.application.tasks;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.common.LocaleUtil;
import hu.kits.tennis.domain.ktr.KTR;
import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.match.MatchInfo;
import hu.kits.tennis.domain.match.MatchRepository;
import hu.kits.tennis.domain.match.MatchResult;
import hu.kits.tennis.domain.match.MatchResult.SetResult;
import hu.kits.tennis.domain.match.MatchService;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.Player.Address;
import hu.kits.tennis.domain.player.Player.Contact;
import hu.kits.tennis.domain.player.PlayerRepository;
import hu.kits.tennis.domain.player.Players;
import hu.kits.tennis.domain.tournament.Contestant;
import hu.kits.tennis.domain.tournament.Organization;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentParams;
import hu.kits.tennis.domain.tournament.TournamentParams.Level;
import hu.kits.tennis.domain.tournament.TournamentParams.Structure;
import hu.kits.tennis.domain.tournament.TournamentParams.Surface;
import hu.kits.tennis.domain.tournament.TournamentParams.Type;
import hu.kits.tennis.domain.tournament.TournamentParams.VenueType;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.domain.tournament.TournamentSummary;
import hu.kits.tennis.domain.tournament.TournamentSummary.CourtInfo;
import hu.kits.tennis.infrastructure.ApplicationContext;

@SuppressWarnings("unused")
public class KVTKMeccsImporter {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd", LocaleUtil.HUN_LOCALE);
    
    private final PlayerRepository playerRepository;
    private final MatchService matchService;
    private final MatchRepository matchRepository;
    private final TournamentService tournamentService;
    
    private Players players;
    private Map<String, String> tournamentIdsByName = new HashMap<>();
    
    public KVTKMeccsImporter(ApplicationContext resourceFactory) {
        playerRepository = resourceFactory.getPlayerRepository();
        matchService = resourceFactory.getMatchService();
        tournamentService = resourceFactory.getTournamentService();
        matchRepository = resourceFactory.getMatchRepository();
    }
    
    public void importMatches() throws IOException {
        
        List<String> lines = Files.readAllLines(Paths.get("c:\\Users\\kocso\\Desktop\\Tenisz\\KVTK\\meccsek.txt"));
        
        players = playerRepository.loadAllPlayers();
        logger.info("{} players loaded", players.entries().size());
        
        
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
            
            String tournamentId = findOrCreateTournament(date, name);
            
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
            
            return new Match(0, tournamentId, null, null, date, player1, player2, result);
        } catch(Exception ex) {
            logger.error("Error parsing line " + rowNum + ": " + line + ": " + ex);
            return null;
        }
    }
    
    private static Integer parseGames(String games) {
        return games.isEmpty() ? null :Integer.parseInt(games);
    }
    
    private String findOrCreateTournament(LocalDate date, String tournamentName) {
        String tournamentId = tournamentIdsByName.get(tournamentName);
        if(tournamentId == null) {
            TournamentParams params = new TournamentParams(Organization.KVTK, Type.DAILY, Level.L90, Level.L1000, date, tournamentName, "Mini Garros", new CourtInfo(4, Surface.CLAY, VenueType.INDOOR), Structure.NA, 1, "");
            logger.info("Saving tournament " + params);
            Tournament tournament = tournamentService.createTournament(params);
            tournamentId = tournament.id();
            tournamentIdsByName.put(tournamentName, tournamentId);
        }
        return tournamentId;
    }

    public void setupTournaments() {
        
        List<TournamentSummary> tournamentsNotSetup = tournamentService.loadDailyTournamentSummariesList().stream()
                .filter(tournament -> tournament.numberOfPlayers() == 0)
                .collect(toList());
        
        for(TournamentSummary tournamentSummary : tournamentsNotSetup) {
            List<MatchInfo> matches = matchService.loadMatchesOfTournament(tournamentSummary.id());
            List<Player> players = findPlayers(matches);
            for(MatchInfo match : matches) {
                matchRepository.updateTournament(match.id(), tournamentSummary.id(), 1, matches.indexOf(match) + 1);
            }
           
            Tournament tournament = tournamentService.findTournament(tournamentSummary.id()).get();
            tournamentService.updateContestants(tournament, Contestant.of(players));
        }
    }
    
    private static List<Player> findPlayers(List<MatchInfo> matches) {
        return matches.stream().flatMap(m -> Stream.of(m.player1(), m.player2())).distinct().toList();
    }

    public void importPlayers() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("c:\\Users\\kocso\\Desktop\\Tenisz\\KVTK\\jatekosok.txt"));
        
        for(String line : lines) {
            playerRepository.saveNewPlayer(new Player(null, line, null, KTR.UNDEFINED, Set.of(Organization.KVTK)));    
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
                    updatedPlayer = new Player(id, updatedPlayer.name(), new Contact(email, phone, Address.EMPTY, ""), updatedPlayer.startingKTR(), updatedPlayer.organisations());
                    playerRepository.updatePlayer(updatedPlayer);
                }  
            }
        }
        
    }

}
