package utils;

import java.io.BufferedReader;
import java.io.IOException;
import org.json.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class JsonCustom {
    private String stringjson;
    
    public JsonCustom(String stringjson) {
        this.stringjson = stringjson;
    }
    public static JsonCustom JsonToString(BufferedReader reader){
        StringBuilder sb = new StringBuilder();
        String line="";
        try {
            while( (line = reader.readLine()) != null){
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JsonCustom(sb.toString());
    }
    public <T> T toModel(Class<T> tClass){
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            return mapper.readValue(stringjson,tClass);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static <T> String toJson(Object model){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        try {
            return mapper.writeValueAsString(model);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject toJsonObject(BufferedReader reader){
        StringBuilder sb = new StringBuilder();
        try {
            String line="";
            while( (line = reader.readLine()) != null){
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONObject(sb.toString());
    }
    @Override
    public String toString() {
        return stringjson;
    }
}
