package hu.kits.tennis.infrastructure.invoice;

import static java.util.stream.Collectors.toMap;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.common.Clock;
import hu.kits.tennis.common.Environment;
import hu.kits.tennis.common.JsonUtil;
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
    public List<Partner> getPartners() {
        
        if(environment != Environment.LIVE) {
            logger.info("Not calling real API in environonment {}", environment);
            return List.of();
        }
        
        HttpClient client = HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                .build();
        
        List<Partner> partners = new ArrayList<>();
        
        int numberOfPages = getNumberOfPages(client);
        for(int page=1;page<=numberOfPages;page++) {
            partners.addAll(getPartners(client, page));
        }
        
        return partners;
    }
    
    private int getNumberOfPages(HttpClient client) {
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
                throw new KITSException("Billingo response status code: " + response.statusCode());
            }
            
            return JsonUtil.readJson(response.body()).asJsonObject().getInt("last_page");
        } catch(Exception ex) {
            logger.error("Unable to create player in Billingo", ex);
            throw new KITSException("Billingo response error: " + ex.getMessage());
        }
    }
    
    private List<Partner> getPartners(HttpClient client, int page) {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.billingo.hu/v3/partners?per_page=100&page=" + page))
                .header("X-API-KEY", apiKey)
                .GET()
                .build();
            
            logger.debug("Calling {}", httpRequest.uri());
            HttpResponse<String> response = client.send(httpRequest, BodyHandlers.ofString());
            logger.info("Response: status {}", response.statusCode());
            if(response.statusCode() != 200) {
                throw new KITSException("Billingo response statue code: " + response.statusCode());
            }
            
            return parsePartners(response.body());
        } catch(Exception ex) {
            logger.error("Unable to create player in Billingo", ex);
            throw new KITSException("Billingo response error: " + ex.getMessage());
        }
    }
    
    private static List<Partner> parsePartners(String body) {
        
        JsonStructure jsonStructure = JsonUtil.readJson(body);
        
        List<Partner> partners = new ArrayList<>();
        for(JsonValue value : jsonStructure.asJsonObject().getJsonArray("data")) {
            JsonObject partnerJsonObject = value.asJsonObject();
            JsonArray emailsJsonArray = partnerJsonObject.getJsonArray("emails");
            if(!emailsJsonArray.isEmpty()) {
                String email = emailsJsonArray.getString(0);
                partners.add(new Partner(
                        partnerJsonObject.getInt("id"),
                        email,
                        partnerJsonObject.getString("name")));
            }
        }
        return partners;
    }

    private static String createPartnerJson(Player player) {
        
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder()
            .add("name", player.name());
        
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        jsonArrayBuilder.add(player.contact().email());
        jsonObjectBuilder.add("emails", jsonArrayBuilder.build());
        
        JsonObject addressJsonObject = Json.createObjectBuilder()
                .add("country_code", "HU")
                .add("post_code", player.contact().address().zip())
                .add("city", player.contact().address().town())
                .add("address", player.contact().address().streetAddress())
                .build();
        
        jsonObjectBuilder.add("address", addressJsonObject);
        
        return JsonUtil.printJson(jsonObjectBuilder.build());
    }

    @Override
    public List<String> createAndSendInvoices(List<String> emailAddresses, int amount) {
        
        Map<String, Long> emailsToPartnerIds = getPartners().stream().collect(toMap(Partner::email, Partner::partnerId));
        
        List<String> emailsInvoiceSentSuccesfully = new ArrayList<>();
        for(String emailAddress : emailAddresses) {
            
            Long partnerId = emailsToPartnerIds.get(emailAddress);
            if(partnerId != null) {
                try {
                    long invoiceID = createInvoice(partnerId, amount);
                    sendInvoice(emailAddress, invoiceID);
                    logger.info("Invoice created and sent successfully to {} (partner id {})", emailAddress, partnerId);
                    emailsInvoiceSentSuccesfully.add(emailAddress);    
                } catch(Exception ex) {
                    logger.error("Error creating or sending invoice to {}", emailAddress, ex);
                }
            } else {
                logger.error("Cant find partner with email address {}", emailAddress);
            }
        }
        
        return emailsInvoiceSentSuccesfully;
    }
    
    private long createInvoice(long partnerId, int amount) {
        
        JsonArray itemsArray = Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                        .add("name", "Sport rendezvény")
                        .add("quantity", 1)
                        .add("unit_price", amount)
                        .add("unit_price_type", "gross")
                        .add("unit", "db")
                        .add("vat", "0%")
                        .add("entitlement", "AAM")
                        .build())
                .build();
        
        JsonObject jsonObject = Json.createObjectBuilder()
                .add("partner_id", partnerId)
                .add("block_id", 195296)
                .add("bank_account_id", 159185)
                .add("type", "invoice")
                .add("fulfillment_date", Clock.today().toString())
                .add("due_date", Clock.today().toString())
                .add("payment_method", "wire_transfer")
                .add("currency", "HUF")
                .add("electronic", false)
                .add("language", "hu")
                .add("items", itemsArray)
                .build();
        
        String jsonString = JsonUtil.printJson(jsonObject);
        
        try {
            HttpClient client = HttpClient.newBuilder()
                    .version(Version.HTTP_1_1)
                    .build();
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.billingo.hu/v3/documents"))
                .header("X-API-KEY", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();
            
            logger.info("Calling {} with json:\n{}", httpRequest.uri(), jsonString);
            HttpResponse<String> response = client.send(httpRequest, BodyHandlers.ofString());
            
            logger.info("Response: status {}", response.statusCode());
            logger.info("Response: body {}", response.body());
            
            if(response.statusCode() != 201) {
                throw new KITSException("Billingo create invoice response error: " + response.body());
            }
            
            return JsonUtil.readJson(response.body()).asJsonObject().getInt("id");
        } catch(Exception ex) {
            logger.error("Unable to create invoice", ex);
            throw new KITSException("Billingo response error: " + ex.getMessage());
        }
    }
    
    private void sendInvoice(String emailAddress, long invoiceId) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .version(Version.HTTP_1_1)
                    .build();
            
            JsonObject emailJsonObject = Json.createObjectBuilder()
                    .add("emails", Json.createArrayBuilder(List.of(emailAddress)).build())
                    .build();
            
            String jsonString = JsonUtil.printJson(emailJsonObject);
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.billingo.hu/v3/documents/" + invoiceId + "/send"))
                .header("X-API-KEY", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();
            
            logger.info("Calling {} with json:\n{}", httpRequest.uri(), jsonString);
            HttpResponse<String> response = client.send(httpRequest, BodyHandlers.ofString());
            
            logger.info("Response: status {}", response.statusCode());
            logger.info("Response: body {}", response.body());
            
            if(response.statusCode() != 200) {
                throw new KITSException("Billingo send invoice response error: " + response.body());
            }
        } catch(Exception ex) {
            logger.error("Unable to send invoice", ex);
            throw new KITSException("Billingo send invoice response error: " + ex.getMessage());
        }
    }
    
}
