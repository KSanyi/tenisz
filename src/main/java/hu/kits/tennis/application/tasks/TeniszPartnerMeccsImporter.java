package hu.kits.tennis.application.tasks;

import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import hu.kits.tennis.common.Formatters;
import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.match.MatchInfo;
import hu.kits.tennis.domain.match.MatchResult;
import hu.kits.tennis.domain.match.MatchService;
import hu.kits.tennis.domain.match.MatchResult.SetResult;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.PlayerRepository;
import hu.kits.tennis.domain.player.Players;
import hu.kits.tennis.domain.player.Player.Contact;
import hu.kits.tennis.domain.tournament.Organization;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.Tournament.Type;
import hu.kits.tennis.domain.tournament.TournamentInfo;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.domain.utr.BookedMatch;
import hu.kits.tennis.domain.utr.UTR;
import hu.kits.tennis.domain.utr.UTRService;
import hu.kits.tennis.infrastructure.ResourceFactory;

public class TeniszPartnerMeccsImporter {

    private final PlayerRepository playerRepository;
    private final UTRService utrService;
    private final TournamentService tournamentService;
    private final MatchService matchService;
    
    public TeniszPartnerMeccsImporter(ResourceFactory resourceFactory) {
        playerRepository = resourceFactory.getPlayerRepository();
        utrService = resourceFactory.getUTRService();
        tournamentService = resourceFactory.getTournamentService();
        matchService = resourceFactory.getMatchService();
    }
    
    public void importTournaments() throws Exception {
        String id = "763 Bikasdomb_Open____________";
        String page = loadTournamentPage(id);
        Files.write(Paths.get("c:\\Users\\kocso\\Desktop\\Tenisz\\TeniszPartner\\tournament_" + id + ".html"), page.getBytes());
    }
    
    public void importPlayers() throws Exception {
        String page = loadHistoryPage();
        page = afterFirst(page, "aktivitását");
        page = afterFirst(page, "</option>");
        HtmlFragment pageHtml = new HtmlFragment(page);
        String block = pageHtml.nextBlock("<option value=", "</option>");
        
        List<String> rows = new ArrayList<>();
        
        do {
            try {
                int id = Integer.parseInt(StringUtils.substringsBetween(block, "\"", "\"")[0]);
                String name = block.substring(block.indexOf(">") + 1);
                if(name.contains("<")) {
                    name = name.substring(0, name.indexOf("<"));
                }
                if(name.contains("(")) {
                    name = name.substring(0, name.indexOf("("));
                }
                rows.add(id + ";" + name.trim());
            } catch(Exception ex) {
                System.out.println(ex);
            }
            block = pageHtml.nextBlock("<option value=", "</option>");
        } while(!block.isBlank());
        
        Files.write(Paths.get("c:\\Users\\kocso\\Desktop\\Tenisz\\TeniszPartner\\players.csv"), rows);
    }
    
    public void importMatches() throws Exception {
        List<Match> matches = new ArrayList<>();
        for(var playerNameAndId : loadPlayerIds().entrySet()) {
            List<Match> matchesForPlayer = parseMatches(playerNameAndId.getValue());
            matches.addAll(matchesForPlayer);
            matchesForPlayer.forEach(utrService::calculatUTRAndSaveMatch);
            System.out.println(matchesForPlayer.size() + " matches added for " + playerNameAndId.getKey());
            Thread.sleep(1000);
        }
        
        //matches.stream().sorted(Comparator.comparing(PlayedMatch::date)).forEach(utrService::calculatUTRAndSaveMatch);
    }
    
    private List<Match> parseMatches(int playerId) throws Exception {
        
        List<Match> matches = new ArrayList<>();
        
        String page = loadPlayersMatchesPage(playerId);
        
        //String page = new String(Files.readAllBytes(Paths.get("c:\\Users\\Sanyi\\Desktop\\alex.html")));
        
        page = afterFirst(page, "aktivitása");
        page = afterFirst(page, "</table>");
        
        HtmlFragment pageHtml = new HtmlFragment(page);
        HtmlFragment table = new HtmlFragment(pageHtml.nextBlock("<table", "</table"));
        
        String matchBlock = table.nextBlock("<tr ", "</tr>");
        while(!matchBlock.isBlank()) {
            
            try {
                parseMatch(matchBlock).ifPresent(matches::add);  
            } catch(Exception ex) {
                System.err.println("Error with block: " + matchBlock + " " + ex);
            }
            matchBlock = table.nextBlock("<tr ", "</tr>");
        }
        
        return matches;
        
    }
    
    private static Map<String, Integer> loadPlayerIds() throws IOException {
        
        return Files.lines(Paths.get("c:\\Users\\Sanyi\\Desktop\\teniszpartner players.txt"))
                .map(line -> line.split("\t"))
                .collect(toMap(
                    parts -> parts[1],
                    parts -> Integer.parseInt(parts[0]), 
                    (a, b) -> a, 
                    TreeMap::new));
    }
    
    private  Optional<Match> parseMatch(String matchBlock) {
        Players players = playerRepository.loadAllPlayers();
        
        HtmlFragment matchFragment = new HtmlFragment(matchBlock);
        String player1Name = cleanName(matchFragment.nextBlock("<td>", "</td>"));
        String player2Name = cleanName(matchFragment.nextBlock("<td>", "</td>"));
        
        Player player1 = findOrCreatePlayer(players, player1Name);
        Player player2 = findOrCreatePlayer(players, player2Name);
        
        String resultString = matchFragment.nextBlock("<td>", "</td>").trim();
        matchFragment.nextBlock("<td>", "</td>");
        matchFragment.nextBlock("<td>", "</td>");
        String dateString = matchFragment.nextBlock("<td>", "</td>");
        
        LocalDate date = LocalDate.parse(dateString);
        
        Optional<SetResult> setResult = parseSetResult(resultString);
        
        return setResult.map(setRes ->  new Match(0, null, null, null, date, player1, player2, new MatchResult(List.of(setRes))));
    }
    
    private Player findOrCreatePlayer(Players players, String playerName) {
        return players.findPlayer(playerName)
                .orElseGet(() -> playerRepository.saveNewPlayer(new Player(0, playerName, Contact.EMPTY, UTR.UNDEFINED, Set.of(Organization.KVTK))));
    }
    
    private static Optional<SetResult> parseSetResult(String resultString) {
        String parts[] = resultString.split(":");
        int player1Games = Integer.valueOf(parts[0].trim());
        int player2Games = Integer.valueOf(parts[1].trim());
        
        if(player1Games + player2Games > 5) {
            return Optional.of(new SetResult(player1Games, player2Games));
        } else {
            return Optional.empty();
        }
    }
    
    private static String cleanName(String name) {
        int index = name.indexOf("<a");
        return index > -1 ? name.substring(0, index) : name;
    }
    
    private static String loadPlayersMatchesPage(int playerId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://www.teniszpartner.hu/?site=competitions&task=jatekos_aktivitas"))
                .headers("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("player_id=" + playerId))
                .build();
        
        HttpClient client = HttpClient.newHttpClient();
        return client.send(request, BodyHandlers.ofString()).body();
    }
    
    private static String loadTournamentPage(String tournamentId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://www.teniszpartner.hu/embed_verseny.php?site=competitions&task=korverseny_lista"))
                .headers("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("station_id=" + tournamentId))
                .build();
        
        HttpClient client = HttpClient.newHttpClient();
        return client.send(request, BodyHandlers.ofString()).body();
    }
    
    private static String loadHistoryPage()  throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://www.teniszpartner.hu/?site=competitions&task=tortenelem"))
                .headers("Content-Type", "application/x-www-form-urlencoded")
                .GET()
                .build();
        
        HttpClient client = HttpClient.newHttpClient();
        return client.send(request, BodyHandlers.ofString()).body();
    }
    
    private static String afterFirst(String page, String pattern) {
        return page.substring(page.indexOf(pattern) + pattern.length());
    }
    
    private static class HtmlFragment {
        
        private int cursor;
        private final String text;
        
        HtmlFragment(String text) {
            this.text = text;
        }
        
        String nextBlock(String startTag, String endTag) {
            
            int startTagIndex = text.indexOf(startTag, cursor);
            int endTagIndex = text.indexOf(endTag, cursor);
            
            if(startTagIndex < 0) return "";
            
            String result = text.substring(startTagIndex + startTag.length(), endTagIndex);
            cursor = endTagIndex + endTag.length();
            return result;
        }
        
    }

    public void cleanupDuplicates() {
        List<BookedMatch> matches = utrService.loadBookedMatches();
        for(var match : matches) {
            Optional<BookedMatch> duplicate = matches.stream()
                    .filter(m -> m.playedMatch().date().equals(match.playedMatch().date()))
                    .filter(m -> m.playedMatch().player1().equals(match.playedMatch().player1()))
                    .filter(m -> m.playedMatch().player2().equals(match.playedMatch().player2()))
                    .filter(m -> m.playedMatch().result().equals(match.playedMatch().result()))
                    .filter(m -> m.playedMatch().id()> match.playedMatch().id())
                    .findAny();
            
            if(duplicate.isPresent()) {
                matchService.deleteMatch(duplicate.get().playedMatch());
                System.out.println("Match deleted: " + duplicate.get().playedMatch());
            } else {
                //System.out.println("No duplicate found for: " + match);
            }
            
        }
    }
    
    public void createTournaments() {
        
        Map<LocalDate, List<MatchInfo>> matchesByDate = matchService.loadAllMatches().stream()
                .filter(match -> match.tournamentInfo().equals(TournamentInfo.UNKNOWN))
                .collect(Collectors.groupingBy(match -> match.date()));
        
        for(LocalDate date : matchesByDate.keySet()) {
            
            List<MatchInfo> matches = matchesByDate.get(date);
            Tournament tournament = tournamentService.createTournament(Organization.KVTK, "Teniszpartner verseny " + Formatters.formatDate(date), "Bikás Park", date, Type.NA, 1);
            List<Player> players = findPlayers(matches);
            tournamentService.updateContestants(tournament, players);
        }
    }

    private static List<Player> findPlayers(List<MatchInfo> matches) {
        return matches.stream().flatMap(m -> Stream.of(m.player1(), m.player2())).distinct().toList();
    }

}
