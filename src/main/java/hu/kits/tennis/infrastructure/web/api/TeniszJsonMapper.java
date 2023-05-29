package hu.kits.tennis.infrastructure.web.api;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import hu.kits.tennis.common.MathUtil;
import hu.kits.tennis.domain.match.MatchInfo;
import hu.kits.tennis.domain.utr.PlayerStats;
import hu.kits.tennis.domain.utr.PlayerWithUTR;
import hu.kits.tennis.domain.utr.UTR;
import hu.kits.tennis.domain.utr.UTRHistory.UTRHistoryEntry;
import io.javalin.plugin.json.JsonMapper;

public class TeniszJsonMapper implements JsonMapper {

    @Override
    public String toJsonString(Object object) {
        return mapToJson(object).toString();
    }
    
    private static Object mapToJson(Object object) {
        
        if(object instanceof Collection) {
            Collection<?> collection = (Collection<?>)object; 
            return new JSONArray(collection.stream().map(TeniszJsonMapper::mapToJson).collect(toList()));
        } else if(object instanceof Map) {
            Map<?, ?> map = (Map<?, ?>)object;
            Map<?, ?> jsonEntriesMap = map.entrySet().stream().collect(toMap(
                    e -> e.getKey().toString(),
                    e -> TeniszJsonMapper.mapToJson(e.getValue()),
                    (a, b) -> a, LinkedHashMap::new));
            return new JSONObject(jsonEntriesMap);
        } else if(object instanceof PlayerWithUTR playerWithUTR) {
            return mapPlayerWithUTRToJson(playerWithUTR);    
        } else if(object instanceof MatchInfo matchInfo) {
            return mapMatchToJson(matchInfo);    
        } else if(object instanceof PlayerStats playerStats) {
            return mapPlayerStatsToJson(playerStats);    
        } else if(object instanceof UTRHistoryEntry utrHistoryEntry) {
            return mapUTRHistoryEntryToJson(utrHistoryEntry);    
        } else if(object instanceof UTR utr) {
            return mapUTRToDouble(utr);    
        }else {
            return object;
        }
    }
    
    private static JSONObject mapPlayerWithUTRToJson(PlayerWithUTR playerWithUtr) {
        
        return new JSONObject()
                .put("id", playerWithUtr.player().id())
                .put("name", playerWithUtr.player().name())
                .put("numberOfMatches", playerWithUtr.numberOfMatches())
                .put("numberOfWins", playerWithUtr.numberOfWins())
                .put("numberOfTrophies", playerWithUtr.numberOfTrophies())
                .put("rank", playerWithUtr.rank())
                .put("utr", mapUTRToDouble(playerWithUtr.utr()))
                .put("utrOneWeekAgo", mapUTRToDouble(playerWithUtr.utrOneWeekAgo()))
                .put("utrChange", mapUTRToDouble(playerWithUtr.utrChange()));
    }
    
    private static JSONObject mapMatchToJson(MatchInfo matchInfo) {
        
        JSONObject jsonObject = new JSONObject()
                .put("id", matchInfo.id())
                .put("date", matchInfo.date())
                .put("tournamentName", matchInfo.tournamentInfo().name())
                .put("player1Id", matchInfo.player1().id())
                .put("player1", matchInfo.player1().name())
                .put("player1UTR", mapUTRToDouble(matchInfo.player1UTR()))
                .put("player2", matchInfo.player2().name())
                .put("player2Id", matchInfo.player2().id())
                .put("player2UTR", mapUTRToDouble(matchInfo.player2UTR()));
        
        if(matchInfo.result() != null) {
            jsonObject = jsonObject
                    .put("result", matchInfo.result().toString())
                    .put("upset", matchInfo.isUpset());
            if(matchInfo.matchUTRForPlayer1() != null) {
                jsonObject = jsonObject
                        .put("matchUTRForPlayer1", mapUTRToDouble(matchInfo.matchUTRForPlayer1()))
                        .put("matchUTRForPlayer2", mapUTRToDouble(matchInfo.matchUTRForPlayer2()));
            }
        }
        
        return jsonObject;
    }
    
    private static JSONObject mapPlayerStatsToJson(PlayerStats playerStats) {
        
        return new JSONObject()
                .put("id", playerStats.player().id())
                .put("name", playerStats.player().name())
                .put("utr", playerStats.utrDetails().utr())
                .put("utrRelevantMatchIds", mapToJson(playerStats.utrDetails().relevantMatchIds()))
                .put("matches", mapToJson(playerStats.matches()))
                .put("numberOfTournaments", playerStats.numberOfTournaments())
                .put("numberOfTrophies", playerStats.utrDetails().numberOfTrophies())
                .put("numberOfWins", playerStats.numberOfWins())
                .put("winPercentage", MathUtil.roundToTwoDigits(playerStats.winPercentage()))
                .put("numberOfLosses", playerStats.numberOfLosses())
                .put("lossPercentage", MathUtil.roundToTwoDigits(playerStats.lossPercentage()))
                .put("numberOfGames", playerStats.numberOfGames())
                .put("numberOfGamesWon", playerStats.numberOfGamesWon())
                .put("gamesWinPercentage", MathUtil.roundToTwoDigits(playerStats.gamesWinPercentage()))
                .put("numberOfGamesLost", playerStats.numberOfGamesLost())
                .put("gamesLossPercentage", MathUtil.roundToTwoDigits(playerStats.gamesLossPercentage()))
                .put("utrHigh", mapToJson(playerStats.utrHigh()))
                .put("bestUTRMatchId", playerStats.bestUTRMatch().id())
                .put("worstUTRMatchId", playerStats.worstUTRMatch().id())
                .put("utrHistory", mapToJson(playerStats.utrHistory().entries()));
    }
    
    private static JSONObject mapUTRHistoryEntryToJson(UTRHistoryEntry utrHistoryEntry) {
        return new JSONObject()
                .put("date", utrHistoryEntry.date())
                .put("utr", mapUTRToDouble(utrHistoryEntry.utr()));
    }
    
    private static double mapUTRToDouble(UTR utr) {
        return utr.isDefinded() ? MathUtil.roundToTwoDigits(utr.value()) : 0;
    }
    
}
