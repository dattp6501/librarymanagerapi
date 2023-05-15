package utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import global.Init;

public class MapBoxUtil {
    public static JSONObject forwardStringAddressToGeoCoding(String address){
        String addressTmp = address.replaceAll("\\s+", "%20");
        JSONArray list = new JSONArray();
        String urlAPI = "https://api.mapbox.com/geocoding/v5/mapbox.places/"+addressTmp+".json";
        // urlAPI = "https://api.mapbox.com/geocoding/v5/mapbox.places/"+address+".json?bbox=-77.083056,38.908611,-76.997778,38.959167&access_token="+Init.ACCESS_TOKEN_MAPBOX;
        // System.out.println(urlAPI);
        try {
            HttpGet req = new HttpGet(urlAPI);
            URI uri = new URIBuilder(req.getURI())
            .addParameter("access_token", Init.ACCESS_TOKEN_MAPBOX)
            .build();
            CloseableHttpClient httpClient = HttpClients.createDefault();
            req.setURI(uri);
            CloseableHttpResponse response = httpClient.execute(req);
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                httpClient.close();
                return null;
            }
            // return it as a String
            try {
                JSONObject respData = new JSONObject(EntityUtils.toString(entity));
                list = respData.getJSONArray("features");
                // System.out.println(list);
            } catch (Exception e) {
                httpClient.close();
                return null;
            }
            httpClient.close();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return predictAddress(address, list);
    }
    public static JSONObject predictAddress(String address, JSONArray listAddress){
        int max = -1;
        JSONObject res = null;
        boolean ok = false;
        for(Object o : listAddress){
            JSONObject aJSON = (JSONObject)o;
            if(aJSON.has("matching_place_name")){
                ok = true;
                break;
            }   
        }
        for(Object addressJSON : listAddress){
            int count = -1;
            // 
            if(ok && ((JSONObject)addressJSON).has("matching_place_name")){
                count = checkTrue1(address, (JSONObject)addressJSON);
                if(count>max){
                    max = count;
                    res = (JSONObject)addressJSON;
                }
                ok = true;
                continue;
            }
            //
            count = checkTrue(address, (JSONObject)addressJSON);
            if(count>max){
                max = count;
                res = (JSONObject)addressJSON;
            }
        }
        return res;
    }
    public static String replaceVN(String str){
        str = str.replaceAll("à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ", "a");
        str = str.replaceAll("è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ", "e");
        str = str.replaceAll("ì|í|ị|ỉ|ĩ", "i");
        str = str.replaceAll("ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ", "o");
        str = str.replaceAll("ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ", "u");
        str = str.replaceAll("ỳ|ý|ỵ|ỷ|ỹ", "y");
        str = str.replaceAll("đ", "d");
        return str;
    }

    public static int checkTrue(String address, JSONObject addressJSON){
        int count = 0;
        String[] words = replaceVN(address.toLowerCase()).split("\\s+");
        String addressTemp = replaceVN(addressJSON.getString("place_name").toLowerCase());
        for(String w : words){
            count += count(w,addressTemp);
        }
        return count;
    }
    public static int checkTrue1(String address, JSONObject addressJSON){
        int count = 0;
        String[] words = replaceVN(address.toLowerCase()).split("\\s+");
        String addressTemp = replaceVN(addressJSON.getString("matching_place_name").toLowerCase());
        for(String w : words){
            count += count(w,addressTemp);
        }
        return count;
    }
    public static int count(String subStr, String str){
        if(subStr==null || str==null || subStr.equals("") || str.equals("")){
            return 0;
        }
        return ( str.split(subStr, -1).length)-1;
    }
    public static void main(String[] args) {
        System.out.println(forwardStringAddressToGeoCoding("Nhà văn hóa thông anry, hồng châu, đông hưng, thái bình").getJSONObject("geometry").getJSONArray("coordinates").getDouble(0));
    }
}