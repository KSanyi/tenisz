package hu.kits.tennis.infrastructure.invoice;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.common.Environment;
import hu.kits.tennis.common.KITSException;
import hu.kits.tennis.domain.invoice.InvoiceService;
import hu.kits.tennis.domain.player.Player;

public class BillingoInvoiceService implements InvoiceService {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final Environment environment;
    private final String apiKey;
    
    public BillingoInvoiceService(Environment environment, String apiKey) {
        this.environment = environment;
        this.apiKey = apiKey;
    }
    
    @Override
    public void createPartnerForPlayer(Player player) {
        
        HttpClient client = HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                .build();

        String json = createPartnerJson(player);
        
        if(environment != Environment.LIVE) {
            logger.info("Not calling real API in environonment {} with json:\n{}", environment, json);
            return;
        }
        
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.billingo.hu/v3/partners"))
                .header("X-API-KEY", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            
            logger.info("Calling {} with json:\n{}", httpRequest.uri(), json);
            HttpResponse<String> response = client.send(httpRequest, BodyHandlers.ofString());
            
            logger.info("Response: status {}", response.statusCode());
            logger.info("Response: body {}", response.body());
            
        } catch(Exception ex) {
            logger.error("Unable to create player in Billingo", ex);
        }
        
    }
    
    @Override
    public List<String> getPartnerEmails() {
        
        if(environment != Environment.LIVE) {
            logger.info("Not calling real API in environonment {}", environment);
            return List.of();
        }
        
        HttpClient client = HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                .build();
        
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.billingo.hu/v3/partners?per_page=100"))
                .header("X-API-KEY", apiKey)
                .GET()
                .build();
            
            logger.debug("Calling {}", httpRequest.uri());
            HttpResponse<String> response = client.send(httpRequest, BodyHandlers.ofString());
            logger.info("Response: status {}", response.statusCode());
            if(response.statusCode() != 200) {
                throw new KITSException("Billingo response statue code: " + response.statusCode());
            }
            
            return parseEmails(response.body());
        } catch(Exception ex) {
            logger.error("Unable to create player in Billingo", ex);
            throw new KITSException("Billingo response error: " + ex.getMessage());
        }
    }
    
    private static List<String> parseEmails(String body) {
        JSONObject jsonObject = new JSONObject(body);
        List<String> emails = new ArrayList<>();
        for(Object object : jsonObject.getJSONArray("data")) {
            JSONObject partnerJsonObject = (JSONObject)object;
            JSONArray emailsJsonArray = partnerJsonObject.getJSONArray("emails");
            if(!emailsJsonArray.isEmpty()) {
                String email = emailsJsonArray.getString(0);
                emails.add(email);
            }
        }
        return emails;
    }

    private static String createPartnerJson(Player player) {
        
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", player.name());
        JSONArray emailsJsonArray = new JSONArray();
        emailsJsonArray.put(player.contact().email());
        jsonObject.put("emails", emailsJsonArray);
        
        JSONObject addressJsonObject = new JSONObject();
        addressJsonObject.put("country_code", "HU");
        addressJsonObject.put("post_code", player.contact().address().zip());
        addressJsonObject.put("city", player.contact().address().town());
        addressJsonObject.put("address", player.contact().address().streetAddress());
        
        jsonObject.put("address", addressJsonObject);
        
        return jsonObject.toString(2);
    }
    
}
