package hu.kits.tennis.infrastructure.web.api;

import org.json.JSONException;
import org.json.JSONObject;

import hu.kits.tennis.infrastructure.web.api.Requests.PlayerCreationRequest;

public class RequestParser {

    public static PlayerCreationRequest parseUserCreationRequest(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return new PlayerCreationRequest(
                    jsonObject.getString("name"));
        } catch(JSONException | IllegalArgumentException ex) {
            throw new HttpServer.BadRequestException(ex.getMessage());
        }
    }

}
