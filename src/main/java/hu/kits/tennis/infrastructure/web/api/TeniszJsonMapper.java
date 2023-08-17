package hu.kits.tennis.infrastructure.web.api;

import java.util.Collection;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import hu.kits.tennis.common.MathUtil;
import hu.kits.tennis.domain.ktr.KTR;
import hu.kits.tennis.domain.ktr.PlayerStats;
import hu.kits.tennis.domain.ktr.PlayerWithKTR;
import hu.kits.tennis.domain.ktr.KTRHistory.KTRHistoryEntry;
import hu.kits.tennis.domain.match.MatchInfo;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.tournament.Contestant;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentParams.Type;
import hu.kits.tennis.domain.tournament.TournamentSummary;
import hu.kits.tennis.domain.tournament.TournamentSummary.CourtInfo;
import hu.kits.tennis.infrastructure.ApplicationContext;
import io.javalin.plugin.json.JsonMapper;

public class TeniszJsonMapper implements JsonMapper {

    private final ApplicationContext applicationContext;
    
    public TeniszJsonMapper(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public String toJsonString(Object object) {
        return mapToJson(object).toString();
    }
    
    private JsonValue mapToJson(Object object) {
        
        if(object instanceof Collection) {
            Collection<?> collection = (Collection<?>)object;
            JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
            collection.stream().map(this::mapToJson).forEach(jsonArrayBuilder::add);
            return jsonArrayBuilder.build();
        } else if(object instanceof Player player) {
            return mapPlayerToJson(player);    
        } else if(object instanceof Tournament tournament) {
            return mapTournamentToJson(tournament);    
        } else if(object instanceof TournamentSummary tournamentSummary) {
            return mapTournamentSummaryToJson(tournamentSummary);    
        } else if(object instanceof PlayerWithKTR playerWithKTR) {
            return mapPlayerWithKTRToJson(playerWithKTR);    
        } else if(object instanceof MatchInfo matchInfo) {
            return mapMatchToJson(matchInfo);    
        }  else if(object instanceof PlayerStats playerStats) {
            return mapPlayerStatsToJson(playerStats);    
        } else if(object instanceof KTRHistoryEntry ktrHistoryEntry) {
            return mapKTRHistoryEntryToJson(ktrHistoryEntry);    
        } else if(object instanceof String string) {
            return Json.createValue(string);
        } else if(object instanceof Double number) {
            return Json.createValue(number);
        } else if(object instanceof Integer number) {
            return Json.createValue(number);
        } else {
            throw new IllegalArgumentException("Can not convert " + object.getClass() + " to JsonValue");
        }
    }
    
    private static JsonObject mapPlayerToJson(Player player) {
        
        if(player == null) {
            return JsonObject.EMPTY_JSON_OBJECT;
        } else {
            return Json.createObjectBuilder()
                    .add("id", player.id())
                    .add("email", player.contact().email())
                    .add("name", player.name())
                    .build();
        }
    }

    private static JsonObject mapPlayerWithKTRToJson(PlayerWithKTR playerWithKTR) {
        
        return Json.createObjectBuilder()
                .add("player", mapPlayerToJson(playerWithKTR.player()))
                .add("numberOfMatches", playerWithKTR.numberOfMatches())
                .add("numberOfWins", playerWithKTR.numberOfWins())
                .add("numberOfTrophies", playerWithKTR.numberOfTrophies())
                .add("rank", playerWithKTR.rank())
                .add("ktr", mapKTRToDouble(playerWithKTR.ktr()))
                .add("ktrOneWeekAgo", mapKTRToDouble(playerWithKTR.ktrOneWeekAgo()))
                .add("ktrChange", mapKTRToDouble(playerWithKTR.ktrChange()))
                .build();
    }
    
    private static JsonObject mapMatchToJson(MatchInfo matchInfo) {
        
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder()
                .add("id", matchInfo.id());
        
        if(matchInfo.date() != null) {
            jsonObjectBuilder.add("date", matchInfo.date().toString());    
        }
        
        jsonObjectBuilder
                .add("tournamentId", matchInfo.tournamentInfo().id())
                .add("tournamentName", matchInfo.tournamentInfo().name())
                .add("player1", mapPlayerToJson(matchInfo.player1()))
                .add("player1KTR", mapKTRToDouble(matchInfo.player1KTR()))
                .add("player2", mapPlayerToJson(matchInfo.player2()))
                .add("player2KTR", mapKTRToDouble(matchInfo.player2KTR()));
        
        if(matchInfo.result() != null) {
            jsonObjectBuilder = jsonObjectBuilder
                    .add("result", matchInfo.result().toString())
                    .add("upset", matchInfo.isUpset());
            if(matchInfo.matchKTRForPlayer1() != null) {
                jsonObjectBuilder = jsonObjectBuilder
                        .add("matchKTRForPlayer1", mapKTRToDouble(matchInfo.matchKTRForPlayer1()))
                        .add("matchKTRForPlayer2", mapKTRToDouble(matchInfo.matchKTRForPlayer2()));
            }
        }
        
        return jsonObjectBuilder.build();
    }
    
    private static JsonObject mapTournamentSummaryToJson(TournamentSummary tournamentSummary) {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("id", tournamentSummary.id())
                .add("name", tournamentSummary.name())
                .add(tournamentSummary.type() == Type.DAILY ? "date" : "startDate", tournamentSummary.date().toString())
                .add("levelFrom", tournamentSummary.levelFrom().name())
                .add("levelTo", tournamentSummary.levelTo().name());
        
        if(tournamentSummary.type() == Type.DAILY) {
            builder
                .add("venue", tournamentSummary.venue())
                .add("courtInfo", mapCourtInfoToJson(tournamentSummary.courtInfo()));
        }
        
        return builder
            .add("type", tournamentSummary.type().name())
            .add("description", tournamentSummary.description())
            .add("status", tournamentSummary.status().name())
            .add("winner", mapPlayerToJson(tournamentSummary.winner()))
            .add("numberOfPlayers", tournamentSummary.numberOfPlayers())
            .add("numberOfMatchesPlayed", tournamentSummary.numberOfMatchesPlayed())
            .build();
    }
    
    private JsonObject mapTournamentToJson(Tournament tournament) {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("id", tournament.id())
                .add("name", tournament.params().name())
                .add(tournament.params().type() == Type.DAILY ? "date" : "startDate", tournament.params().date().toString())
                .add("levelFrom", tournament.params().levelFrom().name())
                .add("levelTo", tournament.params().levelTo().name());
        
        if(tournament.params().type() == Type.DAILY) {
            builder
                .add("venue", tournament.params().venue())
                .add("courtInfo", mapCourtInfoToJson(tournament.params().courtInfo()));
        }
        
        // TODO fix
        List<MatchInfo> matches = applicationContext.getMatchService().loadMatchesOfTournament(tournament.id());
        
        return builder
            .add("type", tournament.params().type().name())
            .add("description", tournament.params().description())
            .add("status", tournament.status().name())
            .add("players", mapToJson(tournament.simplePlayersLineup().stream().map(Contestant::player).toList()))
            .add("matches", mapToJson(matches))
            .build();
    }
    
    private static JsonValue mapCourtInfoToJson(CourtInfo courtInfo) {
        return Json.createObjectBuilder()
                .add("numberOfCourts", courtInfo.numberOfCourts())
                .add("surface", courtInfo.surface().name())
                .add("venueType", courtInfo.venueType().name())
                .build();
    }

    private JsonObject mapPlayerStatsToJson(PlayerStats playerStats) {
        
        return Json.createObjectBuilder()
                .add("player", mapPlayerToJson(playerStats.player()))
                .add("ktr", mapKTRToDouble(playerStats.ktrDetails().ktr()))
                .add("numberOfTournaments", playerStats.numberOfTournaments())
                .add("numberOfTrophies", playerStats.ktrDetails().numberOfTrophies())
                .add("numberOfWins", playerStats.numberOfWins())
                .add("winPercentage", MathUtil.roundToTwoDigits(playerStats.winPercentage()))
                .add("numberOfLosses", playerStats.numberOfLosses())
                .add("lossPercentage", MathUtil.roundToTwoDigits(playerStats.lossPercentage()))
                .add("numberOfGames", playerStats.numberOfGames())
                .add("numberOfGamesWon", playerStats.numberOfGamesWon())
                .add("gamesWinPercentage", MathUtil.roundToTwoDigits(playerStats.gamesWinPercentage()))
                .add("numberOfGamesLost", playerStats.numberOfGamesLost())
                .add("gamesLossPercentage", MathUtil.roundToTwoDigits(playerStats.gamesLossPercentage()))
                .add("matches", mapToJson(playerStats.matches()))
                .add("ktrRelevantMatchIds", mapToJson(playerStats.ktrDetails().relevantMatchIds()))
                .add("bestKTRMatchId", playerStats.bestKTRMatch().id())
                .add("worstKTRMatchId", playerStats.worstKTRMatch().id())
                .add("ktrHistory", mapToJson(playerStats.ktrHistory().entries()))
                .add("ktrHigh", mapToJson(playerStats.ktrHigh()))
                .add("rank", playerStats.rank())
                .build();
    }
    
    private static JsonObject mapKTRHistoryEntryToJson(KTRHistoryEntry ktrHistoryEntry) {
        return Json.createObjectBuilder()
                .add("date", ktrHistoryEntry.date().toString())
                .add("ktr", mapKTRToDouble(ktrHistoryEntry.ktr()))
                .build();
    }
    
    private static double mapKTRToDouble(KTR ktr) {
        return ktr.isDefinded() ? MathUtil.roundToTwoDigits(ktr.value()) : 0;
    }
    
}
