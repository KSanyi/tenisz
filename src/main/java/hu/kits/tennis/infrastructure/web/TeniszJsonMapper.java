package hu.kits.tennis.infrastructure.web;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import hu.kits.tennis.domain.utr.BookedMatch;
import hu.kits.tennis.domain.utr.PlayerWithUTR;
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
        } else if(object instanceof BookedMatch bookedMatch) {
            return mapMatchToJson(bookedMatch);    
        } else {
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
                .put("UTR", playerWithUtr.utr())
                .put("utrOneWeekAgo", playerWithUtr.utrOneWeekAgo())
                .put("utrChange", playerWithUtr.utrChange());
    }
    
    private static JSONObject mapMatchToJson(BookedMatch bookedMatch) {
        
        JSONObject jsonObject = new JSONObject()
                .put("id", bookedMatch.playedMatch().id())
                .put("date", bookedMatch.playedMatch().date())
                .put("player1", bookedMatch.playedMatch().player1().name())
                .put("player2", bookedMatch.playedMatch().player2().name());
        
        if(bookedMatch.playedMatch().result() != null) {
            jsonObject = jsonObject.put("result", bookedMatch.playedMatch().result().toString());
            if(bookedMatch.matchUTRForPlayer1() != null) {
                jsonObject = jsonObject
                        .put("matchUTRForPlayer1", bookedMatch.matchUTRForPlayer1().value())
                        .put("matchUTRForPlayer2", bookedMatch.matchUTRForPlayer2().value());
            }
        }
        
        return jsonObject;
    }
    
}
