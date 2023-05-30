package hu.kits.tennis.common;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

public class JsonUtil {

    public static String printJson(JsonStructure json) {
        JsonWriterFactory jwf = Json.createWriterFactory(Map.of(JsonGenerator.PRETTY_PRINTING, true));
        StringWriter sw = new StringWriter();
        try (JsonWriter jsonWriter = jwf.createWriter(sw)) {
            jsonWriter.write(json);
            return sw.toString();
        }
    }
    
    public static JsonStructure readJson(String jsonString) {
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonString))) {
            return jsonReader.read();
        }
    }
    
}
