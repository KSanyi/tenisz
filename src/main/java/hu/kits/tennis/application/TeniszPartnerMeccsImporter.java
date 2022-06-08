package hu.kits.tennis.application;

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
import java.util.TreeMap;

import hu.kits.tennis.domain.utr.BookedMatch;
import hu.kits.tennis.domain.utr.MatchResult;
import hu.kits.tennis.domain.utr.MatchResult.SetResult;
import hu.kits.tennis.domain.utr.Match;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.domain.utr.PlayerRepository;
import hu.kits.tennis.domain.utr.Players;
import hu.kits.tennis.domain.utr.UTRService;

public class TeniszPartnerMeccsImporter {

    private final PlayerRepository playerRepository;
    private final UTRService utrService;
    
    public TeniszPartnerMeccsImporter(ResourceFactory resourceFactory) {
        playerRepository = resourceFactory.getPlayerRepository();
        utrService = resourceFactory.getUTRService();
    }
    
    private Player findOrCreatePlayer(Players players, String playerName) {
        return players.findPlayer(playerName)
                .orElseGet(() -> playerRepository.saveNewPlayer(new Player(0, playerName, 0)));
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
        
        String page = loadPage(playerId);
        
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
        
        return setResult.map(setRes ->  new Match(0, null, null, date, player1, player2, new MatchResult(List.of(setRes))));
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
    
    private static String loadPage(int playerId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://www.teniszpartner.hu/?site=competitions&task=jatekos_aktivitas"))
                .headers("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("player_id=" + playerId))
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
                utrService.deleteMatch(duplicate.get().playedMatch().id());
                System.out.println("Match deletd: " + duplicate.get().playedMatch());
            } else {
                //System.out.println("No duplicate found for: " + match);
            }
            
        }
        
    }

}
