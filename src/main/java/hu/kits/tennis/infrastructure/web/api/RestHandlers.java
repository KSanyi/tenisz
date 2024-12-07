package hu.kits.tennis.infrastructure.web.api;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

import hu.kits.tennis.application.usecase.AllMatchesUseCase;
import hu.kits.tennis.common.StringUtil;
import hu.kits.tennis.domain.ktr.PlayerStats;
import hu.kits.tennis.domain.ktr.PlayerWithKTR;
import hu.kits.tennis.domain.ktr.KTRService;
import hu.kits.tennis.domain.match.MatchInfo;
import hu.kits.tennis.domain.match.MatchResult;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.PlayerRepository;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.domain.tournament.TournamentSummary;
import hu.kits.tennis.infrastructure.ApplicationContext;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;

class RestHandlers {

    private final PlayerRepository playerRepository;
    private final KTRService ktrService;
    private final TournamentService tournamentService;
    private final AllMatchesUseCase allMatchesUseCase;
    
    RestHandlers(ApplicationContext applicationContext) {
        playerRepository = applicationContext.getPlayerRepository();
        ktrService = applicationContext.getKTRService();
        tournamentService = applicationContext.getTournamentService();
        allMatchesUseCase = applicationContext.getAllMatchesUseCase();
    }
    
    void listAllMatches(Context context) {
        List<MatchInfo> allMatches = allMatchesUseCase.loadAllMatches();
        context.json(allMatches);
    }
    
    void listAllDailyTournaments(Context context) {
        List<TournamentSummary> tournamentSummaries = tournamentService.loadDailyTournamentSummariesList();
        context.json(tournamentSummaries);
    }
    
    void listAllTourTournaments(Context context) {
        List<TournamentSummary> tournamentSummaries = tournamentService.loadTourTournamentSummariesList();
        context.json(tournamentSummaries);
    }
    
    void loadTournamentDetails(Context context) {
        Optional<Tournament> tournament = tournamentService.findTournament(context.pathParam("tournamentId"));
        if(tournament.isEmpty()) {
            context.status(HttpCode.NOT_FOUND);
        } else {
            context.json(tournament.get());    
        }
    }
    
    void calculateKTRRanking(Context context) {
        List<PlayerWithKTR> ktrRanking = ktrService.calculateKTRRanking();
        context.json(ktrRanking);
    }
    
    void playerStats(Context context) {
        int playerId = Integer.parseInt(context.pathParam("playerId"));
        Optional<Player> player = playerRepository.findPlayer(playerId);
        if(player.isEmpty()) {
            context.status(HttpCode.NOT_FOUND);
        } else {
            PlayerStats playerStats = ktrService.loadPlayerStats(player.get());
            context.json(playerStats);  
        }
    }
    
    void listAllPlayersWithKTRInCSV(Context context) {
        List<PlayerWithKTR> playersWithKTR = ktrService.calculateKTRRanking();
        
        String content = playersWithKTR.stream()
                .sorted((p1, p2) -> StringUtil.HUN_COLLATOR.compare(p1.player().name(), p2.player().name()))
                .map(playerWithKTR -> createCsvRow(playerWithKTR))
                .collect(Collectors.joining("\n"));
        
        context.result(content);
        context.contentType(ContentType.TEXT_CSV);
    }
    
    private static String createCsvRow(PlayerWithKTR playerWithKTR) {
        
        return createCsvRow(
                playerWithKTR.player().name(),
                String.valueOf(playerWithKTR.player().id()),
                playerWithKTR.player().contact().phone(),
                playerWithKTR.player().contact().email(), 
                playerWithKTR.ktr().toString().replace(".", ","),
                String.valueOf(playerWithKTR.numberOfMatches()),
                playerWithKTR.lastMatchDate().map(LocalDate::toString).orElse(""));
    }
    
    void listAllMatchesInCSV(Context context) {
        List<MatchInfo> allMatches = allMatchesUseCase.loadAllMatches();
        
        String content = allMatches.stream()
                .sorted(Comparator.comparing(MatchInfo::date, Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(matchInfo -> createCsvRow(matchInfo))
                .collect(Collectors.joining("\n"));
        
        context.result(content);
        context.contentType(ContentType.TEXT_CSV);
    }
    
    private static String createCsvRow(MatchInfo matchInfo) {
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY/MM/DD");
        
        String[] resultParts = toParts(matchInfo.result());
        
        return createCsvRow(
                matchInfo.date() != null ? matchInfo.date().format(formatter) : "",
                matchInfo.player1().name(),
                String.valueOf(matchInfo.player1().id()),
                matchInfo.player2().name(),
                String.valueOf(matchInfo.player2().id()),
                resultParts[0], resultParts[1], resultParts[2], resultParts[3], resultParts[4], resultParts[5],
                matchInfo.tournamentInfo().name());
    }
    
    private static String[] toParts(MatchResult result) {
        String[] games = new String[6];
        Arrays.fill(games, "");
        if(result != null) {
            for(int i=0;i<result.setResults().size();i++) {
                games[i*2] = String.valueOf(result.setResults().get(i).player1Score());
                games[i*2+1] = String.valueOf(result.setResults().get(i).player2Score());
            }
        } 
        return games;
    }

    private static String createCsvRow(String ... values) {
        return Arrays.stream(values).collect(Collectors.joining(";"));
    }
    
    void redirectToVaadin(Context context) {
        context.redirect("/ui/");
    }
    
}
